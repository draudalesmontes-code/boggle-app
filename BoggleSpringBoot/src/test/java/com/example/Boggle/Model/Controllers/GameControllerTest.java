package com.example.Boggle.Model.Controllers;

import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.GameStatus;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.Service.GameScoreService;
import com.example.Boggle.Service.GameService;
import com.example.Boggle.Service.WordSubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.junit.jupiter.api.AssertionsKt.assertNull;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GameController}
 *
 * These test verify game creation, multiplayer joining,
 * and board retrieval behavior using mocked repositories.
 */
@MockitoBean
public class GameControllerTest {

    private GameController gameController;
    private GameService gameService;
    private GameScoreService gameScoreService;
    private WordSubmissionService wordSubmissionService;

    /**
     * Initializes mocked repositories and creates a controller instance
     * before each test runs.
     */
    @BeforeEach
    void setup() {
        gameService = mock(GameService.class);
        gameScoreService = mock(GameScoreService.class);
        wordSubmissionService = mock(WordSubmissionService.class);

        gameController = new GameController(
                gameService,
                gameScoreService,
                wordSubmissionService
        );
    }

    /**
     * Verifies that creating a solo player game
     * return the correct game response (board, player) and in-progress status
     */
    @Test
    void TestCreateGameSolo() {
        User currentUser = new User("Diego9", "diego@test.com", "Secret123");
        currentUser.setId(2);

        Board currentBoard = new Board();
        currentBoard.setBoardId("board-123");
        currentBoard.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        Game savedGame = new Game(currentUser, null, currentBoard);
        savedGame.setId(10);
        savedGame.setStatus(GameStatus.IN_PROGRESS);

        GameController.CreateGameRequest req = new GameController.CreateGameRequest();
        req.mode = GameController.GameMode.SOLO;
        req.playerId = 2;

        when(gameService.createGame(GameController.GameMode.SOLO, 2)).thenReturn(savedGame);

        when(gameService.getGameDurationSeconds()).thenReturn(180L);
        when(gameService.getRemainingSeconds(savedGame)).thenReturn(120L);

        GameController.GameResponse response = gameController.createGame(req);

        assertNotNull(response);
        assertEquals(10, response.gameId);
        assertEquals(2, response.player1Id);
        assertNull(response.player2Id);
        assertEquals("board-123", response.boardId);
        assertEquals("IN_PROGRESS", response.status);

        assertEquals(180L, response.durationSeconds);
        assertEquals(120L, response.remainingSeconds);

        verify(gameService).createGame(GameController.GameMode.SOLO, 2);
        verifyNoInteractions(gameScoreService, wordSubmissionService);
    }

    /**
     * Verifies that creating a multiplayer game places the game
     * in waiting status until another player joins.
     */
    @Test
    void TestCreateGameMultiplayer() {
        User firstPlayer = new User("Diego9", "diego@test.com", "Secret123");
        firstPlayer.setId(2);

        Board board = new Board();
        board.setBoardId("board-Multiplayer");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        Game savedGame = new Game(firstPlayer, null, board);
        savedGame.setId(11);
        savedGame.setStatus(GameStatus.WAITING);

        GameController.CreateGameRequest req = new GameController.CreateGameRequest();
        req.mode = GameController.GameMode.MULTIPLAYER;
        req.playerId = 2;

        when(gameService.createGame(GameController.GameMode.MULTIPLAYER, 2)).thenReturn(savedGame);

        when(gameService.getGameDurationSeconds()).thenReturn(180L);
        when(gameService.getRemainingSeconds(savedGame)).thenReturn(180L);

        GameController.GameResponse response = gameController.createGame(req);

        assertNotNull(response);
        assertEquals(11, response.gameId);
        assertEquals("board-Multiplayer", response.boardId);
        assertEquals(2, response.player1Id);
        assertNull(response.player2Id);
        assertEquals("WAITING", response.status);

        assertEquals(180L, response.durationSeconds);
        assertEquals(180L, response.remainingSeconds);

        verify(gameService).createGame(GameController.GameMode.MULTIPLAYER, 2);
    }

