package com.example.Boggle;

import com.example.Boggle.repository.GameRepository;
import com.example.Boggle.repository.UserRepository;
import com.example.Boggle.Model.Tables.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Run on a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class GameIntegrationAPITests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    // Creates one session, replaces old create session test.
    void testCreateNewSession() throws Exception {
        // Create mock user and obtain id
        User testUser = new User("test_user", "test@nowhere.com", "someHash");
        testUser = userRepository.save(testUser);

        // Mock client for testing purposes
        HttpClient client = HttpClient.newHttpClient();

        // Make request with JSON corresponding to Session member variables
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/game"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"mode\": 0, \"playerId\": " + testUser.getId() + ", \"gameId\": 0}"
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check that our request was acknowledged
        assertEquals(201, response.statusCode());

        // Close client resources
        client.close();
    }

    /* These tests are all broken due to changes that used SessionRepository.
    @Test
    // Creates one session with one user and checks that code and username were saved
    void testCreateNewSession() throws Exception {
        // Mock client for testing purposes
        HttpClient client = HttpClient.newHttpClient();

        // Make request with JSON corresponding to Session member variables
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/join"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"testUser\", \"gameCode\":\"mySessionCode\"}"
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check that our request was acknowledged
        assertEquals(200, response.statusCode());
        // Check that username/session code were persisted
        Session testSession = sessionRepository.findBySessionCode("mySessionCode").orElseThrow();
        assertEquals("mySessionCode", testSession.getSessionCode());
        assertTrue(testSession.getUsers().contains("testUser"));
    }

    @Test
    void testCreateGuestSessionPersistsGuestUserAndSession() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/session/guest"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"guestUser\", \"gameCode\":\"guest-room\"}"
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(userRepository.findByUsername("guestUser").isPresent());

        Session testSession = sessionRepository.findBySessionCode("guest-room").orElseThrow();
        assertTrue(testSession.getUsers().contains("guestUser"));
    }

    @Test
    void testLoginSessionPersistsJoinedUser() throws Exception {
        User user = userRepository.save(new User("registeredUser", "registered@example.com", "hash123"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/session/login"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"email\":\"registered@example.com\", \"passwordHash\":\"hash123\", \"gameCode\":\"login-room\"}"
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Session testSession = sessionRepository.findBySessionCode("login-room").orElseThrow();
        assertTrue(testSession.getUsers().contains(user.getUsername()));
    }
    */
}
