package com.example.Boggle.util;

import java.util.Random;

/**
 * Utility class for generating and flattening randomized Boggle boards.
 *
 * <p>This class uses the weighted letter bag from {@link BoggleBag} to
 * generate a 4x4 board where more common letters appear more frequently.
 */
public class ShuffleUtil extends BoggleBag {

    /**
     * Random generator used to select letters from the weighted bag.
     */
    private static final Random shuffle = new Random();


    /**
     * Immutable container for a generated board in both grid and flattened form.
     */
    public static class GeneratedBoard {

        /**
         * The generated 4x4 board as a two-dimensional grid.
         */
        public final String[][] boardGrid;
        /**
         * The generated board represented as a newline-separated string.
         */
        public final String flattened;

        /**
         * Creates a generated board Result
         *
         * @param grid the board 4x4 grid
         * @param flattened the flattened string representation of the board
         */
        public GeneratedBoard(String[][] grid, String flattened) {
            this.boardGrid = grid;
            this.flattened = flattened;
        }
    }


    /**
     * Generates a random 4x4 Boggle board using letters selected from the
     * weighted letter bag.
     *
     * @return a {@link GeneratedBoard} containing both the 2D grid and its
     *         flattened string form
     */
    public static GeneratedBoard shuffledBoard() {
        String[][] board = new String[4][4];
        for (int i = 0; i < 16; i++) {
            String character = BoggleBag.getBag().get(shuffle.nextInt(getBag().size()));
            board[i / 4][i % 4] = character;

        }
        String flat = flatten(board);
        return new GeneratedBoard(board, flat);
    }

    /**
     * Converts a 4x4 board into a newline-separated string representation.
     *
     * <p>Each row is written as four consecutive characters, and rows are
     * separated by newline characters.
     *
     * @param grid the 4x4 board to flatten
     * @return the flattened string representation of the board
     */
    public static String flatten(String[][] grid) {
        StringBuilder flattenGrid = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                flattenGrid.append(grid[i][j]);
            }
            if (i < 3) {
                flattenGrid.append('\n');
            }

        }
        return flattenGrid.toString();
    }





}