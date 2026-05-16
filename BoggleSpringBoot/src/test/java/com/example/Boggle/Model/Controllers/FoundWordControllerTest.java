package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.*;
import com.example.Boggle.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FoundWordControllerTest {

    @Autowired
    private FoundWordRepository foundWordRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private BoardRepository boardRepository;

    @LocalServerPort
    int port;

    @Test
    void testGetPlayerFoundWords() throws Exception {
        // 1. Setup User - Entity requires username, email, and password
        User player = new User("alice", "alice@test.com", "password123");
        player = userRepository.save(player);

        // 2. Setup Board
        Board board = new Board();
        board.setBoardId("board-1"); // Matches your Board entity fields
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");
        board = boardRepository.save(board);

        // 3. Setup Game - Using the constructor (player1, player2, board)
        Game game = new Game(player, null, board);
        game.setStatus(GameStatus.IN_PROGRESS);
        game = gameRepository.save(game);

        // 4. Setup Dictionary Word
        Dictionary word = new Dictionary();
        word.setWord("REACT");
        word.setPointValue(2);
        word = dictionaryRepository.save(word);

        // 5. Link them in FoundWord
        FoundWord foundWord = new FoundWord();
        foundWord.setPlayer(player);
        foundWord.setGame(game);
        foundWord.setDictionaryWord(word);
        foundWordRepository.save(foundWord);

        // 6. Test the API via HttpClient
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("http://localhost:%d/api/game/%d/player/%d/words",
                port, game.getId(), player.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("REACT"));
        assertTrue(response.body().contains("\"points\":2"));

        client.close();
    }

}