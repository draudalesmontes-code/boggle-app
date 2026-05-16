package com.example.Boggle.Service;

import com.example.Boggle.Model.Controllers.GameController;
import com.example.Boggle.Model.Tables.Dictionary;
import com.example.Boggle.Model.Tables.FoundWord;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.repository.*;
import com.example.Boggle.Model.Tables.*;
import com.example.Boggle.util.ValidateAnswers;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.example.Boggle.util.unflatten;

/**
 *Service responsible for validating word submission during game.
 *
 *  * Is called by {@link GameController} class;
 *
 * <p> Provides the Following Services:
 *
 * 1. Checks if the Game and Player Exist (avoids allowing word submission with no game or player attached)
 * 2. Checks if Player belongs to the game to which the submission is taking place.
 * 3. Checks if the word submitted exist in the dictionary.
 * 4. Checks if the word is long enough
 * 5. Checks if the word is valid in the board (side-by-side or diagonal)
 * 6. Checks if the word has not been submitted (avoid duplicates)
 *
 */
@Service
public class WordSubmissionService{



    public enum SubmissionReason{
        OK,
        GAME_NOT_FOUND,
        PLAYER_NOT_FOUND,
        PLAYER_NOT_IN_GAME,
        GAME_NOT_IN_PROGRESS,
        EMPTY_WORD,
        TOO_SHORT,
        NOT_IN_DICTIONARY,
        NOT_ON_BOARD,
        DUPLICATE
    }

    /**
     * Result of a word submission attempt.
     *
     * <p>This object reports whether the submission was accepted, the reason
     * for the outcome, the normalized uppercase word, and the awarded points
     * when applicable.
     */
    @Service
    public static class Result{
        /**
         * Whether the submitted word was accepted.
         */
        public boolean accepted;

        /**
         * Status code describing the submission outcome.
         */
        public SubmissionReason reason;
        /**
         * The submitted word after trimming and uppercasing.
         */
        public String normalizedWord;

        /**
         * The number of points awarded for an accepted word.
         */
        public Integer points;
    }

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;
    private final FoundWordRepository foundWordRepository;

    /**
     * Constructs a WordSubmissionService with the repositories required
     * to validate submitted words and store accepted results.
     *
     * @param gameRepository Repository that stores game information.
     * @param userRepository Repository storing the user and guest.
     * @param dictionaryRepository Repository that stores words and their point scoring
     * @param foundWordRepository Repository that holds words found by player during game.
     */
    public WordSubmissionService(
            GameRepository gameRepository,
            UserRepository userRepository,
            DictionaryRepository dictionaryRepository,
            FoundWordRepository foundWordRepository
    ){
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.foundWordRepository = foundWordRepository;
    }

    /**
     * Validates and records a player's submitted word for a game.
     *
     * <p>The submitted word is trimmed and converted to uppercase before
     * validation. This method checks that the game exists, the player exists,
     * the player belongs to the game, the word exists in the dictionary, the
     * word can be formed on the board, the word has not already been submitted
     * by that player in that game, and the word length meets the minimum
     * requirement.
     *
     * @param gameId the ID of the game receiving the submission
     * @param playerId the ID of the player submitting the word
     * @param rawWord the raw submitted word
     * @return a {@link Result} describing whether the submission was accepted
     *         and why
     */
    @Transactional
    public Result submitWord(Integer gameId, Integer playerId, String rawWord){
        Result out = new Result();
        String word = (rawWord == null)? "": rawWord.trim().toUpperCase();
        out.normalizedWord = word;

        Game game = gameRepository.findById(gameId).orElse(null);
        //makes sure that a game exist before checking a word is possible
        if(game==null){
            out.accepted = false;
            out.reason = SubmissionReason.GAME_NOT_FOUND;
            return out;
        }

        User player = userRepository.findById(playerId).orElse(null);
        //checks if user associated with submission is real or exist.
        if(player == null){
            out.accepted = false;
            out.reason = SubmissionReason.PLAYER_NOT_FOUND;
            return out;
        }

        //checking if player belongs in game
        Integer p1Id = game.getPlayer1().getId();
        Integer p2Id = (game.getPlayer2() == null)? null: game.getPlayer2().getId();

        boolean isP1 = playerId.equals(p1Id);
        boolean isP2 = (p2Id != null) && playerId.equals(p2Id);

        if(!isP1 && !isP2){
            out.accepted = false;
            out.reason = SubmissionReason.PLAYER_NOT_IN_GAME;
            return out;
        }

        //checks that any submission is being done while game is running
        if(game.getStatus()!= GameStatus.IN_PROGRESS){
            out.accepted = false;
            out.reason =  SubmissionReason.GAME_NOT_IN_PROGRESS;
            return out;
        }

        //checks that blank word not submitted
        if(word.isBlank()){
            out.accepted = false;
            out.reason = SubmissionReason.EMPTY_WORD;
            return out;
        }

        //check a word must be greater than or equal to 3
        if(word.length()<3){
            out.reason = SubmissionReason.TOO_SHORT;
            out.accepted = false;
            return out;
        }

        Dictionary dict = dictionaryRepository.findByWordIgnoreCase(word).orElse(null);
        //checks that word exist in the dictionary
        if(dict==null){
            out.accepted = false;
            out.reason = SubmissionReason.NOT_IN_DICTIONARY;
            return out;
        }

        //Check valid board
        String [][] grid = unflatten.parseStringTo4x4(game.getBoard().getBoardString());
        if(!ValidateAnswers.isValidOnBoard(word,grid)){
            out.accepted = false;
            out.reason = SubmissionReason.NOT_ON_BOARD;
            return out;
        }

        //duplicate check
        if(foundWordRepository.existsByGame_IdAndPlayer_IdAndDictionaryWord_Id(gameId,playerId,dict.getId())){
            out.accepted = false;
            out.reason = SubmissionReason.DUPLICATE;
            return out;
        }

        FoundWord wordPassed = new FoundWord();
        wordPassed.setGame(game);   // links word submitted to game it was submitted in (found_words.game_id = game.id)
        wordPassed.setDictionaryWord(dict); //sets what dictionary word corresponds to.
        wordPassed.setPlayer(userRepository.getReferenceById(playerId));

        //avoid double submission entry error
        try {
            foundWordRepository.save(wordPassed);
        } catch (DataIntegrityViolationException e) {
            // handles race condition duplicates
            out.accepted = false;
            out.reason = SubmissionReason.DUPLICATE;
            return out;
        }
        out.accepted = true;
        out.reason = SubmissionReason.OK;
        out.points = dict.getPointValue();
        return out;
    }


}