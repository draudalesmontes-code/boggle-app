package com.example.Boggle.Service;

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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the game-over feature.
 *
 * Covers timer expiry detection, status transition to FINISHED,
 * finishedAt timestamp assignment, and the zero-floor on remaining time.
 */
@ExtendWith(MockitoExtension.class)
public class GameOverTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameService gameService;

    private User player;
    private Board board;

    @BeforeEach
    void setup() {
        player = new User("alice", "alice@test.com", "password123");
        player.setId(1);

        board = new Board();
        board.setBoardId("board-1");
        board.setBoardString("ABCD\nEFGH\nIJKL\nMNOP");
    }

    /**
     * When a game's timer has run out, updateGameStatusIfExpired must set
     * its status to FINISHED and record a finishedAt timestamp.
     */
    @Test
    void expiredGameTransitionsToFinishedWithTimestamp() {
        Game game = new Game(player, null, board);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now().minusSeconds(181));
        game.setFinishedAt(null);

        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game result = gameService.updateGameStatusIfExpired(game);

        assertEquals(GameStatus.FINISHED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        verify(gameRepository).save(game);
    }

    /**
     * A game still within its time window must not be transitioned to FINISHED.
     */
    @Test
    void activeGameRemainsInProgress() {
        Game game = new Game(player, null, board);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now().minusSeconds(60));
        game.setFinishedAt(null);

        Game result = gameService.updateGameStatusIfExpired(game);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getFinishedAt());
        verify(gameRepository, never()).save(any(Game.class));
    }

    /**
     * getRemainingSeconds must return 0 — never a negative value — once
     * the 180-second window has fully elapsed.
     */
    @Test
    void remainingTimeIsZeroNotNegativeWhenExpired() {
        Game game = new Game(player, null, board);
        game.setStartedAt(LocalDateTime.now().minusSeconds(300));

        long remaining = gameService.getRemainingSeconds(game);

        assertEquals(0L, remaining);
    }

    /**
     * isGameExpired must return false for a game that still has time remaining,
     * so the board stays enabled and word submissions are accepted.
     */
    @Test
    void isGameExpiredFalseWhileTimeRemains() {
        Game game = new Game(player, null, board);
        game.setStartedAt(LocalDateTime.now().minusSeconds(90));

        assertFalse(gameService.isGameExpired(game));
    }
}
