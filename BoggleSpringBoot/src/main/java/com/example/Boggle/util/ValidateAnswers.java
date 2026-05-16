package com.example.Boggle.util;


import com.example.Boggle.Model.Tables.Dictionary;
/**
 * Utility class for validating guessed words against a Boggle board.
 *
 * <p>This class checks whether a guessed word can be formed on the board
 * by traversing adjacent cells horizontally, vertically, or diagonally
 * without reusing the same cell in a single word path.
 */
public class ValidateAnswers{


    /**
     * All valid movement directions for Boggle traversal:
     * vertical, horizontal, and diagonal.
     */
    public static boolean isValidOnBoard(String wordGuessed, String[][]board){
        //checks correct word guessed length
        if (wordGuessed == null) return false;

        String word = wordGuessed.trim().toUpperCase();

        //checks correct word
        if(!existOnBoard(board,word)){
            return false;
        }
        return true;

    }

    /**
     * Checks whether a guessed word is valid on the provided board.
     *
     * <p>The word is trimmed and converted to uppercase before checking.
     * This method currently validates only whether the word exists on the
     * board, not whether it exists in an external dictionary.
     *
     * @param wordGuessed the word submitted by the player
     * @param board the 2D board to search
     * @return {@code true} if the word exists on the board; {@code false} otherwise
     */
    private static final int [][] DIRECTIONS = {
            {1, 0},//down
            {0,1},//right
            {-1,0},//up
            {0,-1},//left
            {-1,1},//diagonal up-right
            {1,1},// diagonal down-right
            {-1,-1},//diagonal up-left
            {1,-1}//diagonal down-left
            };

    /**
     * Searches the board to determine whether the given word can be formed.
     *
     * <p>A word can be formed by starting at a matching cell and recursively
     * moving to adjacent cells without revisiting a cell in the same path.
     *
     * @param board the board to search
     * @param word the uppercase word to find
     * @return {@code true} if the word can be formed on the board; {@code false} otherwise
     */
    private static boolean existOnBoard(String[][] board, String word){
        if(board==null || board.length == 0 || board[0].length == 0 ) return false;//might remove leater
        int rows = board.length;
        int cols = board[0].length;

        boolean [][] visited = new boolean[rows][cols];
        char first = word.charAt(0);

        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                String tile = board[r][c];
                if (tile == null || tile.isEmpty()) continue;

                char tileChar = Character.toUpperCase(tile.charAt(0));
                if (tileChar == first) {
                    if (dfs(board, word, r, c, 0, visited)) return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs a depth-first search from a given board position to determine
     * whether the remaining characters of the word can be matched.
     *
     * @param board the board being searched
     * @param word the word being matched
     * @param r the current row
     * @param c the current column
     * @param idx the current character index in the word
     * @param visisted tracks cells already used in the current search path
     * @return {@code true} if a valid path is found; {@code false} otherwise
     */
    private static boolean dfs(String[][] board, String word, int r, int c, int idx, boolean[][] visisted){
        if(r < 0 || r >=board.length || c < 0 || c >= board[0].length) return false;
        if(visisted[r][c]) return false;

        String tile = board[r][c];
        if(tile == null) return false;

        char tileChar = Character.toUpperCase(tile.charAt(0));
        if (tileChar != word.charAt(idx)) return false;

        if (idx == word.length() - 1) return true;

        visisted[r][c] = true;

        for (int[] d: DIRECTIONS){
            int newRow = r + d[0];
            int newCol =  c + d[1];
            if(dfs(board,word,newRow,newCol,idx+1,visisted)){
                visisted[r][c] = false;
                return true;
            }
        }


        visisted[r][c] = false;
        return false;
    }
}