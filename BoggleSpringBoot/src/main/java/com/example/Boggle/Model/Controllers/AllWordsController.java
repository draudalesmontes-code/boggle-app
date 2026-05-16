package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Service.FindWordsService;
import com.example.Boggle.Service.GameService;
import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving all valid words that can be formed on a game's board.
 */
@RestController
@RequestMapping("/api/game")
public class AllWordsController {

    private GameService gameService;
    private FindWordsService findWordsService;

    /**
     * Creates a controller with the services required to look up the board and find words.
     *
     * @param gameService service used to retrieve the board for a given game
     * @param findWordsService service used to find all valid words on a board
     */
    public AllWordsController(GameService gameService, FindWordsService findWordsService){
        this.gameService = gameService;
        this.findWordsService = findWordsService;
    }

    /**
     * Returns all valid words that can be formed from the board of the specified game.
     *
     * @param gameId the ID of the game
     * @return a list of {@link WordCandidate} objects representing all valid words on the board
     * @throws org.springframework.web.server.ResponseStatusException if the game does not exist
     */
    @GetMapping("{gameId}/board/words")
    public ResponseEntity<List<WordCandidate>> getAllBoardWords(@PathVariable Integer gameId ){
        Board board =  gameService.getBoard(gameId);
        String boardString = board.getBoardString();
        List<WordCandidate> words = findWordsService.findValidWords(boardString);
        return ResponseEntity.ok(words);
    }


}
