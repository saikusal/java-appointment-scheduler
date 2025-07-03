package com.scheduler.userservice.repository;

import com.scheduler.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);
}
