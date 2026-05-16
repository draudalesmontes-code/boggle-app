package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.Security.PasswordUtil;
import com.example.Boggle.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

/**
 * REST controller for user registration, login, and guest account creation.
 *
 * <p>This controller supports creating registered users, authenticating
 * existing non-guest users, and generating guest accounts for temporary play.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private static final SecureRandom rng = new SecureRandom();

    /**
     * Creates a controller with access to user persistence.
     *
     * @param userRepository repository for user records
     */
    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    /**
     * Request body for registering a new user account.
     */
    public static class RegisterRequest{
        /**
         * The request username.
         */
        public String username;
        /**
         * The user's email address
         */
        public String email;
        /**
         * The user's plain-text password.
         */
        public String password;
    }

    /**
     * Request body for login/authenticating an existing user.
     */
    public static class LoginRequest{
        /**
         * The username used for login.
         */
        public String username;
        /**
         * The plain-text password used for login.
         */
        public String password;
    }

    /**
     * Optional request body for creating a guest account.
     *
     * <p>If a username is provided, the controller attempts to use it as the
     * base guest name; otherwise a generated guest name is used.
     */
    public static class GuestRequest{
        /**
         * Optional preferred guest username.
         */
        public String username;
    }

    /**
     * Response body representing a user returned by the API.
     */
    public static class UserResponse{
        /**
         * The user ID.
         */
        public Integer id;

        /**
         * The username
         */
        public String username;

        /**
         * The email address.
         */
        public String email;

        /**
         * The profile picture filename, or null if none selected.
         */
        public String profilePicture;

        /**
         * Builds a response DTO from a user entity
         *
         * @param u the user entity
         * @return a response containing public user data
         */
        public static UserResponse userDTO(User u){
            UserResponse out = new UserResponse();
            out.id = u.getId();
            out.username = u.getUsername();
            out.email = u.getEmail();
            out.profilePicture = u.getProfilePicture();

            return out;
        }
    }

    /**
     * Request body for updating a user's profile picture.
     */
    public static class UpdateAvatarRequest {
        /**
         * The filename of the selected avatar (e.g. "avatar_bear.png").
         */
        public String profilePicture;
    }

    /**
     * Registers a new (non-guest) user account
     *
     * @param req the registration request.
     * @return the created user response.
     * @throws ResponseStatusException if required fields are missing or the
     *                                 username or email is laready in use
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public UserResponse register (@RequestBody RegisterRequest req) {
        if(req == null) throw new ResponseStatusException(BAD_REQUEST,"Body is required");
        String username = require(req.username,"username");
        String email = require(req.email,"email");
        String password = require(req.password,"password");

        if(userRepository.existsByUsername(username)){
            throw new ResponseStatusException(CONFLICT,"Username already taken.");
        }
        if(userRepository.findByEmail(email).isPresent()){
            throw new ResponseStatusException(CONFLICT,"Email Already Taken");
        }

        String passwordHash = PasswordUtil.hash(password);

        try{
            User u = new User(username, email, passwordHash);
            u.setGuest(false);
            u = userRepository.save(u);
            return UserResponse.userDTO(u);
        } catch (DataIntegrityViolationException e){
            throw new ResponseStatusException(CONFLICT, "Username or email already taken");
        }
    }

    /**
     * Authenticates an existing user.
     *
     * @param req the login request
     * @return the login user response
     * @throws ResponseStatusException if the request is invalid or the credentials
     *                                 do not match
     */
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest req) {
        if (req == null) throw new ResponseStatusException(BAD_REQUEST, "Body is required");
        String username = require(req.username, "username");
        String password = require(req.password, "password");

        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid username or password"));

        // Optional policy: guests can't log in with password
        if (u.isGuest()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
        }

        if (!PasswordUtil.verify(password, u.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
        }

        return UserResponse.userDTO(u);
    }

    /**
     * Create a guest user account
     *
     * <p>If a preferred username is provided, the controller attempts to make
     * it unique. Otherwise, a generated guest username is used.
     *
     * @param req the optional guest creation request
     *             (custom username or auto-generated)
     * @return the created guest user response
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/guest")
    public UserResponse guest(@RequestBody(required = false) GuestRequest req) {
        String desired = (req == null) ? "" : safe(req.username);
        String base = desired.isBlank() ? generateGuestUsername() : desired.trim();

        String username = makeUniqueUsername(base);

        // DB requires NOT NULL email + password_hash :contentReference[oaicite:4]{index=4}
        String email = "guest_" + UUID.randomUUID() + "@guest.local";

        // Guests won't use passwords, but store a valid hash format anyway.
        String passwordHash = PasswordUtil.hash(UUID.randomUUID().toString());

        try {
            User u = new User(username, email, passwordHash);
            u.setGuest(true);
            u = userRepository.save(u);
            return UserResponse.userDTO(u);
        } catch (DataIntegrityViolationException e) {
            String retryUsername = makeUniqueUsername("Guest-" + shortToken());
            User u = new User(retryUsername, "guest_" + UUID.randomUUID() + "@guest.local",
                    PasswordUtil.hash(UUID.randomUUID().toString()));
            u.setGuest(true);
            u = userRepository.save(u);
            return UserResponse.userDTO(u);
        }
    }

    private static final java.util.Set<String> ALLOWED_AVATARS = java.util.Set.of(
        "avatar_bear.png", "avatar_blocks.png", "avatar_cube.png",
        "avatar_fox.png", "avatar_hourglass.png", "avatar_owl.png", "avatar_robot.png"
    );

    /**
     * Updates the profile picture for an existing user.
     *
     * @param userId the ID of the user to update
     * @param req    the request containing the chosen avatar filename
     * @return the updated user response
     * @throws ResponseStatusException if the user is not found or the avatar is invalid
     */
    @PutMapping("/{userId}/avatar")
    public UserResponse updateAvatar(@PathVariable Integer userId,
                                     @RequestBody UpdateAvatarRequest req) {
        if (req == null || req.profilePicture == null || req.profilePicture.isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "profilePicture is required");
        if (!ALLOWED_AVATARS.contains(req.profilePicture.trim()))
            throw new ResponseStatusException(BAD_REQUEST, "Invalid profile picture");

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        u.setProfilePicture(req.profilePicture.trim());
        u = userRepository.save(u);
        return UserResponse.userDTO(u);
    }

    /**
     * Validates that a required string field is present and non-blank.
     *
     * @param value the input value
     * @param field the field name for error messages
     * @return the trimmed value
     * @throws ResponseStatusException if the value is null or blank
     */
    private String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    /**
     * Returns a non-null version of the provided string.
     *
     * @param s the input string
     * @return the original string, or an empty string if null
     */
    private String safe(String s) { return s == null ? "" : s; }


    /**
     * Generates a unique username based on the provided base value.
     *
     * @param base the desired base username
     * @return a username that does not currently exist in the repository
     */
    private String makeUniqueUsername(String base) {
        String candidate = base;
        int tries = 0;
        while (userRepository.existsByUsername(candidate)) {
            tries++;
            if (tries > 25) return base + "-" + UUID.randomUUID();
            candidate = base + "-" + shortToken();
        }
        return candidate;
    }

    /**
     * Generates a default guest username.
     *
     * @return a guest username with a short random suffix
     */
    private String generateGuestUsername() { return "Guest-" + shortToken(); }

    /**
     * Generates a short random URL-safe token.
     *
     * @return a short random token string
     */
    private String shortToken() {
        byte[] b = new byte[3];
        rng.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private void deleteExpiredGuests(){

    }
}
