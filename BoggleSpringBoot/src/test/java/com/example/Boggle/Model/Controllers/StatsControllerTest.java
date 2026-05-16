package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.*;
import com.example.Boggle.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatsControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private FoundWordRepository foundWordRepository;

    @Test
    void testGetUserStatsApi() throws Exception {
        // 1. Create a User using the Public Constructor
        User user = new User("stats_user_" + System.currentTimeMillis(), "stats@test.com", "hash");
        user = userRepository.save(user);
        Integer userId = user.getId();

        // 2. Create a Board
        Board board = new Board();
        board.setBoardId("test_board_" + userId);
        board.setBoardString("ABCDEFGHIJKLMNOP");
        boardRepository.save(board);

        // 3. Game 1: User played and WON
        // Using the public Game constructor: Game(player1, player2, board)
        Game game1 = new Game(user, null, board);
        game1.setStatus(GameStatus.FINISHED);
        game1.setWinner(user); // Passing the User object, not an ID
        gameRepository.save(game1);

        // 4. Game 2: User played but LOST
        Game game2 = new Game(user, null, board);
        game2.setStatus(GameStatus.FINISHED);
        game2.setWinner(null); // No winner or different winner
        gameRepository.save(game2);

        // 5. Add a "Found Word"
        Dictionary word = new Dictionary();
        word.setWord("test");
        word.setPointValue(1);
        dictionaryRepository.save(word);

        FoundWord fw = new FoundWord();
        fw.setPlayer(user);
        fw.setGame(game1);
        fw.setDictionaryWord(word);
        foundWordRepository.save(fw);

        // 6. Execute API Request
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/api/users/" + userId + "/stats"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 7. Verify Results
            assertEquals(200, response.statusCode());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            assertEquals(2, json.get("gamesPlayed").asInt(), "Should count both games played");
            assertEquals(1, json.get("gamesWon").asInt(), "Should count 1 win");
            assertEquals(1, json.get("wordsFound").asInt(), "Should count 1 found word");
        }
    }
}