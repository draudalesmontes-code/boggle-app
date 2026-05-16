package com.example.Boggle.Service;


import com.example.Boggle.Model.Controllers.GameController;
import com.example.Boggle.Model.Tables.Board;
import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.GameStatus;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.repository.BoardRepository;
import com.example.Boggle.repository.GameRepository;
import com.example.Boggle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

/**
 * Unit tests for {@link GameService}.
 *
 * <p>
 * These tests verify game creation, joining behavior, and retrieval of games
 * and boards. Repository dependencies are mocked so that each test focuses only
 * on the service-layer logic.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    /**
     * Mocked repository used for game persistence and look up behavior
     */
    @Mock
    private GameRepository gameRepository;

    /**
     * Mocked repository used for board persistence.
     */
    @Mock
    private BoardRepository boardRepository;

    /**
     * Mocked repository used for player lookup.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * Service under test with mocked dependencies injected.
     */
    @InjectMocks
    private GameService gameService;

    /**
     * Sample primary player used across tests.
     */
    private User player1;

    /**
     * Sample secondary player used across tests.
     */
    private User player2;

    /**
     * Sample board returned by the mocked board repository.
     */
    private Board savedBoard;

    /**
     * Initializes reusable test data before each test case.
     */
    @BeforeEach
    void setup(){
        player1 = new User("alice","alice@test.com","password123");
        player1.setId(1);

        player2 = new User("James","james@test.com","password123");
        player2.setId(2);

        savedBoard = new Board();
        savedBoard.setBoardId("board-1");
        savedBoard.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");
    }

    /**
     * Test create SOLO game session
     */
    @Test
    void testSoloGameCreation(){

        Integer playerId = 1;
        GameController.GameMode mode = GameController.GameMode.SOLO;

        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);
        when(gameRepository.save(any(Game.class))).thenAnswer(mockedGame -> {
           Game gamePassedToSave = mockedGame.getArgument(0);
           return gamePassedToSave;
        });

        Game gameResult = gameService.createGame(mode,playerId);

        assertNotNull(gameResult);
        assertEquals(player1, gameResult.getPlayer1());
        assertNull(gameResult.getPlayer2());
        assertEquals(savedBoard, gameResult.getBoard());
        assertEquals(GameStatus.IN_PROGRESS, gameResult.getStatus());
        assertNotNull(gameResult.getStartedAt());
        assertNull(gameResult.getFinishedAt());

        verify(userRepository).findById(playerId);
        verify(boardRepository).save(any(Board.class));
        verify(gameRepository).save(any(Game.class));

    }

    /**
     * Verifies multiplayer game creation
     */
    @Test
    void testCreateMultiplayerGame(){
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);
        when(gameRepository.save(any(Game.class))).thenAnswer(
                invocation -> {
                    Game game = invocation.getArgument(0);
                    return game;
                }
        );

        Game result = gameService.createGame(GameController.GameMode.MULTIPLAYER,1);

        assertNotNull(result);
        assertEquals(player1, result.getPlayer1());
        assertNull(result.getPlayer2());
        assertEquals(savedBoard, result.getBoard());
        assertEquals(GameStatus.WAITING, result.getStatus());
        assertNull(result.getStartedAt());
        assertNull(result.getFinishedAt());
    }


    /**
     * Verifies that createGame rejects a null player id.
     */
    @Test
    void createGameThrowsWhenPlayerIdNull() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.createGame(GameController.GameMode.SOLO, null)
        );

        assertEquals(BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("playerId is required"));
    }

    /**
     * Verifies that createGame throws not found when the requesting player does not exist.
     */
    @Test
    void createGameThrowsWhenPlayerNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.createGame(GameController.GameMode.SOLO, 1)
        );

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("playerId not found"));

        verify(userRepository).findById(1);
    }

    /**
     * Verifies that a waiting multiplayer game can be joined by a valid second player,
     * transitioning the game into progress.
     */
    @Test
    void joinGameSuccess() {
        Game waitingGame = new Game(player1, null, savedBoard);
        waitingGame.setStatus(GameStatus.WAITING);
        waitingGame.setStartedAt(null);

        when(gameRepository.findById(10)).thenReturn(Optional.of(waitingGame));
        when(userRepository.findById(2)).thenReturn(Optional.of(player2));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game result = gameService.joinGame(10, 2);

        assertEquals(player2, result.getPlayer2());
        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartedAt());
        verify(gameRepository).save(waitingGame);
    }

    /**
     * Verifies that joinGame throws not found when the target game does not exist.
     */
    @Test
    void joinGameThrowsWhenGameNotFound() {
        when(gameRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, 2)
        );

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Game id not found"));
    }

    /**
     * Verifies that joinGame rejects a null player id.
     */
    @Test
    void joinGameThrowsWhenPlayerIdNull() {
        Game waitingGame = new Game(player1, null, savedBoard);
        waitingGame.setStatus(GameStatus.WAITING);

        when(gameRepository.findById(10)).thenReturn(Optional.of(waitingGame));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, null)
        );

        assertEquals(BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("playerId is required"));
    }

    /**
     * Verifies that joinGame throws not found when the joining player does not exist.
     */
    @Test
    void joinGameThrowsWhenPlayerNotFound() {
        Game waitingGame = new Game(player1, null, savedBoard);
        waitingGame.setStatus(GameStatus.WAITING);

        when(gameRepository.findById(10)).thenReturn(Optional.of(waitingGame));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, 2)
        );

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("playerId not found"));
    }

    /**
     * Verifies that joinGame rejects games that are not in the waiting state.
     */
    @Test
    void joinGameThrowsWhenGameNotWaiting() {
        Game inProgressGame = new Game(player1, null, savedBoard);
        inProgressGame.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findById(10)).thenReturn(Optional.of(inProgressGame));
        when(userRepository.findById(2)).thenReturn(Optional.of(player2));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, 2)
        );

        assertEquals(CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Game is not joinable"));
    }

    /**
     * Verifies that joinGame rejects attempts to join a game that already has a second player.
     */
    @Test
    void joinGameThrowsWhenGameAlreadyFull() {
        Game waitingButFullGame = new Game(player1, player2, savedBoard);
        waitingButFullGame.setStatus(GameStatus.WAITING);

        User thirdPlayer = new User("charlie", "charlie@test.com", "pw");
        thirdPlayer.setId(3);

        when(gameRepository.findById(10)).thenReturn(Optional.of(waitingButFullGame));
        when(userRepository.findById(3)).thenReturn(Optional.of(thirdPlayer));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, 3)
        );

        assertEquals(CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Game already full"));
    }

    /**
     * Verifies that a player cannot join their own waiting game.
     */
    @Test
    void joinGameThrowsWhenPlayerJoinsOwnGame() {
        Game waitingGame = new Game(player1, null, savedBoard);
        waitingGame.setStatus(GameStatus.WAITING);

        when(gameRepository.findById(10)).thenReturn(Optional.of(waitingGame));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.joinGame(10, 1)
        );

        assertEquals(BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Player cannot join own game"));
    }

    /**
     * Verifies that getGame returns the requested game when it exists.
     */
    @Test
    void getGameSuccess() {
        Game game = new Game(player1, null, savedBoard);
        game.setStatus(GameStatus.WAITING);

        when(gameRepository.findById(5)).thenReturn(Optional.of(game));

        Game result = gameService.getGame(5);

        assertEquals(game, result);
        verify(gameRepository).findById(5);
    }

    /**
     * Verifies that getGame throws not found when the requested game does not exist.
     */
    @Test
    void getGameThrowsWhenNotFound() {
        when(gameRepository.findById(5)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> gameService.getGame(5)
        );

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Game id not found"));
    }

    /**
     * Verifies that getBoard returns the board associated with the requested game.
     */
    @Test
    void getBoardSuccess() {
        Game game = new Game(player1, null, savedBoard);
        when(gameRepository.findById(5)).thenReturn(Optional.of(game));

        Board result = gameService.getBoard(5);

        assertEquals(savedBoard, result);
        verify(gameRepository).findById(5);
    }

    /**
     * Verifies that the game duration returns the expected fixed round length.
     */
    @Test
    void testGetGameDurationSeconds() {
        assertEquals(180L, gameService.getGameDurationSeconds());
    }

    /**
     * Verifies that a game that has not started yet returns the full remaining time.
     */
    @Test
    void testGetRemainingSecondsWhenGameNotStarted() {
        Game game = new Game();
        game.setStartedAt(null);

        long remaining = gameService.getRemainingSeconds(game);

        assertEquals(180L, remaining);
    }

    /**
     * Verifies that remaining time decreases for a started game.
     */
    @Test
    void testGetRemainingSecondsForStartedGame() {
        Game game = new Game();
        game.setStartedAt(LocalDateTime.now().minusSeconds(30));

        long remaining = gameService.getRemainingSeconds(game);

        assertTrue(remaining <= 150L && remaining >= 149L);
    }

    /**
     * Verifies that remaining time never becomes negative after expiration.
     */
    @Test
    void testGetRemainingSecondsNeverNegative() {
        Game game = new Game();
        game.setStartedAt(LocalDateTime.now().minusSeconds(300));

        long remaining = gameService.getRemainingSeconds(game);

        assertEquals(0L, remaining);
    }

    /**
     * Verifies that a game with no start time is not expired.
     */
    @Test
    void testIsGameExpiredFalseWhenGameNotStarted() {
        Game game = new Game();
        game.setStartedAt(null);

        assertFalse(gameService.isGameExpired(game));
    }

    /**
     * Verifies that a started game becomes expired after the timer runs out.
     */
    @Test
    void testIsGameExpiredTrueWhenTimeRunsOut() {
        Game game = new Game();
        game.setStartedAt(LocalDateTime.now().minusSeconds(181));

        assertTrue(gameService.isGameExpired(game));
    }

    /**
     * Verifies that getGame updates an expired in-progress game to finished.
     */
    @Test
    void testGetGameUpdatesExpiredGameStatus() {
        Game expiredGame = new Game(player1, null, savedBoard);
        expiredGame.setStatus(GameStatus.IN_PROGRESS);
        expiredGame.setStartedAt(LocalDateTime.now().minusSeconds(181));
        expiredGame.setFinishedAt(null);

        when(gameRepository.findById(5)).thenReturn(Optional.of(expiredGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game result = gameService.getGame(5);

        assertEquals(GameStatus.FINISHED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        verify(gameRepository).save(expiredGame);
    }

    /**
     * Verifies that updateGameStatusIfExpired marks an expired in-progress game as finished.
     */
    @Test
    void testUpdateGameStatusIfExpiredMarksFinished() {
        Game game = new Game(player1, null, savedBoard);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now().minusSeconds(181));
        game.setFinishedAt(null);

        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game updated = gameService.updateGameStatusIfExpired(game);

        assertEquals(GameStatus.FINISHED, updated.getStatus());
        assertNotNull(updated.getFinishedAt());
        verify(gameRepository).save(game);
    }

    /**
     * Verifies that updateGameStatusIfExpired leaves an active game unchanged.
     */
    @Test
    void testUpdateGameStatusIfExpiredLeavesActiveGameUnchanged() {
        Game game = new Game(player1, null, savedBoard);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now().minusSeconds(30));
        game.setFinishedAt(null);

        Game updated = gameService.updateGameStatusIfExpired(game);

        assertEquals(GameStatus.IN_PROGRESS, updated.getStatus());
        assertNull(updated.getFinishedAt());
        verify(gameRepository, never()).save(any(Game.class));
    }

    /**
     * Verifies that updateGameStatusIfExpired does not modify a game that is already finished.
     */
    @Test
    void testUpdateGameStatusIfExpiredDoesNotChangeFinishedGame() {
        Game game = new Game(player1, null, savedBoard);
        game.setStatus(GameStatus.FINISHED);
        game.setStartedAt(LocalDateTime.now().minusSeconds(181));
        game.setFinishedAt(LocalDateTime.now().minusSeconds(1));

        Game updated = gameService.updateGameStatusIfExpired(game);

        assertEquals(GameStatus.FINISHED, updated.getStatus());
        assertNotNull(updated.getFinishedAt());
        verify(gameRepository, never()).save(any(Game.class));
    }

}
