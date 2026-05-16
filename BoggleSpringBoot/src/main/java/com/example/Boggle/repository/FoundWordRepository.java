package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.FoundWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FoundWord entities.
 *
 * Provides CRUD operations and custom query methods related to
 * words submitted during a game, score aggregation and duplicate checks.
 */
@Repository
public interface FoundWordRepository extends JpaRepository<FoundWord, Integer> {

    /**
     * Fetch all words found in a specific game (regardless of player).
     */
    List<FoundWord> findByGame_Id(Integer gameId);

    /**
     * Fetch a specific player's found words for the "Found Board" display.
     * Ordered by 'foundAt' descending so the most recent finds appear first.
     */
    List<FoundWord> findByGame_IdAndPlayer_IdOrderByFoundAtDesc(Integer gameId, Integer playerId);

    /**
     * Checks if a player has already submitted a specific dictionary word in a game.
     * Prevents duplicates before attempting a database save.
     */
    boolean existsByGame_IdAndPlayer_IdAndDictionaryWord_Id(Integer gameId, Integer playerId, Integer dictionaryWordId);

    /**
     * Calculates the total score for a player in a specific game.
     *
     * @param gameId   ID of the current game
     * @param playerId ID of the player
     * @return total score (returns 0 if no words found)
     */
    @Query("""
    select coalesce(sum(f.dictionaryWord.pointValue), 0)
    from FoundWord f
    where f.game.id = :gameId and f.player.id = :playerId
    """)
    Integer totalPointsForPlayer(@Param("gameId") Integer gameId,
                                 @Param("playerId") Integer playerId);

}