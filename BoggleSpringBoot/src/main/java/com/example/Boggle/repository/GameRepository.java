package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.Game;
import com.example.Boggle.Model.Tables.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Provides standard Spring Data JPA operations such as creating,
 * retrieving, updating, and deleting game records for Game entities.
 * The repository uses:
 * - Game as the entity type
 * - Integer as the primary key type
 */
public interface GameRepository extends JpaRepository<Game, Integer> {
    List<Game> findByStatus(GameStatus status);
}