    /**
     * Verifies that a second player can successfully join an
     * existing waiting multiplayer game.
     */
    @Test
    void testJoinGameSuccess() {
        User firstPlayer = new User("Diego9", "diego@test.com", "Secret123");
        firstPlayer.setId(1);

        User secondPlayer = new User("james9", "james@test.com", "Secret456");
        secondPlayer.setId(2);

        Board board = new Board();
        board.setBoardId("Board-join");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        Game savedGame = new Game(firstPlayer, secondPlayer, board);
        savedGame.setId(20);
        savedGame.setStatus(GameStatus.IN_PROGRESS);

        GameController.JoinGameRequest req = new GameController.JoinGameRequest();
        req.playerId = 2;

        when(gameService.joinGame(20, 2)).thenReturn(savedGame);

        when(gameService.getGameDurationSeconds()).thenReturn(180L);
        when(gameService.getRemainingSeconds(savedGame)).thenReturn(175L);

        GameController.GameResponse response = gameController.joinGame(20, req);

        assertNotNull(response);
        assertEquals(20, response.gameId);
        assertEquals(1, response.player1Id);
        assertEquals(2, response.player2Id);
        assertEquals("Board-join", response.boardId);
        assertEquals("IN_PROGRESS", response.status);

        assertEquals(180L, response.durationSeconds);
        assertEquals(175L, response.remainingSeconds);

        verify(gameService).joinGame(20, 2);
    }

    /**
     * Verifies that joining a game with a null request body
     * throws a bad request exception.
     */
    @Test
    void testJoinGameNullBody() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.joinGame(20, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    /**
     * Verifies that attempting to join a non-existent game
     * throws a not found exception.
     */
    @Test
    void testJoinGameNotFound() {
        GameController.JoinGameRequest req = new GameController.JoinGameRequest();
        req.playerId = 2;

        when(gameService.joinGame(50, 2))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game id not found"));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.joinGame(50, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testListWaiting() {
        List<GameController.GameResponse> waitingResponse = gameController.listWaitingGames();
        assertEquals(0, waitingResponse.size());

        Board board = new Board();
        User user1 = new User("user1", "someemail@nowhere.com", "");
        user1.setId(1);

        Game savedGame = new Game(user1, null, board);
        savedGame.setId(11);
        savedGame.setStatus(GameStatus.WAITING);
        when(gameService.getWaitingGames()).thenReturn(List.of(savedGame));
        assertEquals(savedGame.getStatus(), GameStatus.WAITING);

        waitingResponse = gameController.listWaitingGames();
        assertEquals(1, waitingResponse.size());

        // Reject in-progress game
        Board board2 = new Board();
        User user2 = new User("user1", "someemail@nowhere.com", "");
        user1.setId(2);

        Game savedGame2 = new Game(user2, null, board2);
        savedGame.setId(12);
        savedGame.setStatus(GameStatus.IN_PROGRESS);
        when(gameService.getWaitingGames()).thenReturn(List.of(savedGame));

        waitingResponse = gameController.listWaitingGames();
        assertEquals(1, waitingResponse.size());
    }

    /**
     * Verifies that retrieving a board for an existing game
     * returns the expected board identifier and board contents.
     */
    @Test
    void testGetBoard_success() {
        Board board = new Board();
        board.setBoardId("board-2");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        when(gameService.getBoard(50)).thenReturn(board);

        GameController.BoardResponse response = gameController.getBoard(50);

        assertNotNull(response);
        assertEquals("board-2", response.boardId);
        assertEquals("ABCD\nEFGH\nIJKL\nMNOP", response.boardString);

        verify(gameService).getBoard(50);
    }

    /**
     * Verifies  that creating a game with a null request body
     * throws a bad request exception.
     */
    @Test
    void testCreateGameNullBody(){
         ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                 () -> gameController.createGame(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Body is required\"", ex.getMessage());
    }


    /**
     * Verifies that creating a game with no playerId
     * throws a bad request exception.
     */
    @Test
    void testCreateGameNoPlayerId(){
        GameController.CreateGameRequest req = new GameController.CreateGameRequest();
        req.mode = GameController.GameMode.SOLO;
        req.playerId = null;
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameController.createGame(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"playerId is required\"", ex.getMessage());
    }

    /**
     * Verifies that creating a game without a mode
     * throws a bad request exception.
     */
    @Test
    void testCreateGameNoMode(){
        GameController.CreateGameRequest req = new GameController.CreateGameRequest();
        req.mode = null;
        req.playerId = 2;
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameController.createGame(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Mode is required\"", ex.getMessage());
    }

    /**
     * Verifies joining a game with no playerId
     * throws a bad request exception
     */
    @Test
    void testJoinGameMissingPlayerId(){
        GameController.JoinGameRequest req = new GameController.JoinGameRequest();
        req.playerId = null;

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        ()->gameController.joinGame(20,req));
        assertEquals(HttpStatus.BAD_REQUEST,ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"playerId is required\"", ex.getMessage());
    }

