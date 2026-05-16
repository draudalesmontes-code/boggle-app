package com.example.Boggle.util;


/**
 * Utility class for converting a flattened board string into a 4x4 grid.
 */
public class unflatten {
    /**
     * Stores a grid representation of the board.
     */
    public String[][] gridBoard;

    /**
     * Parses a newline-separated board string into a 4x4 string array.
     *
     * <p>The input is expected to contain exactly 4 rows, each with 4
     * characters. Each character is converted into a single-cell string
     * in the returned grid.
     *
     * @param boardString the board string to parse
     * @return a 4x4 grid representation of the board
     * @throws IllegalArgumentException if {@code boardString} is null
     */
    public static String [][] parseStringTo4x4 (String boardString){
        if(boardString==null){
            throw new IllegalArgumentException("boardString is null");
        }
        String [] rows = boardString.split("\\R");

        String [][] grid = new String[4][4];
        for(int r = 0; r<4; r++){
            for(int c = 0; c<4; c++){
                grid[r][c] = String.valueOf(rows[r].charAt(c));
            }
        }
        return grid;
    }
}
