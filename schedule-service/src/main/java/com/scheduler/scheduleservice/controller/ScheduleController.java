package com.scheduler.scheduleservice.controller;

import com.scheduler.scheduleservice.model.Availability;
import com.scheduler.scheduleservice.model.Service;
import com.scheduler.scheduleservice.repository.AvailabilityRepository;
import com.scheduler.scheduleservice.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;

    public ScheduleController(ServiceRepository serviceRepository, AvailabilityRepository availabilityRepository) {
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
    }

    // --- Service Endpoints ---

    @PostMapping("/services")
    public ResponseEntity<Service> createService(@RequestBody Service service) {
        Service savedService = serviceRepository.save(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
    }

    @GetMapping("/services/provider/{userId}")
    public ResponseEntity<List<Service>> getServicesByProvider(@PathVariable Long userId) {
        List<Service> services = serviceRepository.findByUserId(userId);
        return ResponseEntity.ok(services);
    }

    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        // In a real app, you'd also check if the user owns this service before deleting.
        serviceRepository.deleteById(serviceId);
        return ResponseEntity.noContent().build();
    }

    // --- Availability Endpoints ---

    @PostMapping("/availability")
    public ResponseEntity<List<Availability>> saveAvailability(@RequestBody List<Availability> availabilityList) {
        // This endpoint can be used to save the entire weekly schedule at once.
        // It assumes the frontend sends a list of availability objects.
        List<Availability> savedList = availabilityRepository.saveAll(availabilityList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
    }

    @GetMapping("/availability/provider/{userId}")
    public ResponseEntity<List<Availability>> getAvailabilityByProvider(@PathVariable Long userId) {
        List<Availability> availability = availabilityRepository.findByUserId(userId);
        return ResponseEntity.ok(availability);
    }
}
