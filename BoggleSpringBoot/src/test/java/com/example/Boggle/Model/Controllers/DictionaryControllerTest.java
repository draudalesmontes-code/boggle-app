package com.example.Boggle.Model.Controllers;
import com.example.Boggle.Model.Tables.Dictionary;
import com.example.Boggle.repository.DictionaryRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Integration tests for dictionary API endpoints.
 *
 * These tests verify that dictionary data can be retrieved
 * from the database via the API.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DictionaryControllerTest {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @LocalServerPort
    int port;

    /**
     * Tests that GET /api/dictionary/all returns data from the database.
     */
    @Test
    void testGetAllWords() throws Exception {
        Dictionary d = new Dictionary();
        d.setWord("cat");
        d.setPointValue(1);
        dictionaryRepository.save(d);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/dictionary/all"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verify that the request succeeded
        assertEquals(200, response.statusCode());

        // Check that the response contains at least one known word
        // (replace "cat" with a word that exists in your DB)
        assertTrue(response.body().contains("cat"));

        client.close();
    }
}