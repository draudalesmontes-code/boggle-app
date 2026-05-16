package com.example.Boggle.util;

import com.example.Boggle.Model.Controllers.UserController;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the profile picture feature.
 *
 * <p>Covers the {@link User} entity field, the {@link UserController.UserResponse} DTO mapping,
 * and the {@code PUT /api/users/{userId}/avatar} endpoint logic.
 */
@DisplayName("Profile Picture Tests")
class ProfilePictureTest {

    private UserRepository userRepository;
    private UserController userController;

    /**
     * Sets up a mocked repository and controller before each test.
     */
    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userController = new UserController(userRepository);
    }

    /**
     * Verifies that a newly created User has no profile picture by default.
     */
    @Test
    void newUserHasNullProfilePictureByDefault() {
        User user = new User("testUser", "test@test.com", "hashedPassword");
        assertNull(user.getProfilePicture());
    }

    /**
     * Verifies that updating with a valid avatar filename succeeds
     * and returns the updated profilePicture in the response.
     */
    @Test
    void updateAvatarSucceedsWithValidFilename() {
        User user = new User("testUser", "test@test.com", "hashedPassword");
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserController.UpdateAvatarRequest req = new UserController.UpdateAvatarRequest();
        req.profilePicture = "avatar_fox.png";

        UserController.UserResponse response = userController.updateAvatar(1, req);

        assertEquals("avatar_fox.png", response.profilePicture);
    }

    /**
     * Verifies that submitting an invalid avatar filename is rejected with 400 BAD_REQUEST.
     */
    @Test
    void updateAvatarRejectsInvalidFilename() {
        User user = new User("testUser", "test@test.com", "hashedPassword");
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserController.UpdateAvatarRequest req = new UserController.UpdateAvatarRequest();
        req.profilePicture = "hacker_image.png";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateAvatar(1, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    /**
     * Verifies that attempting to update the avatar for a non-existent user
     * returns 404 NOT_FOUND.
     */
    @Test
    void updateAvatarReturnsNotFoundForMissingUser() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        UserController.UpdateAvatarRequest req = new UserController.UpdateAvatarRequest();
        req.profilePicture = "avatar_bear.png";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateAvatar(999, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    /**
     * Simulates persistence across sessions — verifies that after saving a profile picture,
     * re-fetching the user from the repository still returns the updated value.
     */
    @Test
    void profilePicturePersistedAfterSave() {
        User user = new User("testUser", "test@test.com", "hashedPassword");
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            when(userRepository.findById(1)).thenReturn(Optional.of(saved));
            return saved;
        });

        UserController.UpdateAvatarRequest req = new UserController.UpdateAvatarRequest();
        req.profilePicture = "avatar_robot.png";

        userController.updateAvatar(1, req);

        User reFetched = userRepository.findById(1).orElseThrow();
        assertEquals("avatar_robot.png", reFetched.getProfilePicture());
    }
}
