package com.example.Boggle.Model.Controllers;

import com.example.Boggle.repository.StatsRepository;
import com.example.Boggle.repository.StatsRepository.UserStatsProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing and retrieving user-related statistics.
 * <p>
 * This controller provides endpoints to access aggregated data regarding
 * a player's performance, including games played, games won, and total words found.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class StatsController {

    /**
     * Repository for accessing player statistics via custom database queries.
     */
    @Autowired
    private StatsRepository statsRepository;

    /**
     * Retrieves the game statistics for a specific user.
     * <p>
     * This method queries the database for the total count of games the user
     * participated in, the number of games where the user was the winner,
     * and the total number of words successfully found across all games.
     * </p>
     *
     * @param userId the unique identifier of the user whose stats are being requested
     * @return a {@link ResponseEntity} containing the {@link UserStatsProjection} if found,
     * or a 404 Not Found response if the statistics cannot be retrieved
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsProjection> getStats(@PathVariable Integer userId) {
        UserStatsProjection stats = statsRepository.getUserStats(userId);

        if (stats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }
}