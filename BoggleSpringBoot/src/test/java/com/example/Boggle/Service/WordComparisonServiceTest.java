package com.example.Boggle.Service;

import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Dictionary;
import com.example.Boggle.Model.Tables.FoundWord;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import com.example.Boggle.repository.FoundWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WordComparisonService}.
 *
 * Verifies that board words are correctly split into found and missed lists
 * based on what a player submitted during a game.
 */
@ExtendWith(MockitoExtension.class)
public class WordComparisonServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private FindWordsService findWordsService;

    @Mock
    private FoundWordRepository foundWordRepository;

    @InjectMocks
    private WordComparisonService wordComparisonService;

    private Board board;
    private User player;
    private Game game;

    @BeforeEach
    void setup() {
        board = new Board();
        board.setBoardId("board-1");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        player = new User("alice", "alice@test.com", "password123");
        player.setId(1);

        game = new Game(player, null, board);
    }

    private FoundWord makeFoundWord(String word, int points) {
        Dictionary dict = new Dictionary();
        dict.setWord(word);
        dict.setPointValue(points);

        FoundWord fw = new FoundWord();
        fw.setPlayer(player);
        fw.setGame(game);
        fw.setDictionaryWord(dict);
        return fw;
    }

    /**
     * Words the player found should appear in foundWords,
     * words they did not find should appear in missedWords.
     */
    @Test
    void compareCorrectlySplitsFoundAndMissed() {
        when(gameService.getBoard(1)).thenReturn(board);
        when(findWordsService.findValidWords(board.getBoardString())).thenReturn(List.of(
                new WordCandidate("CAT", 2),
                new WordCandidate("BAT", 2),
                new WordCandidate("ARC", 2)
        ));
        when(foundWordRepository.findByGame_IdAndPlayer_IdOrderByFoundAtDesc(1, 1))
                .thenReturn(List.of(makeFoundWord("CAT", 2)));

        WordComparisonService.ComparisonResult result = wordComparisonService.compare(1, 1);

        assertEquals(1, result.foundWords().size());
        assertEquals("CAT", result.foundWords().get(0).getWord());

        assertEquals(2, result.missedWords().size());
        assertTrue(result.missedWords().stream().anyMatch(w -> w.getWord().equals("BAT")));
        assertTrue(result.missedWords().stream().anyMatch(w -> w.getWord().equals("ARC")));
    }

    /**
     * When the player found no words, all board words should be in missedWords.
     */
    @Test
    void compareAllMissedWhenNoWordsFound() {
        when(gameService.getBoard(1)).thenReturn(board);
        when(findWordsService.findValidWords(board.getBoardString())).thenReturn(List.of(
                new WordCandidate("CAT", 2),
                new WordCandidate("BAT", 2)
        ));
        when(foundWordRepository.findByGame_IdAndPlayer_IdOrderByFoundAtDesc(1, 1))
                .thenReturn(List.of());

        WordComparisonService.ComparisonResult result = wordComparisonService.compare(1, 1);

        assertTrue(result.foundWords().isEmpty());
        assertEquals(2, result.missedWords().size());
    }

    /**
     * When the player found all words, missedWords should be empty.
     */
    @Test
    void compareAllFoundWhenEveryWordSubmitted() {
        when(gameService.getBoard(1)).thenReturn(board);
        when(findWordsService.findValidWords(board.getBoardString())).thenReturn(List.of(
                new WordCandidate("CAT", 2),
                new WordCandidate("BAT", 2)
        ));
        when(foundWordRepository.findByGame_IdAndPlayer_IdOrderByFoundAtDesc(1, 1))
                .thenReturn(List.of(
                        makeFoundWord("CAT", 2),
                        makeFoundWord("BAT", 2)
                ));

        WordComparisonService.ComparisonResult result = wordComparisonService.compare(1, 1);

        assertEquals(2, result.foundWords().size());
        assertTrue(result.missedWords().isEmpty());
    }

    /**
     * Comparison should be case-insensitive — a found word stored in lowercase
     * should still match a board word stored in uppercase.
     */
    @Test
    void compareIsCaseInsensitive() {
        when(gameService.getBoard(1)).thenReturn(board);
        when(findWordsService.findValidWords(board.getBoardString())).thenReturn(List.of(
                new WordCandidate("CAT", 2)
        ));
        when(foundWordRepository.findByGame_IdAndPlayer_IdOrderByFoundAtDesc(1, 1))
                .thenReturn(List.of(makeFoundWord("cat", 2)));

        WordComparisonService.ComparisonResult result = wordComparisonService.compare(1, 1);

        assertEquals(1, result.foundWords().size());
        assertTrue(result.missedWords().isEmpty());
    }
}
