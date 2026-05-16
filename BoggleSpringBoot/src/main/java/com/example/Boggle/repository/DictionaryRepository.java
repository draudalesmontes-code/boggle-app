package com.example.Boggle.repository;

import com.example.Boggle.Model.Tables.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Dictionary entities.
 *
 * Provides basic CRUD operations for dictionary entries and
 * helper query methods for retrieving words from the dictionary.
 */
public interface DictionaryRepository extends JpaRepository<Dictionary, Integer> {

    /**
     *Finds a dictionary entry whose word matches the given value exactly
     *
     * @param word the exact dictionary word to search for
     * @return the Dictionary matching entry
     */
    Optional<Dictionary> findByWord(String word);

    /**
     * Finds a dictionary entry ignoring letter case. Used for word submission.
     *
     * @param word the dictionary word to search for
     * @return optional containing the matching dictionary entry if found
     */
    Optional<Dictionary> findByWordIgnoreCase(String word);

}