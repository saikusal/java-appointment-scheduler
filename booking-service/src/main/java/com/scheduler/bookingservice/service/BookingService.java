package com.scheduler.bookingservice.service;

import com.scheduler.bookingservice.dto.AvailabilityDTO;
import com.scheduler.bookingservice.dto.ServiceDTO;
import com.scheduler.bookingservice.model.Appointment;
import com.scheduler.bookingservice.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;
    private final String scheduleServiceUrl;

    public BookingService(AppointmentRepository appointmentRepository, RestTemplate restTemplate, @Value("${SCHEDULE_SERVICE_URL}") String scheduleServiceUrl) {
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
        this.scheduleServiceUrl = scheduleServiceUrl;
    }

    public List<LocalDateTime> getAvailableSlots(Long providerId, Long serviceId, LocalDate date) {
        // 1. Fetch the service details to get the duration
        ServiceDTO service = restTemplate.getForObject(scheduleServiceUrl + "/services/" + serviceId, ServiceDTO.class);

        if (service == null) {
            return new ArrayList<>();
        }

        // 2. Fetch the provider's availability for the given day of the week
        ResponseEntity<List<AvailabilityDTO>> availabilityResponse = restTemplate.exchange(
                scheduleServiceUrl + "/availability/provider/" + providerId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AvailabilityDTO>>() {}
        );
        List<AvailabilityDTO> allAvailabilities = availabilityResponse.getBody();
        
        if (allAvailabilities == null || allAvailabilities.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter for the correct day of the week
        List<AvailabilityDTO> availabilities = allAvailabilities.stream()
                .filter(a -> a.getDayOfWeek() == date.getDayOfWeek().getValue() % 7)
                .collect(Collectors.toList());

        if (availabilities.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Fetch existing appointments for that day
        List<Appointment> existingAppointments = appointmentRepository.findByProviderIdAndStartTimeBetween(
                providerId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        // 4. Calculate slots
        List<LocalDateTime> availableSlots = new ArrayList<>();
        for (AvailabilityDTO availability : availabilities) {
            LocalDateTime slot = date.atTime(availability.getStartTime());
            // Loop as long as the end of the new slot is not after the provider's end time
            while (!slot.plusMinutes(service.getDurationMinutes()).toLocalTime().isAfter(availability.getEndTime())) {
                final LocalDateTime currentSlot = slot;
                boolean isBooked = existingAppointments.stream()
                        .anyMatch(a -> a.getStartTime().equals(currentSlot));

                if (!isBooked) {
                    availableSlots.add(currentSlot);
                }
                slot = slot.plusMinutes(service.getDurationMinutes());
            }
        }
        return availableSlots;
    }

    public Appointment createAppointment(Appointment appointment) {
        // 1. Get the service duration to calculate the end time
        ServiceDTO service = restTemplate.getForObject(scheduleServiceUrl + "/services/" + appointment.getServiceId(), ServiceDTO.class);
        if (service == null) {
            // In a real app, you'd throw a proper exception here
            throw new IllegalStateException("Service not found, cannot calculate end time.");
        }

        // 2. Calculate and set the end time
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(service.getDurationMinutes());
        appointment.setEndTime(endTime);

        // 3. Save the completed appointment
        // In a real application, you would also re-validate here that the slot hasn't been taken
        // since the user loaded the page.
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByProvider(Long providerId) {
        return appointmentRepository.findByProviderId(providerId);
    }
}
