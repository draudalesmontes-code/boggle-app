package com.example.Boggle.Model.Controllers;
import com.example.Boggle.Model.Tables.Dictionary;
import com.example.Boggle.repository.DictionaryRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for retrieving dictionary words.
 *
 * This version uses a DTO to safely expose only the fields
 * needed by the frontend, avoiding lazy-loading or internal fields.
 */
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    private final DictionaryRepository dictionaryRepository;

    public DictionaryController(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * DTO for dictionary entries to be sent to the frontend.
     */
    public static class DictionaryDTO {
        private String word;
        private int pointValue;

        public DictionaryDTO(String word, int pointValue) {
            this.word = word;
            this.pointValue = pointValue;
        }
        public String getWord() {
            return word;
        }
        public int getPointValue() {
            return pointValue;
        }

    }

    /**
     * GET /api/dictionary/all
     *
     * Returns a list of all dictionary words as JSON.
     */
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<DictionaryDTO> getAllWords() {
        return dictionaryRepository.findAll()
                .stream()
                .map(d -> new DictionaryDTO(d.getWord(), d.getPointValue()))
                .toList(); // Cleaner syntax for modern Java
    }
}