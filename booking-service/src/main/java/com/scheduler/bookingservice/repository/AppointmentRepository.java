package com.scheduler.bookingservice.repository;

import com.scheduler.bookingservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds all appointments for a specific user that fall within a given time range.
     * This is essential for checking for scheduling conflicts.
     *
     * @param userId the ID of the user (provider)
     * @param start the start of the time range
     * @param end the end of the time range
     * @return a list of conflicting appointments
     */
    List<Appointment> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
