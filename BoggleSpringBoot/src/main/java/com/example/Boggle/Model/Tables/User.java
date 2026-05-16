package com.example.Boggle.Model.Tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an application user entity mapped to the users table, including identity, credentials,
 * and relationships to games and found words.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    //NEVER STORE PLAIN PASSWORDS ALWAYS CRYPTOGRAPHIC HASHES
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //Relations

    @JsonIgnore
    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    private List<Game> gamesAsPlayer1 = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    private List<Game> gamesAsPlayer2 = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "winner", fetch = FetchType.LAZY)
    private List<Game> gamesWon = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<FoundWord> foundWords = new ArrayList<>();

    @Column(name="is_guest", nullable=false)
    private boolean isGuest = false;

    @Column(name="profile_picture", length=50)
    private String profilePicture;

    public boolean isGuest() { return isGuest; }
    public void setGuest(boolean guest) { isGuest = guest; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    //Constructor
    protected User() {

    }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    //Getters

    public Integer getId() {
        return id;
    }
    public void setId(Integer id){this.id = id; }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Game> getGamesAsPlayer1() {
        return gamesAsPlayer1;
    }

    public List<Game> getGamesAsPlayer2() {
        return gamesAsPlayer2;
    }

    public List<Game> getGamesWon() {
        return gamesWon;
    }

    public List<FoundWord> getFoundWords() {
        return foundWords;
    }

    //Setters

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}