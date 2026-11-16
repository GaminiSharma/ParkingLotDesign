package com.parking.controller;

import com.parking.application.CreateLotCommand;
import com.parking.application.ManagedLot;
import com.parking.application.SpotGroup;
import com.parking.controller.dto.CreateLotRequest;
import com.parking.controller.dto.LotResponse;
import com.parking.controller.dto.ReceiptResponse;
import com.parking.controller.dto.SpotResponse;
import com.parking.controller.dto.TicketResponse;
import com.parking.lot.ExitResult;
import com.parking.model.ParkingSpot;
import com.parking.model.Ticket;

final class ApiMapper {
    private ApiMapper() {
    }

    static CreateLotCommand toCommand(CreateLotRequest r) {
        return new CreateLotCommand(r.lotId(), r.pricingPlan(),
                r.spots().stream().map(s -> new SpotGroup(s.type(), s.floor(), s.count())).toList());
    }

    static LotResponse toResponse(ManagedLot m) {
        return new LotResponse(m.id(), m.pricingPlan(), m.capacity(), m.lot().availability());
    }

    static TicketResponse toResponse(String lotId, Ticket t) {
        return new TicketResponse(t.id(), lotId, t.vehicle().licensePlate(), t.vehicle().type(), toResponse(t.spot()), t.entryTime());
    }

    static ReceiptResponse toResponse(ExitResult.Completed c) {
        Ticket t = c.ticket();
        return new ReceiptResponse(t.id(), t.vehicle().licensePlate(), t.vehicle().type(), toResponse(t.spot()),
                t.entryTime(), c.exitTime(), c.fee(), c.paymentReference());
    }

    private static SpotResponse toResponse(ParkingSpot s) {
        return new SpotResponse(s.number(), s.floor(), s.type());
    }
}
