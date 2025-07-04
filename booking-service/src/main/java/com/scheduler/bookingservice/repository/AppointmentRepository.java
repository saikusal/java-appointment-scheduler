package com.scheduler.bookingservice.repository;

import com.scheduler.bookingservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find all appointments for a specific provider that fall within a given time range
    List<Appointment> findByProviderIdAndStartTimeBetween(Long providerId, LocalDateTime start, LocalDateTime end);

}
