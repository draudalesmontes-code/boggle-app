package com.example.Boggle.Service;

import com.example.Boggle.Model.Controllers.GameController;
import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.GameStatus;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.repository.BoardRepository;
import com.example.Boggle.repository.GameRepository;
import com.example.Boggle.repository.UserRepository;
import com.example.Boggle.util.ShuffleUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service responsible for creating, joining, and retrieving Boggle games
 * and their associated boards.
 *
 * <p>This service handles solo and multiplayer game setup, validates
 * player existence, creates randomized boards, and manages state changes
 * when players join multiplayer games.
 */
@Service
public class GameService {

    private static final long GAME_DURATION_SECONDS = 180;

    private final GameRepository gameRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a GameService with the repositories required to manage games,
     * boards, and users.
     *
     * @param gameRepository repository storing game records
     * @param boardRepository repository storing generated boards
     * @param userRepository repository storing players
     */
    public GameService(GameRepository gameRepository,
                       BoardRepository boardRepository,
                       UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new game for the given player and mode.
     *
     * <p>Solo games begin immediately. Multiplayer games are created
     * in a waiting state until a second player joins.
     *
     * @param mode the requested game mode
     * @param playerId the ID of the player creating the game
     * @return the saved game entity
     * @throws ResponseStatusException if the mode is missing or the player does not exist
     */
    public Game createGame(GameController.GameMode mode, Integer playerId) {
        if (mode == null) {
            throw new ResponseStatusException(BAD_REQUEST, "mode is required");
        }

        User p1 = requireUser(playerId, "playerId");
        Game game;

        switch (mode) {
            case SOLO -> {
                game = new Game(p1, null, createAndSaveBoard());
                game.setStatus(GameStatus.IN_PROGRESS);
                game.setStartedAt(LocalDateTime.now());
            }
            case MULTIPLAYER -> {
                game = new Game(p1, null, createAndSaveBoard());
                game.setStatus(GameStatus.WAITING);
                game.setStartedAt(null);
            }
            default -> throw new ResponseStatusException(BAD_REQUEST, "Unknown mode");
        }

        game.setFinishedAt(null);
        return gameRepository.save(game);
    }

    /**
     * Adds a second player to a waiting multiplayer game.
     *
     * @param gameId the ID of the game to join
     * @param playerId the ID of the joining player
     * @return the updated saved game entity
     * @throws ResponseStatusException if the game does not exist, is not joinable,
     *         is already full, or the player is invalid
     */
    public Game joinGame(Integer gameId, Integer playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Game id not found"));

        User player2 = requireUser(playerId, "playerId");

        if (game.getStatus() != GameStatus.WAITING) {
            throw new ResponseStatusException(CONFLICT, "Game is not joinable");
        }

        if (game.getPlayer2() != null) {
            throw new ResponseStatusException(CONFLICT, "Game already full");
        }

        if (game.getPlayer1().getId().equals(player2.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Player cannot join own game");
        }

        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        game.setFinishedAt(null);

        return gameRepository.save(game);
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameId the ID of the requested game
     * @return the matching game entity
     * @throws ResponseStatusException if the game does not exist
     */
    public Game getGame(Integer gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Game id not found"));

        return updateGameStatusIfExpired(game);
    }


    /**
     * Retrieves the board associated with a game.
     *
     * @param gameId the ID of the game
     * @return the board linked to the game
     * @throws ResponseStatusException if the game does not exist
     */
    public Board getBoard(Integer gameId) {
        return getGame(gameId).getBoard();
    }

    /**
     * Generates a shuffled Boggle board and saves it to the database.
     *
     * @return the saved board entity
     */
    private Board createAndSaveBoard() {
        String flattened = ShuffleUtil.shuffledBoard().flattened;

        Board board = new Board();
        board.setBoardId(UUID.randomUUID().toString());
        board.setBoardString(flattened);

        return boardRepository.save(board);
    }

    /**
     * Loads a user by ID and throws an error if the user is missing.
     *
     * @param userId the ID of the required user
     * @param fieldName the request field name used in error messages
     * @return the matching user
     * @throws ResponseStatusException if the user ID is missing or not found
     */
    private User requireUser(Integer userId, String fieldName) {
        if (userId == null) {
            throw new ResponseStatusException(BAD_REQUEST, fieldName + " is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, fieldName + " not found"));
    }

    /**
     * Returns the fixed duration of a Boggle round in seconds.
     *
     * This is exposed to the controller so the frontend can display or
     * initialize the round timer consistently with backend rules.
     *
     * @return total round duration in seconds
     */
    public long getGameDurationSeconds() {
        return GAME_DURATION_SECONDS;
    }

    /**
     * Calculates how many seconds remain in the current game.
     *
     * If the game has not started yet, the full duration is returned.
     * Once the timer reaches zero, this method never returns a negative value.
     *
     * @param game the game whose remaining time should be calculated
     * @return remaining time in seconds, minimum 0
     */
    public long getRemainingSeconds(Game game) {
        if (game.getStartedAt() == null) {
            return GAME_DURATION_SECONDS;
        }

        LocalDateTime endTime = game.getStartedAt().plusSeconds(GAME_DURATION_SECONDS);
        long remaining = Duration.between(LocalDateTime.now(), endTime).getSeconds();

        return Math.max(remaining, 0);
    }

    /**
     * Determines whether a started game has run out of time.
     *
     * @param game the game to check
     * @return true if the game has started and no time remains
     */
    public boolean isGameExpired(Game game) {
        return game.getStartedAt() != null && getRemainingSeconds(game) <= 0;
    }

    /**
     * Updates an in-progress game to FINISHED when its timer has expired.
     *
     * If the game is expired and still marked IN_PROGRESS, the finished time is
     * recorded and the updated game is saved. Non-expired games are returned
     * unchanged.
     *
     * @param game the game to inspect and update if needed
     * @return the saved expired game, or the original game if no update was needed
     */
    public Game updateGameStatusIfExpired(Game game) {
        if (game.getStatus() == GameStatus.IN_PROGRESS && isGameExpired(game)) {
            game.setStatus(GameStatus.FINISHED);

            if (game.getFinishedAt() == null) {
                game.setFinishedAt(LocalDateTime.now());
            }

            game = gameRepository.save(game);
        }

        return game;
    }

    public List<Game> getWaitingGames() {
        return gameRepository.findByStatus(GameStatus.WAITING);
    }
}