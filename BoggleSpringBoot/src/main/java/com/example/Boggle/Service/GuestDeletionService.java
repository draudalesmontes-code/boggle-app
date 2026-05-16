package com.example.Boggle.Service;

import com.example.Boggle.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service that periodically removes expired guest accounts.
 *
 * <p>Runs on a fixed schedule and deletes guest users whose accounts
 * were created more than 24 hours ago.
 */
@Service
public class GuestDeletionService {
    private UserRepository userRepository;

    /**
     * Creates the service with the repository required to delete guest users.
     *
     * @param userRepository repository used to find and delete expired guests
     */
    public GuestDeletionService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Deletes guest users whose accounts are older than 24 hours.
     *
     * <p>Runs automatically every hour.
     */
    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void removeExpiredGuest(){
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int deleted = userRepository.deleteExpiredGuests(cutoff);
        System.out.println("[GuestCleanup] Deleted " + deleted + " expired guest(s)");

    }

}