package com.example.Boggle.Model.Controllers;
import com.example.Boggle.Model.Tables.FoundWord;
import com.example.Boggle.Service.WordComparisonService;
import com.example.Boggle.repository.FoundWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for sending found word data to the frontend.
 */
record FoundWordResponse(String word, Integer points, LocalDateTime foundAt) {}

@RestController
@RequestMapping("/api/game")
public class FoundWordController {

    @Autowired
    private FoundWordRepository foundWordRepository;

    @Autowired
    private WordComparisonService wordComparisonService;

    /**
     * GET /api/game/{gameId}/player/{playerId}/words
     * Returns the list of unique words found by a specific player in a specific game.
     */
    @GetMapping("/{gameId}/player/{playerId}/words")
    public ResponseEntity<List<FoundWordResponse>> getPlayerFoundWords(
            @PathVariable Integer gameId,
            @PathVariable Integer playerId) {

        List<FoundWord> foundWords = foundWordRepository.findByGame_IdAndPlayer_IdOrderByFoundAtDesc(gameId, playerId);
        // Map the entities to our lightweight DTO
        List<FoundWordResponse> response = foundWords.stream()
                .map(fw -> new FoundWordResponse(
                        fw.getDictionaryWord().getWord(),      // The actual string "APPLE"
                        fw.getDictionaryWord().getPointValue(), // Points for the word
                        fw.getFoundAt()                         // When it was found
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/game/{gameId}/player/{playerId}/word-comparison
     * Returns all valid words on the board split into foundWords and missedWords
     * based on what the player submitted during the game.
     */
    @GetMapping("/{gameId}/player/{playerId}/word-comparison")
    public ResponseEntity<WordComparisonService.ComparisonResult> getWordComparison(
            @PathVariable Integer gameId,
            @PathVariable Integer playerId) {
        return ResponseEntity.ok(wordComparisonService.compare(gameId, playerId));
    }
}