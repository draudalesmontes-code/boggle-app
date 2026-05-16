package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for retrieving aggregated game statistics from the database.
 * <p>
 * This repository uses native SQL queries to bypass standard entity mapping for
 * high-performance calculation of user-specific metrics across multiple tables.
 * </p>
 */
@Repository
public interface StatsRepository extends JpaRepository<User, Integer> {

    /**
     * A Projection interface used to map the results of the aggregate statistics query.
     * Spring Data JPA automatically maps the aliases in the SQL query (e.g., 'gamesPlayed')
     * to these getter methods.
     */
    interface UserStatsProjection {
        /**
         * @return Total number of games where the user was either player 1 or player 2.
         */
        Long getGamesPlayed();

        /**
         * @return Total number of games where the user was marked as the winner.
         */
        Long getGamesWon();

        /**
         * @return Total count of unique word entries found by the user across all games.
         */
        Long getWordsFound();
    }

    /**
     * Executes a native SQL query to calculate a comprehensive stats summary for a user.
     * <p>
     * The query performs three sub-selects:
     * <ul>
     * <li>Counts games where the user is participant 1 or 2.</li>
     * <li>Counts games where the user's ID matches the winner_player_id.</li>
     * <li>Counts entries in the found_words table associated with the user.</li>
     * </ul>
     * </p>
     *
     * @param userId The ID of the user for whom to calculate statistics.
     * @return A {@link UserStatsProjection} containing the calculated counts.
     */
    @Query(value = "SELECT " +
            "(SELECT COUNT(*) FROM games WHERE player1_id = :userId OR player2_id = :userId) as gamesPlayed, " +
            "(SELECT COUNT(*) FROM games WHERE winner_player_id = :userId) as gamesWon, " +
            "(SELECT COUNT(*) FROM found_words WHERE player_id = :userId) as wordsFound",
            nativeQuery = true)
    UserStatsProjection getUserStats(@Param("userId") Integer userId);
}