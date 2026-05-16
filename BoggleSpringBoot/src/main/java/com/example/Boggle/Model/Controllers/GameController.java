package com.example.Boggle.Model.Controllers;
import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Service.GameScoreService;
import com.example.Boggle.Service.GameService;
import com.example.Boggle.Service.WordSubmissionService;
import com.example.Boggle.Model.Tables.GameStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

/**
 * REST controller for creating, joining, and retrieving Boggle games.
 *
 * <p>This controller supports solo and multiplayer game creation,
 * allows a second player to join a waiting multiplayer game, and exposes
 * endpoints for retrieving game and board information.
 */
@RestController
@RequestMapping("/api")
public class GameController{

    private final GameService gameService;
    private final GameScoreService gameScoreService;
    private final WordSubmissionService wordSubmissionService;

    /**
     * Creates a controller with the repositories required to manage games
     * and boards.
     *
     * Related classes to check out {@link GameService} {@link GameScoreService} {@link WordSubmissionService}
     *
     * @param gameService Service that handles game logic
     * @param gameScoreService Service that handles score tracking
     * @param wordSubmissionService Service that handles word submission validation
     */
    public GameController(GameService gameService,
                          GameScoreService gameScoreService,
                          WordSubmissionService wordSubmissionService){
        this.gameService = gameService;
        this.gameScoreService = gameScoreService;
        this.wordSubmissionService = wordSubmissionService;
    }

    /**
     * Supported game modes for game creation.
     */
    public enum GameMode {SOLO, MULTIPLAYER}

    /**
     * Request body for creating a new game.
     */
    public static class CreateGameRequest{
        /**
         * The requested game mode.
         */
        public GameMode mode;

        /**
         * The ID of the player creating the game.
         */
        public Integer playerId;
    }
    /**
     * Request body for joining an existing multiplayer game.
     */
    public static class JoinGameRequest{
        /**
         * The ID of the player joining the game.
         */
        public Integer playerId;
    }

    /**
     * Request body for submitting a word during an active game.
     */
    public static class SubmitWordRequest {
        public Integer playerId;
        public String word;
    }

    /**
     * Response body summarizing a game.
     */
    public static class GameResponse{
        /**
         * The game ID.
         */
        public Integer gameId;

        /**
         * The ID of player one.
         */
        public Integer player1Id;

        /**
         * The ID of player two, or {@code null} if no second player has joined.
         */
        public Integer player2Id;

        /**
         * The public board identifier associated with the game.
         */
        public String boardId;

        /**
         * The current game status.
         */
        public String status;

        /**
         * The time the game record was created.
         */
        public LocalDateTime createdAt;

        /**
         * The time gameplay began, or {@code null} if the game has not started.
         */
        public LocalDateTime startedAt;

        /**
         * The time gameplay ended, or {@code null} if the game is not finished.
         */
        public LocalDateTime finishedAt;

        /**
         * Total round duration in seconds
         * Frontend clients can use this for timer setup or reset logic
         */
        public Long durationSeconds;

        /**
         * Number of seconds remaining in the current round
         * This value is capped at 0 once time has expired
         */
        public Long remainingSeconds;

        /** Username of player one. */
        public String player1Username;

        /** Username of player two, or {@code null} if no second player has joined. */
        public String player2Username;

        /**
         * Builds a response DTO from a game entity.
         *
         * @param currentGame the game entity to summarize
         * @return a response containing key game details
         */
        public static GameResponse GameSummaryDTO(Game currentGame, GameService gameService){
            if (currentGame == null) {
                throw new ResponseStatusException(NOT_FOUND, "Game id not found");
            }

            GameResponse gameSummary = new GameResponse();
            gameSummary.gameId = currentGame.getId();
            gameSummary.player1Id = currentGame.getPlayer1().getId();
            gameSummary.player2Id = (currentGame.getPlayer2() == null) ? null : currentGame.getPlayer2().getId();
            gameSummary.player1Username = currentGame.getPlayer1().getUsername();
            gameSummary.player2Username = (currentGame.getPlayer2() == null) ? null : currentGame.getPlayer2().getUsername();
            gameSummary.boardId = (currentGame.getBoard() == null) ? null : currentGame.getBoard().getBoardId();
            gameSummary.status = (currentGame.getStatus() == null) ? null : currentGame.getStatus().name();
            gameSummary.createdAt = currentGame.getCreatedAt();
            gameSummary.startedAt = currentGame.getStartedAt();
            gameSummary.finishedAt = currentGame.getFinishedAt();
            gameSummary.durationSeconds = gameService.getGameDurationSeconds();
            gameSummary.remainingSeconds = gameService.getRemainingSeconds(currentGame);
            return gameSummary;
        }
    }

    /**
     * Used to return a list of boards that are currently waiting for a second player
     */
    public static class ListWaitingResponse{
        public List<Integer> gameIds;

        public static ListWaitingResponse ListWaitingResponseDTO(ArrayList<Integer> boardIds) {
            ListWaitingResponse lwr = new ListWaitingResponse();
            lwr.gameIds = boardIds;
            return lwr;
        }
    }

    /**
     * Response body containing board information.
     */
    public static class BoardResponse{

        public String boardId;

        /**
         * The flattened board string representation.
         */
        public String boardString;

        /**
         * Builds a response DTO from a board entity.
         *
         * @param currentBoard the board entity
         * @return a response containing board details
         */
        public static BoardResponse BoardDTO(Board currentBoard){
            BoardResponse boardOut = new BoardResponse();
            boardOut.boardId = currentBoard.getBoardId();
            boardOut.boardString = currentBoard.getBoardString();
            return boardOut;
        }
    }

    /**
     * Response body describing the outcome of a word submission.
     */
    public static class SubmitWordResponse{
        public boolean accepted;

