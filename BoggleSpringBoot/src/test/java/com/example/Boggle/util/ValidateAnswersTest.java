package com.example.Boggle.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//TODO: palindrome, sideways (off board), change board around, longer words, safe, gate, more test cases testing validate answers
//TODO: comments & documentation

/**
 * Unit tests for the ValidateAnswers class.
 *
 * These tests verify the correctness of the word-search algorithm used in a Boggle-style board.
 * Specifically, they check whether a given word can be constructed from adjacent letters
 * on the board using valid movements (horizontal, vertical, and diagonal).
 *
 * Test coverage includes:
 *  - Valid horizontal and diagonal word paths
 *  - Detection of words not present on the board
 *  - Handling of null input
 *  - Longer valid words spanning multiple directions
 *  - Repeated-letter word handling
 *  - Invalid cases where traversal is not possible due to board layout
 *
 * Notes:
 *  - The algorithm does not allow reuse of the same tile in a single word path.
 *  - All input words are normalized to uppercase internally.
 */
@DisplayName("Board Word Validation Tests")
class ValidateAnswersTest {

    /**
     * Primary board used for basic path testing.
     *
     * Layout:
     * C A T S
     * D O G E
     * B I R D
     * F I S H
     *
     * Used to test:
     *  - simple horizontal words (CAT)
     *  - diagonal words (COG)
     *  - invalid words (COW)
     */
    private final String[][] board1 = {
            {"C", "A", "T", "S"},
            {"D", "O", "G", "E"},
            {"B", "I", "R", "D"},
            {"F", "I", "S", "H"}
    };

    /**
     * Secondary board used for longer and more complex word paths.
     *
     * Layout:
     * G B T E
     * O A S L
     * L N V E
     * P E R S
     *
     * Used to test:
     *  - longer valid words (GATE)
     *  - words with repeated letters (LEVELS)
     */
    private final String[][] board2 = {
            {"G", "B", "T", "E"},
            {"O", "A", "S", "L"},
            {"L", "N", "V", "E"},
            {"P", "E", "R", "S"}
    };

    /**
     * Board designed to test invalid traversal cases.
     *
     * Layout:
     * E G A T
     * X X X X
     * X X X X
     * X X X X
     *
     * Although all letters for "GATE" exist,
     * they are arranged in reverse order and cannot be connected
     * through valid adjacent moves.
     */
    private final String[][] board3 = {
            {"E", "G", "A", "T"},
            {"X", "X", "X", "X"},
            {"X", "X", "X", "X"},
            {"X", "X", "X", "X"}
    };

    /**
     * Tests a simple horizontal word.
     * Expected: true
     */
    @Test
    void validWordOnBoardReturnsTrue() {
        assertTrue(ValidateAnswers.isValidOnBoard("CAT", board1));
    }

    /**
     * Tests a valid diagonal traversal.
     * Expected: true
     */
    @Test
    void diagonalWordOnBoardReturnsTrue() {
        assertTrue(ValidateAnswers.isValidOnBoard("COG", board1));
    }

    /**
     * Tests a word that does not exist on the board.
     * Expected: false
     */
    @Test
    void wordNotOnBoardReturnsFalse() {
        assertFalse(ValidateAnswers.isValidOnBoard("COW", board1));
    }

    /**
     * Tests handling of null input.
     * Expected: false
     */
    @Test
    void nullWordReturnsFalse() {
        assertFalse(ValidateAnswers.isValidOnBoard(null, board1));
    }

    /**
     * Tests a longer valid word that spans multiple directions.
     * Expected: true
     */
    @Test
    void longerValidWordOnBoardReturnsTrue() {
        assertTrue(ValidateAnswers.isValidOnBoard("GATE", board2));
    }

    /**
     * Tests a word with repeated letters (palindrome-like structure).
     * Ensures DFS correctly handles revisiting similar characters
     * without reusing the same tile.
     *
     * Expected: true
     */
    @Test
    void repeatedLetterWordReturnsTrue() {
        assertTrue(ValidateAnswers.isValidOnBoard("LEVELS", board2));
    }

    /**
     * Tests an invalid case where letters exist but cannot form
     * a valid adjacent path.
     *
     * Expected: false
     */
    @Test
    void invalidTraversalReturnsFalse() {
        assertFalse(ValidateAnswers.isValidOnBoard("GATE", board3));
    }

    /**
     * Tests that words requiring reuse of the same tile are rejected.
     * Example: "CACA" would require reusing 'C' or 'A'.
     *
     * Expected: false
     */
    @Test
    void cannotReuseSameTileReturnsFalse() {
        assertFalse(ValidateAnswers.isValidOnBoard("CACA", board1));
    }

    /**
     * Tests that lowercase input is normalized correctly.
     * Expected: true
     */
    @Test
    void lowercaseInputStillWorks() {
        assertTrue(ValidateAnswers.isValidOnBoard("cat", board1));
    }

    /**
     * Tests that leading/trailing whitespace is handled correctly.
     * Expected: true
     */
    @Test
    void trimmedInputStillWorks() {
        assertTrue(ValidateAnswers.isValidOnBoard("  cat  ", board1));
    }

    /**
     * Tests behavior when the board is null.
     * Expected: false
     */
    @Test
    void nullBoardReturnsFalse() {
        assertFalse(ValidateAnswers.isValidOnBoard("CAT", null));
    }

    /**
     * Tests behavior when the board is empty.
     * Expected: false
     */
    @Test
    void emptyBoardReturnsFalse() {
        String[][] emptyBoard = {};
        assertFalse(ValidateAnswers.isValidOnBoard("CAT", emptyBoard));
    }
}