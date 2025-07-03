package com.scheduler.scheduleservice.repository;

import com.scheduler.scheduleservice.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    /**
     * Finds all availability rules for a specific user.
     *
     * @param userId the ID of the user (provider)
     * @return a list of availability rules for that user
     */
    List<Availability> findByUserId(Long userId);
}
