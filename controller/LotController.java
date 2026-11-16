package com.parking.controller;

import java.net.URI;
import java.time.Clock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.parking.application.ParkOutcome;
import com.parking.application.ParkingService;
import com.parking.controller.dto.ExitTicketRequest;
import com.parking.controller.dto.ParkVehicleRequest;
import com.parking.controller.dto.ReceiptResponse;
import com.parking.controller.dto.TicketResponse;
import com.parking.lot.ExitResult;
import com.parking.model.Vehicle;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/{lotId}")
class LotController {
	private final ParkingService service;
	private final Clock clock;

	LotController(ParkingService service, Clock clock) {
		this.service = service;
		this.clock = clock;
	}

	@PostMapping
	@Operation(summary = "Park a vehicle. 201 new ticket; 200 same ticket if the plate is already parked (safe to retry); 409 lot full")
	ResponseEntity<TicketResponse> parkingEntry(@PathVariable("lotId") String lotId,
			@Valid @RequestBody ParkVehicleRequest request) {
		ParkOutcome outcome = service.park(lotId, new Vehicle(request.licensePlate(), request.vehicleType()));
		TicketResponse body = ApiMapper.toResponse(lotId, outcome.ticket());
		URI location = UriComponentsBuilder.fromPath("/api/v1/{lotId}/{ticketId}")
				.buildAndExpand(lotId, outcome.ticket().id()).encode().toUri();
		return outcome.created() ? ResponseEntity.created(location).body(body)
				: ResponseEntity.ok().location(location).body(body);
	}

	@GetMapping("{ticketId}")
	@Operation(summary = "Get an active ticket (404 once the vehicle has exited)")
	TicketResponse get(@PathVariable("lotId") String lotId, @PathVariable("ticketId") String ticketId) {
		return ApiMapper.toResponse(lotId, service.getTicket(lotId, ticketId));
	}

	@PostMapping("{ticketId}/exit")
	@Operation(summary = "Pay and leave. 200 receipt; 402 payment failed (ticket stays active, fee is pinned); 404 unknown or already exited")
	ReceiptResponse parkingExit(@PathVariable("lotId") String lotId, @PathVariable("ticketId") String ticketId,
			@Valid @RequestBody ExitTicketRequest request) {
		ExitResult.Completed completed = service.exit(lotId, ticketId, request.paymentMethod());
		return ApiMapper.toResponse(completed);
	}
}
