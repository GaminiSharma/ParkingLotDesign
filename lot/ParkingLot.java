package com.parking.lot;

import com.parking.model.ParkingSpot;
import com.parking.model.SpotType;
import com.parking.model.Ticket;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.payment.PaymentResult;
import com.parking.payment.PaymentStrategy;
import com.parking.pricing.PricingStrategy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One parking lot. Thread-safe and lock-free on the hot path.
 *
 * Why it scales and is correct:
 *  - allocation = {@code pollFirst()} on a per-type sorted free set: O(log n), atomic, and it hands the
 *    spot to exactly one caller (the old version scanned all spots, O(n), with a check-then-act race);
 *  - "one active ticket per plate" is enforced by a single putIfAbsent (the linearisation point);
 *  - exit is serialised per ticket with a lock, so a double click can never charge twice;
 *  - the fee is quoted once and pinned, so a retry after a failed payment never changes the amount;
 *  - availability counters give O(1) reads for display boards.
 */
public final class ParkingLot {

    /** Mutable state of one active ticket. Everything except {@code ticket} is guarded by {@code lock}. */
    private static final class Session {
        final Ticket ticket;
        final ReentrantLock lock = new ReentrantLock(); // ReentrantLock (not synchronized): payment I/O must not pin virtual threads
        Quote quote;
        boolean closed;

        Session(Ticket ticket) {
            this.ticket = ticket;
        }
    }

    private record Quote(Instant exitTime, BigDecimal fee) {}

    private final Map<SpotType, ConcurrentSkipListSet<ParkingSpot>> freeSpots = new EnumMap<>(SpotType.class);
    private final Map<SpotType, AtomicInteger> freeCounts = new EnumMap<>(SpotType.class);
    private final Map<String, Session> sessionsByPlate = new ConcurrentHashMap<>();
    private final Map<String, Session> sessionsByTicketId = new ConcurrentHashMap<>();
    private final PricingStrategy pricing;
    private final Clock clock;

    public ParkingLot(Collection<ParkingSpot> spots, PricingStrategy pricing, Clock clock) {
        this.pricing = Objects.requireNonNull(pricing, "pricing");
        this.clock = Objects.requireNonNull(clock, "clock");
        for (SpotType t : SpotType.values()) {
            freeSpots.put(t, new ConcurrentSkipListSet<>());
            freeCounts.put(t, new AtomicInteger());
        }
        Set<String> seen = new HashSet<>();
        for (ParkingSpot s : spots) {
            if (!seen.add(s.floor() + ":" + s.number())) {
                throw new IllegalArgumentException("Duplicate spot " + s.floor() + ":" + s.number());
            }
            free(s);
        }
    }

    // ------------------------------------------------------------------ entry

    public ParkResult park(Vehicle vehicle) {
        Session existing = sessionsByPlate.get(vehicle.licensePlate());
        if (existing != null) {
            return new ParkResult.AlreadyParked(existing.ticket); // fast path for retries
        }
        ParkingSpot spot = allocate(vehicle.type());
        if (spot == null) {
            return new ParkResult.LotFull(vehicle.type());
        }
        Session session = new Session(new Ticket(UUID.randomUUID().toString(), vehicle, spot, clock.instant()));
        Session prior = sessionsByPlate.putIfAbsent(vehicle.licensePlate(), session); // linearisation point
        if (prior != null) {                       // lost a race against the same plate: give the spot back
            free(spot);
            return new ParkResult.AlreadyParked(prior.ticket);
        }
        sessionsByTicketId.put(session.ticket.id(), session);
        return new ParkResult.Parked(session.ticket);
    }

    // ------------------------------------------------------------------ exit

    public ExitResult exit(String ticketId, PaymentStrategy payment) {
        Session s = sessionsByTicketId.get(ticketId);
        if (s == null) {
            return new ExitResult.UnknownTicket(ticketId);
        }
        s.lock.lock();
        try {
            if (s.closed) {
                return new ExitResult.UnknownTicket(ticketId); // a concurrent exit already finished
            }
            if (s.quote == null) {
                Instant now = clock.instant();
                Duration parked = Duration.between(s.ticket.entryTime(), now);
                s.quote = new Quote(now, pricing.feeFor(s.ticket.vehicle().type(), parked));
            }
            PaymentResult result = payment.pay(s.quote.fee());
            return switch (result) {
                case PaymentResult.Failure f -> new ExitResult.PaymentFailed(f.reason());
                case PaymentResult.Success ok -> {
                    s.closed = true;
                    sessionsByPlate.remove(s.ticket.vehicle().licensePlate(), s);
                    sessionsByTicketId.remove(ticketId);
                    free(s.ticket.spot());  // last: only now may another vehicle take the spot
                    yield new ExitResult.Completed(s.ticket, s.quote.fee(), s.quote.exitTime(), ok.reference());
                }
            };
        } finally {
            s.lock.unlock();
        }
    }

    // ------------------------------------------------------------------ queries

    /** Active (not yet exited) ticket by id. */
    public Optional<Ticket> findTicket(String ticketId) {
        Session s = sessionsByTicketId.get(ticketId);
        return s == null ? Optional.empty() : Optional.of(s.ticket);
    }

    /** O(1), eventually consistent snapshot suitable for a display board. */
    public Map<SpotType, Integer> availability() {
        Map<SpotType, Integer> out = new EnumMap<>(SpotType.class);
        freeCounts.forEach((type, count) -> out.put(type, Math.max(0, count.get())));
        return out;
    }

    public int availableSpots(SpotType type) {
        return Math.max(0, freeCounts.get(type).get());
    }

    // ------------------------------------------------------------------ internals

    /** Best-fit first (VehicleType preference order), nearest spot first within a type. */
    private ParkingSpot allocate(VehicleType vehicleType) {
        for (SpotType type : vehicleType.acceptableSpots()) {
            ParkingSpot spot = freeSpots.get(type).pollFirst(); // atomic: one winner per spot
            if (spot != null) {
                freeCounts.get(type).decrementAndGet();
                return spot;
            }
        }
        return null;
    }

    private void free(ParkingSpot spot) {
        freeSpots.get(spot.type()).add(spot);
        freeCounts.get(spot.type()).incrementAndGet();
    }
}
