package com.example.Boggle.Model.Tables;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a persisted Boggle game row in the games table.
 *
 * <p>Stores player assignments, the associated board, game status,
 * timestamps for creation, start, and finish, and the winner when applicable.
 */
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne(optional = true)
    @JoinColumn(name = "player2_id", nullable = true)
    private User player2;

    @ManyToOne
    @JoinColumn(name = "winner_player_id")
    private User winner;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "game")
    private List<FoundWord> foundWords;

    //Constructor

    public Game(User player1, User player2, Board board) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = board;
        this.status = GameStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    public Game() {

    }

    //Getters

    public Integer getId() {
        return id;
    }

    public User getPlayer1() {
        return player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public User getWinner() {
        return winner;
    }

    public Board getBoard() {
        return board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public List<FoundWord> getFoundWords() {
        return foundWords;
    }

    //Setters

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public void setFoundWords(List<FoundWord> foundWords) {
        this.foundWords = foundWords;
    }


}