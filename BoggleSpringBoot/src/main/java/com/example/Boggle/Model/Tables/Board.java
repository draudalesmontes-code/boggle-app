package com.example.Boggle.Model.Tables;


import jakarta.persistence.*;

import java.time.LocalDateTime;



/**
 * Entity representing a persisted Boggle board.
 *
 * <p>Each board stores a public board identifier and a flattened string
 * representation of the letter layout used during gameplay.
 */
@Entity
@Table(name = "boards")
public class Board {

    /**
     * Creates an empty board entity.
     */
    public Board(){}

    /**
     * Unique public identifier for the board.
     */
    @Id
    @Column(name = "board_id",length = 50, nullable = false)
    private String boardId;

    /**
     * Flattened string representation of the board letters.
     */
    @Column(name="board_string",columnDefinition = "text", nullable = false)
    private String boardString;


    /**
     * Returns the board identifier.
     *
     * @return the board ID
     */
    public String getBoardId() { return boardId; }

    /**
     * Sets the board identifier.
     *
     * @param boardId the board ID
     */
    public void setBoardId(String boardId) { this.boardId = boardId; }

    /**
     * Returns the flattened board string.
     *
     * @return the board string
     */
    public String getBoardString() { return boardString; }

    /**
     * Sets the flattened board string.
     *
     * @param boardString the board string
     */
    public void setBoardString(String boardString) { this.boardString = boardString; }


}
