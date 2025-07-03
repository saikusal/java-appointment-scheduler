package com.scheduler.bookingservice.controller;

import com.scheduler.bookingservice.model.Appointment;
import com.scheduler.bookingservice.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/slots/available")
    public Mono<ResponseEntity<List<LocalDateTime>>> getAvailableSlots(
            @RequestParam Long userId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        return bookingService.getAvailableSlots(userId, serviceId, date)
                .map(slots -> ResponseEntity.ok(slots));
    }

    @PostMapping
    public Mono<ResponseEntity<Appointment>> createAppointment(@RequestBody Appointment appointment) {
        // In a real-world scenario, you'd perform more validation here,
        // ensuring the requested slot is still available before creating the appointment.
        return bookingService.createAppointment(appointment)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }
}