        /**
         * The reason code describing the submission result.
         */
        public String reason;

        /**
         * The submitted word after normalization.
         */
        public String normalizedWord;

        /**
         * The number of points awarded for the submitted word, if accepted.
         */
        public Integer points;

        /**
         * Builds a response DTO from a word submission result.
         *
         * @param result the service result describing the submission outcome
         * @return a response containing acceptance status, reason, normalized word,
         *         and awarded points
         */
        public static SubmitWordResponse SubmitWordDTO(WordSubmissionService.Result result){
            SubmitWordResponse submitWordResult = new SubmitWordResponse();
            submitWordResult.accepted = result.accepted;
            submitWordResult.reason = result.reason.name();
            submitWordResult.normalizedWord = result.normalizedWord;
            submitWordResult.points = result.points;
            return submitWordResult;
        }
    }

    /**
     * Creates a new game in solo or multiplayer mode.
     *
     * <p>Solo games begin immediately. Multiplayer games are created
     * in a waiting state until a second player joins.
     *
     * @param request the game creation request
     * @return a summary of the created game
     * @throws ResponseStatusException if the request is invalid or the player does not exist
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/game")
    public GameResponse createGame(@RequestBody CreateGameRequest request){
        if(request == null){
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is required");
        }
        if(request.playerId == null){
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "playerId is required");
        }
        if(request.mode == null){
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mode is required");
        }

        Game game = gameService.createGame(request.mode,request.playerId);
        return GameResponse.GameSummaryDTO(game, gameService);
    }

    /**
     * Joins an existing multiplayer game as the second player.
     *
     * @param gameId the ID of the game to join
     * @param request the join request containing the joining player's ID
     * @return a summary of the updated game
     * @throws ResponseStatusException if the game does not exist, is not
     *         joinable, is already full, or the player is invalid
     */
    @PostMapping("/game/{gameId}/join")
    public GameResponse joinGame(@PathVariable Integer gameId, @RequestBody JoinGameRequest request){
        if (request == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (request.playerId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "playerId is required");
        }

        Game game = gameService.joinGame(gameId, request.playerId);
        return GameResponse.GameSummaryDTO(game, gameService);
    }

    /**
     * Used to return a list of boards that are currently waiting for a second player
     *
     * @return a list of IDs for all games that are currently in 'waiting' status
     */
    @GetMapping("/game/list-waiting")
    public List<GameResponse> listWaitingGames() {
        List<Game> waitingGames = gameService.getWaitingGames();
        List<GameResponse> responses = new ArrayList<>();
        for (Game game : waitingGames) {
            responses.add(GameResponse.GameSummaryDTO(game, gameService));
        }
        return responses;
    }

    /**
     * Retrieves a summary of a game by ID.
     *
     * @param gameId the game ID
     * @return a summary of the requested game
     * @throws ResponseStatusException if the game does not exist
     */
    @GetMapping("/game/{gameId}")
    public GameResponse getGame(@PathVariable Integer gameId){
        Game game = gameService.getGame(gameId);
        if (game == null) {
            throw new ResponseStatusException(NOT_FOUND, "Game id not found");
        }
        return GameResponse.GameSummaryDTO(game, gameService);
    }

    /**
     * Retrieves a summary of the board related to the gameID passed.
     *
     * @param gameId the game ID
     * @return the board information for the specified game
     * @throws ResponseStatusException if the game does not exist.
     */
    @GetMapping("/game/{gameId}/board")
    public BoardResponse getBoard(@PathVariable Integer gameId){
        return BoardResponse.BoardDTO(gameService.getBoard(gameId));
    }

    /**
     * Validates and records a submitted word for a player in a game.
     *
     * <p>The request must include both a player ID and a word. The word is
     * validated by the word submission service and the result is returned to
     * the client.
     *
     * @param gameId the ID of the game receiving the submission
     * @param request the submission request containing the player ID and word
     * @return the result of the word submission attempt
     * @throws ResponseStatusException if the request body, player ID, or word is missing
     */
    @PostMapping("/game/{gameId}/submit-word")
    public SubmitWordResponse submitWord(@PathVariable Integer gameId,
                                         @RequestBody SubmitWordRequest request){
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Body is required");
        }
        if (request.playerId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "playerId is required");
        }
        if (request.word == null) {
            throw new ResponseStatusException(BAD_REQUEST, "word is required");
        }

        Game game = gameService.getGame(gameId);

        if (game.getStatus() != GameStatus.IN_PROGRESS || gameService.isGameExpired(game)) {
            SubmitWordResponse response = new SubmitWordResponse();
            response.accepted = false;
            response.reason = "GAME_NOT_IN_PROGRESS";
            response.normalizedWord = request.word;
            response.points = null;
            return response;
        }

        WordSubmissionService.Result result =
                wordSubmissionService.submitWord(gameId, request.playerId, request.word);

        return SubmitWordResponse.SubmitWordDTO(result);
    }

    /**
     * Retrieves the current score totals for a game.
     *
     * @param gameId the ID of the game
     * @return the current point totals and leading player information
     */
    @GetMapping("/game/{gameId}/score")
    public GameScoreService.Totals getScore(@PathVariable Integer gameId){
        return gameScoreService.computeTotals(gameId);
    }

    /**
     * Finalizes a game and returns the final score totals.
     *
     * <p>This marks the game as finished, records the finish time, and stores
     * the winner when one exists.
     *
     * @param gameId the ID of the game to finish
     * @return the final score totals after the game is completed
     */
    @PostMapping("/game/{gameId}/finish")
    public GameScoreService.Totals finishGame(@PathVariable Integer gameId){
        return gameScoreService.finishGame(gameId);
    }
}
