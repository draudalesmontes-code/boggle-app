package com.example.Boggle;


import com.example.Boggle.Model.Tables.*;
import com.example.Boggle.Security.PasswordUtil;
import com.example.Boggle.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;


/**
 * Integration tests for user-related API endpoints.
 *
 * These tests start the Spring Boot application on a random port
 * and verify registration, guest creation, login, and duplicate-user handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationAPITests {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    int port;

    /**
     * Clears user data before each test to ensure
     * a clean database state.
     */
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @AfterEach
    /**
     * Clears user data after each test to prevent
     * interference between test cases.
     */
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * Verifies that the register endpoint creates a new user
     * and returns HTTP 201.
     */
    @Test
    void testRegisterCreateUser() throws Exception{
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create("http://localhost:" + port + "/api/users/register"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                                {
                                 "username": "diego9",
                                 "email": "diego@test.com",
                                 "password": "Secret123"
                                }
                              """
                ))
                .header("Content-Type","application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(userRepository.findByUsername("diego9").isPresent());

        client.close();
    }

    /**
     * Verifies that the guest endpoint creates a guest user
     * and returns HTTP 201.
     */
    @Test
    void registerCreateGuest() throws Exception{
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create("http://localhost:" + port + "/api/users/guest"))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type","application/json")
                .build();

        HttpResponse response = client.send(request,HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, userRepository.count());

        client.close();

    }

    /**
     * Verifies that the login endpoint authenticates an existing user
     * with valid credentials and returns HTTP 200.
     */
    @Test
    void registerCreateUser() throws Exception{
        String storedHash = PasswordUtil.hash("Secret123");

        User user = new User("diego9", "diego@test.com", storedHash);
        user.setGuest(false);
        userRepository.save(user);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create("http://localhost:" + port + "/api/users/login"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                        {
                          "username": "diego9",
                          "password": "Secret123"
                        }
                        """
                ))
                .header("Content-Type","application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("status = " + response.statusCode());
        System.out.println("body = " + response.body());

        assertEquals(200, response.statusCode());

        client.close();
    }

    /**
     * Verifies that registration with a username already present
     * in the database returns HTTP 409.
     */
    @Test
    void testRegisterUserRepeatedUsername() throws Exception{
        User user = new User("diego9", "diego@test.com", "someHash");
        user.setGuest(false);
        userRepository.save(user);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create("http://localhost:" + port + "/api/users/register"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                        {
                          "username": "diego9",
                          "email": "new@test.com",
                          "password": "Secret123"
                        }
                        """
                ))
                .header("Content-Type","application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("status = " + response.statusCode());
        System.out.println("body = " + response.body());

        assertEquals(409, response.statusCode());
        assertEquals(1, userRepository.count());

        client.close();

    }
}
