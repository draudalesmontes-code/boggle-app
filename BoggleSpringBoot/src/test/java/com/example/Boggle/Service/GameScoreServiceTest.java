package com.example.Boggle.Service;

import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.GameStatus;
import com.example.Boggle.Model.Tables.User;
import com.example.Boggle.repository.FoundWordRepository;
import com.example.Boggle.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;


import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameScoreService.
 *
 * These tests verify score aggregation and end-of-game logic.
 * The service is responsible for:
 *
 *  - computing each player's total score for a game
 *  - determining which player won
 *  - marking a game as finished
 *  - recording the winner and finish time when the game ends
 *
 * Mock repositories are used so that scoring behavior can be tested
 * independently of the database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Game Score Service Tests")
class GameScoreServiceTest {

    /**
     * Repository used to load and save Game entities.
     */
    @Mock
    private GameRepository gameRepository;

    /**
     * Repository used to calculate total points scored by each player.
     */
    @Mock
    private FoundWordRepository foundWordRepository;

    /**
     * Service under test.
     */
    @InjectMocks
    private GameScoreService gameScoreService;

    /**
     * Verifies that computeTotals correctly returns each player's score
     * and identifies player one as the winner when player one has more points.
     *
     * Expected result:
     *  - player 1 total = 8
     *  - player 2 total = 5
     *  - winnerPlayerId = 1
     */
    @Test
    void computeTotalsReturnsCorrectScoresAndWinnerWhenPlayerOneWins() {
        User player1 = new User("p1", "p1@test.com", "hash");
        User player2 = new User("p2", "p2@test.com", "hash");
        Game game = new Game();
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);


        setPrivateId(player1, 1);
        setPrivateId(player2, 2);
        game.setId(10);

        when(gameRepository.findById(10)).thenReturn(Optional.of(game));
        when(foundWordRepository.totalPointsForPlayer(10, 1)).thenReturn(8);
        when(foundWordRepository.totalPointsForPlayer(10, 2)).thenReturn(5);

        GameScoreService.Totals totals = gameScoreService.computeTotals(10);

        assertEquals(10, totals.gameId);
        assertEquals("IN_PROGRESS", totals.status);
        assertEquals(1, totals.player1Id);
        assertEquals(2, totals.player2Id);
        assertEquals(8, totals.player1Points);
        assertEquals(5, totals.player2Points);
        assertEquals(1, totals.winnerPlayerId);
    }

    /**
     * Verifies that finishGame:
     *  - computes final totals
     *  - marks the game status as FINISHED
     *  - sets the finish timestamp
     *  - stores the correct winner on the Game entity
     *
     * In this scenario, player two has the higher score and should be saved
     * as the winner.
     */
    @Test
    void finishGameSetsStatusFinishedAndWinner() {
        User player1 = new User("p1", "p1@test.com", "hash");
        User player2 = new User("p2", "p2@test.com", "hash");
        Game game = new Game();
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);


        setPrivateId(player1, 1);
        setPrivateId(player2, 2);
        game.setId(22);

        when(gameRepository.findById(22)).thenReturn(Optional.of(game));
        when(foundWordRepository.totalPointsForPlayer(22, 1)).thenReturn(4);
        when(foundWordRepository.totalPointsForPlayer(22, 2)).thenReturn(7);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameScoreService.Totals totals = gameScoreService.finishGame(22);

        assertNotNull(totals);
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertNotNull(game.getFinishedAt());
        assertEquals(player2, game.getWinner());
    }

    /**
     * Helper method used to assign IDs to User entities in tests.
     *
     * Since test users are not being persisted through JPA,
     * reflection is used to simulate generated IDs.
     */
    private static void setPrivateId(User user, Integer id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}