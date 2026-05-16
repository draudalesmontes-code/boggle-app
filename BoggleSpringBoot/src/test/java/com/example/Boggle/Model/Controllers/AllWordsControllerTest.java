package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Service.FindWordsService;
import com.example.Boggle.Service.GameService;
import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AllWordsController}.
 *
 * <p>Tests verify that the controller correctly delegates to
 * {@link GameService} and {@link FindWordsService} and wraps the result
 * in the appropriate HTTP response.
 */
public class AllWordsControllerTest {

    private GameService gameService;
    private FindWordsService findWordsService;
    private AllWordsController allWordsController;

    /**
     * Initializes class needed for testing before each test.
     */
    @BeforeEach
    public void setup(){
         gameService = Mockito.mock(GameService.class);
         findWordsService = Mockito.mock((FindWordsService.class));
         allWordsController = new AllWordsController(gameService,findWordsService);
    }

    @Test
    void testGetAllBoardWords(){
        Board board = new Board();
        board.setBoardString("CATS\nXXXX\nXXXX\nXXXX");
        when(gameService.getBoard(1)).thenReturn(board);
        when(findWordsService.findValidWords("CATS\nXXXX\nXXXX\nXXXX"))
                .thenReturn(List.of(new WordCandidate(("CAT"),2)));

        ResponseEntity<List<WordCandidate>> response = allWordsController.getAllBoardWords(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("CAT", response.getBody().get(0).getWord());
    }

    @Test
    void testGetALLBoardWords_noWordsFound() {
        Board board = new Board();
        board.setBoardString("ZZZZ\nZZZZ\nZZZZ\nZZZZ");
        when(gameService.getBoard(2)).thenReturn(board);
        when(findWordsService.findValidWords("ZZZZ\nZZZZ\nZZZZ\nZZZZ"))
                .thenReturn(List.of());
        ResponseEntity<List<WordCandidate>> response = allWordsController.getAllBoardWords(2);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }




}
