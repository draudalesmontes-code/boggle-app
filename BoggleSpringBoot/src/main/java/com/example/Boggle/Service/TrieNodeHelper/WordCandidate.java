package com.example.Boggle.Service.TrieNodeHelper;

/**
 * Represents a valid word candidate found on a board.
 *
 * <p>This object stores the word and its associated point value.
 *
 */
public class WordCandidate {
    private String word;
    private int points;

    /**
     * Creates a word candidate.
     *
     * @param word word represented by this candidate
     * @param points point value associated with the word
     */
    public WordCandidate(String word, int points){
        this.word = word;
        this.points = points;
    }

    public String getWord(){
        return word;
    }
    public int getPoints(){
        return points;
    }

}
