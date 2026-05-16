package com.example.Boggle.Model.Tables;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a word found by a player during a game.
 *
 * <p>Each row links a player, a game, and a dictionary word, and records
 * when the word was found. A player cannot submit the same dictionary word
 * more than once in the same game.
 */
@Entity
@Table(name="found_words",
        indexes = {@Index(name = "fk_found_game", columnList = "game_id"),
                @Index(name="fk_found_dictionary",columnList = "dictionary_word_id")
        }
        , uniqueConstraints =
    @UniqueConstraint(name = "unique_word_per_player_per_game",
            columnNames = {"player_id",
                            "game_id",
                            "dictionary_word_id"
            }
    )
)
public class FoundWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name="player_id", foreignKey = @ForeignKey(name = "fk_found_player"))
    private User player;

    @ManyToOne(optional = false)
    @JoinColumn(name="game_id", foreignKey = @ForeignKey(name="fk_found_game"))
    private Game game;

    @ManyToOne(optional = false)
    @JoinColumn(name="dictionary_word_id", foreignKey=@ForeignKey(name="fk_found_dictionary"))
    private Dictionary dictionaryWord;

    @CreationTimestamp
    @Column(name="found_at", nullable = false, updatable = false)
    private LocalDateTime foundAt;

    /**
     * Returns the player who found the word.
     *
     * @return the player entity
     */
    public User getPlayer(){
        return player;
    }

    /**
     * Returns the unique ID of this found-word record.
     *
     * @return the found-word ID
     */
    public Integer getId(){
        return id;
    }

    /**
     * Returns the game in which the word was found.
     *
     * @return the game entity
     */
    public Game getGame(){
        return game;
    }

    /**
     * Returns the dictionary entry associated with the found word.
     *
     * @return the dictionary word entity
     */
    public Dictionary getDictionaryWord() {
        return dictionaryWord;
    }

    /**
     * Returns the time the word was recorded.
     *
     * @return the timestamp when the word was found
     */
    public LocalDateTime getFoundAt() {
        return foundAt;
    }

    /**
     * Sets the player who found the word.
     *
     * @param player the player entity
     */
    public void setPlayer(User player) {
        this.player = player;
    }

    /**
     * Sets the dictionary entry associated with the found word.
     *
     * @param dictionaryWord the dictionary word entity
     */
    public void setDictionaryWord(Dictionary dictionaryWord) {
        this.dictionaryWord = dictionaryWord;
    }

    /**
     * Sets the game in which the word was found.
     *
     * @param game the game entity
     */
    public void setGame(Game game) {
        this.game = game;
    }
}
