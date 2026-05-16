package com.example.Boggle.Service;

import com.example.Boggle.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestDeletionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GuestDeletionService guestDeletionService;

    @Test
    void removeExpiredGuest_deletesGuestsOlderThan24Hours() {
        when(userRepository.deleteExpiredGuests(any(LocalDateTime.class))).thenReturn(3);

        guestDeletionService.removeExpiredGuest();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository).deleteExpiredGuests(cutoffCaptor.capture());

        // The cutoff passed should be approximately 24 hours ago
        LocalDateTime captured = cutoffCaptor.getValue();
        LocalDateTime expectedCutoff = LocalDateTime.now().minusHours(24);
        assertTrue(captured.isBefore(expectedCutoff.plusSeconds(5)),
                "Cutoff should be ~24 hours in the past");
        assertTrue(captured.isAfter(expectedCutoff.minusSeconds(5)),
                "Cutoff should be ~24 hours in the past");
    }

    @Test
    void removeExpiredGuest_noGuestsToDelete_deletesZero() {
        when(userRepository.deleteExpiredGuests(any(LocalDateTime.class))).thenReturn(0);

        guestDeletionService.removeExpiredGuest();

        verify(userRepository, times(1)).deleteExpiredGuests(any(LocalDateTime.class));
    }

    @Test
    void removeExpiredGuest_guestStillWithinTimeWindow_isNotDeleted() {
        // Repository returns 0 meaning the guest created recently was not deleted
        when(userRepository.deleteExpiredGuests(any(LocalDateTime.class))).thenReturn(0);

        guestDeletionService.removeExpiredGuest();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository).deleteExpiredGuests(cutoffCaptor.capture());

        // A guest created just now should be newer than the cutoff, meaning they are safe
        LocalDateTime recentGuestCreatedAt = LocalDateTime.now();
        assertTrue(recentGuestCreatedAt.isAfter(cutoffCaptor.getValue()),
                "A recently created guest should be newer than the cutoff and not deleted");
    }
}
