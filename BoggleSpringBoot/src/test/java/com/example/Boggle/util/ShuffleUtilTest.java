package com.example.Boggle.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BoggleBag, ShuffleUtil, and unflatten
 *
 * These tests verify:
 *  - the weighted letter bag used for board generation
 *  - the structure and formatting of generated boards
 *  - conversion of a 4x4 grid into the flattened board string format
 *  - conversion of a flattened board string back into a 4x4 grid
 */
@DisplayName("Shuffle and Board Utility Tests")
class ShuffleUtilTest {
    /**
     * Verifies that the weighted letter bag is built correctly.
     * This checks the total bag size and confirms the counts of
     * one common letter and one rare letter.
     */
    @Test
    void getBagContainsExpectedWeightedSize() {
        List<String> bag = BoggleBag.getBag();

        // Total number of weighted letters defined in WEIGHTED_BAG
        assertEquals(98, bag.size());

        // Check one common letter and one rare letter
        assertEquals(12, bag.stream().filter("E"::equals).count());
        assertEquals(1, bag.stream().filter("Q"::equals).count());
    }

    /**
     * Verifies that shuffle_board() returns a valid generated board.
     * Since board generation is random, this test checks the board's
     * structure and formatting rather than exact letter placement.
     */
    @Test
    void shuffleBoardReturnsFourByFourBoardAndFlattenedString() {
        ShuffleUtil.GeneratedBoard generatedBoard = ShuffleUtil.shuffledBoard();

        assertNotNull(generatedBoard);
        assertNotNull(generatedBoard.boardGrid);
        assertEquals(4, generatedBoard.boardGrid.length);

        // Verify the generated board is 4x4 and every tile contains
        // a non-blank letter from the weighted Boggle bag
        for (String[] row : generatedBoard.boardGrid) {
            assertEquals(4, row.length);
            for (String cell : row) {
                assertNotNull(cell);
                assertFalse(cell.isBlank());
                assertTrue(BoggleBag.getBag().contains(cell));
            }
        }

        // Verify the flattened board string has 4 rows and 4 characters per row
        // Split by any newline character (Java regex \R)
        String[] rows = generatedBoard.flattened.split("\\R");
        assertEquals(4, rows.length);
        for (String row : rows) {
            assertEquals(4, row.length());
        }
    }

    /**
     * Verifies that flatten() converts a 4x4 grid into the expected
     * board string format used by the backend.
     */
    @Test
    void flattenFormatsBoardAsExpected() {
        String[][] grid = {
                {"A", "B", "C", "D"},
                {"E", "F", "G", "H"},
                {"I", "J", "K", "L"},
                {"M", "N", "O", "P"}
        };

        String flattened = ShuffleUtil.flatten(grid);

        assertEquals("ABCD\nEFGH\nIJKL\nMNOP", flattened);
    }

    /**
     * Verifies that parseStringTo4x4() correctly reconstructs a 4x4 grid
     * from a flattened board string.
     */
    @Test
    void parseStringTo4x4ReconstructsBoardAsExpected() {
        String[][] expectedGrid = {
                {"A", "B", "C", "D"},
                {"E", "F", "G", "H"},
                {"I", "J", "K", "L"},
                {"M", "N", "O", "P"}
        };

        String boardString = "ABCD\nEFGH\nIJKL\nMNOP";

        String[][] actualGrid = unflatten.parseStringTo4x4(boardString);

        // Use deepEquals because 2D arrays cannot be compared correctly with assertEquals
        assertTrue(Arrays.deepEquals(expectedGrid, actualGrid));
    }

    /**
     * Verifies that flatten() and parseStringTo4x4() work together
     * as inverse operations for a valid 4x4 board.
     */
    @Test
    void flattenThenUnflattenReturnsOriginalBoard() {
        String[][] originalGrid = {
                {"A", "B", "C", "D"},
                {"E", "F", "G", "H"},
                {"I", "J", "K", "L"},
                {"M", "N", "O", "P"}
        };

        String flattened = ShuffleUtil.flatten(originalGrid);
        String[][] parsedGrid = unflatten.parseStringTo4x4(flattened);

        assertEquals("ABCD\nEFGH\nIJKL\nMNOP", flattened);
        assertTrue(Arrays.deepEquals(originalGrid, parsedGrid));
    }

    /**
     * Verifies that parseStringTo4x4() throws an exception when given null input.
     */
    @Test
    void parseStringTo4x4ThrowsExceptionWhenInputIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            unflatten.parseStringTo4x4(null);
        });
    }
}