package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Repository for User entities.
 *
 * Provides standard CRUD operations and query methods for
 * locating users by email or username, as well as checking
 * whether a username is already taken.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Query("DELETE FROM User u WHERE u.isGuest = true AND u.username != 'bot' AND u.createdAt < :cutoff")
    int deleteExpiredGuests(@Param("cutoff") LocalDateTime cutoff);
}
