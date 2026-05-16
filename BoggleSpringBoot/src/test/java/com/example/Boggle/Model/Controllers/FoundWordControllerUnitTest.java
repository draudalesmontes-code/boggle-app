package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import com.example.Boggle.Service.WordComparisonService;
import com.example.Boggle.repository.FoundWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FoundWordController}.
 *
 * <p>Dependencies are mocked so each test focuses only on controller logic.
 */
@ExtendWith(MockitoExtension.class)
public class FoundWordControllerUnitTest {

    @Mock
    private FoundWordRepository foundWordRepository;

    @Mock
    private WordComparisonService wordComparisonService;

    @InjectMocks
    private FoundWordController foundWordController;

    private WordComparisonService.ComparisonResult mockResult;

    /**
     * Sets up a sample ComparisonResult used across tests.
     */
    @BeforeEach
    void setup() {
        List<WordCandidate> found  = List.of(new WordCandidate("CAT", 2));
        List<WordCandidate> missed = List.of(new WordCandidate("BAT", 2), new WordCandidate("ARC", 2));
        mockResult = new WordComparisonService.ComparisonResult(found, missed);
    }

    /**
     * Verifies that getWordComparison returns 200 and delegates to WordComparisonService.
     */
    @Test
    void getWordComparisonReturns200WithResult() {
        when(wordComparisonService.compare(1, 2)).thenReturn(mockResult);

        ResponseEntity<WordComparisonService.ComparisonResult> response =
                foundWordController.getWordComparison(1, 2);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(wordComparisonService).compare(1, 2);
    }

    /**
     * Verifies that foundWords in the response matches what the service returned.
     */
    @Test
    void getWordComparisonReturnsCorrectFoundWords() {
        when(wordComparisonService.compare(1, 2)).thenReturn(mockResult);

        ResponseEntity<WordComparisonService.ComparisonResult> response =
                foundWordController.getWordComparison(1, 2);

        List<WordCandidate> foundWords = response.getBody().foundWords();
        assertEquals(1, foundWords.size());
        assertEquals("CAT", foundWords.get(0).getWord());
    }

    /**
     * Verifies that missedWords in the response matches what the service returned.
     */
    @Test
    void getWordComparisonReturnsCorrectMissedWords() {
        when(wordComparisonService.compare(1, 2)).thenReturn(mockResult);

        ResponseEntity<WordComparisonService.ComparisonResult> response =
                foundWordController.getWordComparison(1, 2);

        List<WordCandidate> missedWords = response.getBody().missedWords();
        assertEquals(2, missedWords.size());
        assertTrue(missedWords.stream().anyMatch(w -> w.getWord().equals("BAT")));
        assertTrue(missedWords.stream().anyMatch(w -> w.getWord().equals("ARC")));
    }
}
