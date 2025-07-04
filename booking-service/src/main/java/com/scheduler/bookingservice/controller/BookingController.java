package com.scheduler.bookingservice.controller;

import com.scheduler.bookingservice.model.Appointment;
import com.scheduler.bookingservice.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping
    public Mono<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return bookingService.createAppointment(appointment);
    }

    @GetMapping("/availability/{providerId}")
    public Mono<ResponseEntity<List<LocalDateTime>>> getAvailableSlots(
            @PathVariable Long providerId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        return bookingService.getAvailableSlots(providerId, serviceId, date)
                .map(ResponseEntity::ok);
    }
}