    /**
     * Verifies that retrieving an existing game
     * return the correct game summary
     */
    @Test
    void testGetGameSuccess(){
        User player1 = new User("diego9","diego@test.com","Secret123");
        player1.setId(1);

        User player2 = new User("james2","james@test.com","secret123");
        player2.setId(2);

        Board board = new Board();
        board.setBoardId("board_4");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        Game game = new Game();
        game.setId(50);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        game.setBoard(board);

        when(gameService.getGame(50)).thenReturn(game);

        when(gameService.getGameDurationSeconds()).thenReturn(180L);
        when(gameService.getRemainingSeconds(game)).thenReturn(90L);

        GameController.GameResponse response = gameController.getGame(50);

        assertNotNull(response);
        assertEquals(50, response.gameId);
        assertEquals(1, response.player1Id);
        assertEquals(2, response.player2Id);
        assertEquals("board_4", response.boardId);
        assertEquals("IN_PROGRESS", response.status);

        assertEquals(180L, response.durationSeconds);
        assertEquals(90L, response.remainingSeconds);

        verify(gameService).getGame(50);
    }

    /**
     * Verifies that retrieving a non-existent game
     * throws a not found exception.
     */
    @Test
    void testGetGameNonExistent(){
        when(gameService.getGame(2))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Game id not found"));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.getGame(999));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    /**
     * Verifies that retrieving a board for non-existent game
     * throws not found exception
     */
    @Test
    void testGetBoardNullGame(){
        when(gameService.getBoard(50))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Board id not found"));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.getBoard(50));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    /**
     * verifies that submitting a valid word
     * returns the expect submit-word response
     */
    @Test
    void testSubmitWordSuccess(){
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 2;
        req.word = "apple";

        WordSubmissionService.Result result = new WordSubmissionService.Result();
        result.accepted = true;
        result.reason = WordSubmissionService.SubmissionReason.OK;
        result.normalizedWord = "APPLE";
        result.points = 5;

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(15)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(false);
        when(wordSubmissionService.submitWord(15,2,"apple"))
                .thenReturn(result);

        GameController.SubmitWordResponse response = gameController.submitWord(15,req);

        assertNotNull(response);
        assertTrue(response.accepted);
        assertEquals("OK", response.reason);
        assertEquals("APPLE", response.normalizedWord);
        assertEquals(5, response.points);

        verify(wordSubmissionService).submitWord(15, 2, "apple");
    }

    /**
     * Verifies that submitting a word with a null body
     * throws a bad request exception.
     */
    @Test
    void testSubmitWordNullBody() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.submitWord(15, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    /**
     * Verifies that submitting a word without playerId
     * throws a bad request exception.
     */
    @Test
    void testSubmitWordMissingPlayerId() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = null;
        req.word = "apple";

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.submitWord(15, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"playerId is required\"", ex.getMessage());
    }

    /**
     * Verifies that submitting a word without the word field
     * throws a bad request exception.
     */
    @Test
    void testSubmitWordMissingWord() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 2;
        req.word = null;

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> gameController.submitWord(15, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"word is required\"", ex.getMessage());
    }

    /**
     * Verifies that retrieving a score delegates to the score service.
     */
    @Test
    void testGetScoreSuccess() {
        GameScoreService.Totals totals = mock(GameScoreService.Totals.class);

        when(gameScoreService.computeTotals(33)).thenReturn(totals);

        GameScoreService.Totals response = gameController.getScore(33);

        assertNotNull(response);
        assertEquals(totals, response);
        verify(gameScoreService).computeTotals(33);
    }

    /**
     * Verifies that finishing a game delegates to the score service.
     */
    @Test
    void testFinishGameSuccess() {
        GameScoreService.Totals totals = mock(GameScoreService.Totals.class);

        when(gameScoreService.finishGame(44)).thenReturn(totals);

        GameScoreService.Totals response = gameController.finishGame(44);

        assertNotNull(response);
        assertEquals(totals, response);
        verify(gameScoreService).finishGame(44);
    }

    /**
     * Verifies that the boardString returned by getBoard has exactly 4 newline-separated
     * rows each containing 4 characters — the format GamePage splits to build its 16 tiles.
     */
    @Test
    void testGetBoardStringParsesIntoSixteenTiles() {
        Board board = new Board();
        board.setBoardId("board-parse");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");

        when(gameService.getBoard(10)).thenReturn(board);

        GameController.BoardResponse response = gameController.getBoard(10);

        String[] rows = response.boardString.split("\n");
        assertEquals(4, rows.length, "boardString must have exactly 4 rows");
        for (String row : rows) {
            assertEquals(4, row.length(), "each row must have exactly 4 characters");
        }

        int totalTiles = 0;
        for (String row : rows) totalTiles += row.length();
        assertEquals(16, totalTiles, "board must contain exactly 16 tiles total");
    }

    /**
     * Verifies that an accepted word submission returns points greater than zero —
     * confirming the score increment path in GamePage will receive a valid value.
     */
    @Test
    void testSubmitWordAcceptedReturnsPositivePoints() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 1;
        req.word = "cat";

        WordSubmissionService.Result result = new WordSubmissionService.Result();
        result.accepted = true;
        result.reason = WordSubmissionService.SubmissionReason.OK;
        result.normalizedWord = "CAT";
        result.points = 3;

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(5)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(false);
        when(wordSubmissionService.submitWord(5, 1, "cat")).thenReturn(result);

        GameController.SubmitWordResponse response = gameController.submitWord(5, req);

        assertTrue(response.accepted);
        assertEquals("OK", response.reason);
        assertTrue(response.points > 0, "accepted word must return points > 0 for GamePage score update");
    }

