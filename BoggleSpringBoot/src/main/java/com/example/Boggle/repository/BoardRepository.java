package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.Board;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Board entities.
 *
 * Provides standard Spring Data JPA operations such as saving,
 * finding, deleting, and listing Board records.
 *
 * The repository uses:
 * - Board as the entity type
 * - String as the primary key type
 */
public interface BoardRepository extends JpaRepository<Board, String> {
}