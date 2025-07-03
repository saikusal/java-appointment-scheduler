package com.scheduler.scheduleservice.repository;

import com.scheduler.scheduleservice.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Finds all services offered by a specific user.
     *
     * @param userId the ID of the user (provider)
     * @return a list of services for that user
     */
    List<Service> findByUserId(Long userId);
}