    /**
     * Verifies that submitting a word shorter than the minimum length returns
     * accepted=false with reason TOO_SHORT — matching the feedback label GamePage displays.
     */
    @Test
    void testSubmitWordRejectedTooShort() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 1;
        req.word = "ab";

        WordSubmissionService.Result result = new WordSubmissionService.Result();
        result.accepted = false;
        result.reason = WordSubmissionService.SubmissionReason.TOO_SHORT;
        result.normalizedWord = "AB";
        result.points = 0;

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(5)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(false);
        when(wordSubmissionService.submitWord(5, 1, "ab")).thenReturn(result);

        GameController.SubmitWordResponse response = gameController.submitWord(5, req);

        assertFalse(response.accepted);
        assertEquals("TOO_SHORT", response.reason);
    }

    /**
     * Verifies that submitting a word to a non-existent game returns
     * accepted=false with reason GAME_NOT_FOUND — matching the feedback label GamePage displays.
     */
    @Test
    void testSubmitWordRejectedGameNotFound() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 1;
        req.word = "cat";

        WordSubmissionService.Result result = new WordSubmissionService.Result();
        result.accepted = false;
        result.reason = WordSubmissionService.SubmissionReason.GAME_NOT_FOUND;
        result.normalizedWord = "CAT";
        result.points = 0;

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(999)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(false);
        when(wordSubmissionService.submitWord(999, 1, "cat")).thenReturn(result);

        GameController.SubmitWordResponse response = gameController.submitWord(999, req);

        assertFalse(response.accepted);
        assertEquals("GAME_NOT_FOUND", response.reason);
    }

    /**
     * Verifies that submitting a word as a player not in the game returns
     * accepted=false with reason PLAYER_NOT_IN_GAME — matching the feedback label GamePage displays.
     */
    @Test
    void testSubmitWordRejectedPlayerNotInGame() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 99;
        req.word = "cat";

        WordSubmissionService.Result result = new WordSubmissionService.Result();
        result.accepted = false;
        result.reason = WordSubmissionService.SubmissionReason.PLAYER_NOT_IN_GAME;
        result.normalizedWord = "CAT";
        result.points = 0;

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(5)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(false);
        when(wordSubmissionService.submitWord(5, 99, "cat")).thenReturn(result);

        GameController.SubmitWordResponse response = gameController.submitWord(5, req);

        assertFalse(response.accepted);
        assertEquals("PLAYER_NOT_IN_GAME", response.reason);
    }

    /**
     * Verifies that getScore returns a Totals object with the correct player point values —
     * confirming GamePage's score display will receive the right data from the backend.
     */
    @Test
    void testGetScoreReturnsPlayerPointTotals() {
        GameScoreService.Totals totals = new GameScoreService.Totals();
        totals.gameId = 7;
        totals.status = "IN_PROGRESS";
        totals.player1Id = 1;
        totals.player2Id = 2;
        totals.player1Points = 12;
        totals.player2Points = 8;
        totals.winnerPlayerId = null;

        when(gameScoreService.computeTotals(7)).thenReturn(totals);

        GameScoreService.Totals response = gameController.getScore(7);

        assertEquals(7, response.gameId);
        assertEquals(12, response.player1Points);
        assertEquals(8, response.player2Points);
        assertNull(response.winnerPlayerId);
        assertEquals("IN_PROGRESS", response.status);
    }

    /**
     * Verifies that expired games reject word submissions with GAME_NOT_IN_PROGRESS.
     */
    @Test
    void testSubmitWordRejectedWhenGameExpired() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 1;
        req.word = "cat";

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(gameService.getGame(5)).thenReturn(mockGame);
        when(gameService.isGameExpired(mockGame)).thenReturn(true);

        GameController.SubmitWordResponse response = gameController.submitWord(5, req);

        assertFalse(response.accepted);
        assertEquals("GAME_NOT_IN_PROGRESS", response.reason);
        assertEquals("cat", response.normalizedWord);
        assertNull(response.points);

        verify(wordSubmissionService, never()).submitWord(anyInt(), anyInt(), anyString());
    }

    /**
     * Verifies that finished games reject word submissions with GAME_NOT_IN_PROGRESS.
     */
    @Test
    void testSubmitWordRejectedWhenGameAlreadyFinished() {
        GameController.SubmitWordRequest req = new GameController.SubmitWordRequest();
        req.playerId = 1;
        req.word = "cat";

        Game mockGame = mock(Game.class);
        when(mockGame.getStatus()).thenReturn(GameStatus.FINISHED);
        when(gameService.getGame(5)).thenReturn(mockGame);

        GameController.SubmitWordResponse response = gameController.submitWord(5, req);

        assertFalse(response.accepted);
        assertEquals("GAME_NOT_IN_PROGRESS", response.reason);
        assertEquals("cat", response.normalizedWord);
        assertNull(response.points);

        verify(wordSubmissionService, never()).submitWord(anyInt(), anyInt(), anyString());
    }
}