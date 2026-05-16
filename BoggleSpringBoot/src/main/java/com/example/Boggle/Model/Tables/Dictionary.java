package com.example.Boggle.Model.Tables;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a dictionary word that can be found during gameplay.
 *
 * <p>Each dictionary entry stores the word itself, its point value, and the
 * time the row was created.
 */
@Entity
@Table(name="dictionary")
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="word",length = 100,nullable = false, unique = true)
    private String word;

    @Column(name="point_value", nullable = false)
    private Integer pointValue;

    /**
     * Creates an empty dictionary entity.
     */
    public Dictionary(){}

    /**
     * Returns the dictionary row ID.
     *
     * @return the dictionary ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the stored word.
     *
     * @return the dictionary word
     */
    public String getWord(){
        return word;
    }

    /**
     * Returns the point value assigned to the word.
     *
     * @return the point value
     */
    public int getPointValue() {
        return pointValue;
    }

    /**
     * Set word to dictionary meant for testing purposes
     * @param word that use wants to save.
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Set point for word to dictionary meant for testing purposes
     * @param points the amount of points wanted for a workd
     */
    public void setPointValue(int points) {
        this.pointValue = points;
    }
}
