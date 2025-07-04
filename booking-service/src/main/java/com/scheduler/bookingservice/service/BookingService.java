package com.scheduler.bookingservice.service;

import com.scheduler.bookingservice.dto.AvailabilityDTO;
import com.scheduler.bookingservice.dto.ServiceDTO;
import com.scheduler.bookingservice.model.Appointment;
import com.scheduler.bookingservice.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final WebClient webClient;

    public BookingService(AppointmentRepository appointmentRepository, WebClient.Builder webClientBuilder, @Value("${schedule.service.url}") String scheduleServiceUrl) {
        this.appointmentRepository = appointmentRepository;
        this.webClient = webClientBuilder.baseUrl(scheduleServiceUrl).build();
    }

    public Mono<List<LocalDateTime>> getAvailableSlots(Long providerId, Long serviceId, LocalDate date) {
        // 1. Fetch the service details to get the duration
        Mono<ServiceDTO> serviceMono = webClient.get()
                .uri("/services/{id}", serviceId) // Assuming this endpoint exists in schedule-service
                .retrieve()
                .bodyToMono(ServiceDTO.class);

        // 2. Fetch the provider's availability for the given day of the week
        Flux<AvailabilityDTO> availabilityFlux = webClient.get()
                .uri("/availability/provider/{providerId}", providerId)
                .retrieve()
                .bodyToFlux(AvailabilityDTO.class)
                .filter(a -> a.getDayOfWeek() == date.getDayOfWeek().getValue() % 7); // Adjust for 0=Sun, 1=Mon...

        // 3. Fetch existing appointments for that day
        List<Appointment> existingAppointments = appointmentRepository.findByProviderIdAndStartTimeBetween(
                providerId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        // 4. Combine the results and calculate slots
        return Mono.zip(serviceMono, availabilityFlux.collectList())
                .map(tuple -> {
                    ServiceDTO service = tuple.getT1();
                    List<AvailabilityDTO> availabilities = tuple.getT2();
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
                });
    }

    public Mono<Appointment> createAppointment(Appointment appointment) {
        // In a real application, you would re-validate the slot here before saving
        return Mono.just(appointmentRepository.save(appointment));
    }
}
