# Specification Document

## Boggle Buddies

### Project Abstract

**Boggle Buddies** is a real time multiplayer word game inspired by the classic game **Boggle**, where players compete to find as many valid words as possible from a randomly generated letter board within a time limit. The application features both <u>live multiplayer matches</u> and a <u>player vs computer mode</u>. Users are able to play as a *guest* or *create an account* to track long term stats like wins and losses. The system includes *board shuffling algorithms*, *dictionary-based validation*, *rule enforcement* like no duplicate words, and a *dynamic scoring screen* that highlights unique vs shared words. This is built with a **React** frontend and a **Java Spring Boot** backend that allows *settings customization*, *real time game sync*, and *persistent data storage* using a **SQL** database and is containerized using **Docker** for consistency. 

### Customer

The general customer for **Boggle Buddies** includes casual word game players, students, and competitive players who enjoy fast paced vocab challenges, specifically those interested in a real time multiplayer setting. The system is designed for users who value social gameplay, competitive scoring, and a customizable setting in an accessible web environment.

### Specification

#### Technology Stack

```mermaid
flowchart RL
subgraph Frontend
	A(Javascript: React)
end
	
subgraph Backend
	B(Java: Spring Boot REST API)
    R(Real-Time Layer: WebSocket / TBD)
end
	
subgraph Database
	C[(SQL Database)]
end

A <-->|"REST API (HTTP)"| B
B <-->|WebSocket| C
B <--> C
R <--> B
```

#### Database

```mermaid
erDiagram
    users ||--o{ games : "player1 / player2"
    users ||--o{ found_words : submits
    games ||--o{ found_words : contains
    games ||--|| boards : uses
    found_words }o--|| dictionary : references
    games }o--|| users : winner

    users {
        int id PK
        string username
        string email
        string password_hash
        datetime created_at
    }

    games {
        int id PK
        int player1_id FK
        int player2_id FK
        varchar board_id FK
        int winner_player_id FK
        enum status
        datetime created_at
        datetime started_at
        datetime finished_at
    }

    boards {
        varchar board_id PK
        text board_string
    }

    dictionary {
        int id PK
        string word
        int point_value
    }

    found_words {
        int id PK
        int player_id FK
        int game_id FK
        int dictionary_word_id FK
    }
```

#### Class Diagram

```mermaid
classDiagram
    class User {
        +int userId
        +String username
        +boolean isGuest
        +int wins
        +int losses
    }
    class Game {
        +int gameId
        +Board board
        +List players
        +GameStatus status
        +void startGame()
        +void endGame()
        +void calculateScores()
    }
    class Board {
        +char[][] grid
        +void shuffleDice()
        +boolean isValidPath(String word)
    }
    class WordValidator {
        +boolean existsInDictionary(String word)
        +boolean followsRules(String word, Board board)
    }
    class WordEntry {
        +String word
        +int score
        +boolean isUnique
    }
    User --> WordEntry
    Game --> WordEntry
    Game --> Board
    Game --> User
    Game --> WordValidator
```

#### Sequence Diagram

```mermaid
sequenceDiagram

participant Player1
participant Player2
participant Frontend
participant Backend
participant Database

Player1 ->> Frontend: Join Room
Frontend ->> Backend: Create/Join Game
Backend ->> Database: Create/Join Game in DB
Backend ->> Frontend: Game Created

Player2 ->> Frontend: Join Room
Frontend ->> Backend: Join Game
Backend ->>Frontend: Game State Sync

Frontend ->> Backend: Submit Word
Backend ->>Backend: Validate Word
Backend ->> Database: Store WordEntry
Backend ->> Frontend: Real time Update
```

## Features

- Multiplayer gameplay
- Guest and registered user modes
- Random board generation using dice-based shuffling
- Dictionary-based word validation
- Duplicate word prevention
- Score updates during gameplay
- Unique vs shared word highlighting
- Persistent user stats (wins/losses)

## Testing & CI/CD

- Backend testing with JUnit
- Static analysis using PMD
- CI pipeline runs linting and tests on each commit

### Standards & Conventions

<!--This is a link to a seperate coding conventions document / style guide-->
[Style Guide & Conventions](STYLE.md)

---
*Last Updated: Sprint 2*