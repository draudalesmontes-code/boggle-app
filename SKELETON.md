# Boggle Buddies Project

## Project Name: Boggle Buddies

### 1. Overview
Implement an application for the game **Boggle**. The application should include algorithms to (1) randomly shuffle the letter dice to generate a board and (2) check whether submitted words are valid.

The application should support **real-time multiplayer** games with other users, as well as games where a user can compete against the **machine**. Users should be able to play as a **guest** or create an **account**.

Core gameplay features include tracking and displaying **user stats** (e.g., wins/losses), showing each player’s **word list** at the end of the game, and **highlighting unique words**. Games should include a **timer**, and the application should allow **customizable game settings** and board options (within project scope).

### 2. Scope

#### 2.1 Must-have (MVP)
- **Account registration** (authentication)
- **Login** 
- **Guest Play**
- **Real-time multiplayer** 
- **Play vs Machine**
- **Settings Customization**
- **Stats display**
- **Display words and highlight unique words**

#### 2.2 Extra/Out of Scope
- **Share custom boards**
- **Machine difficulty setting** (applicable to board and machine)
- **Ranking system**
- **multi-alphabet**

### 3. High-Level Requirements

#### 3.1 Gameplay
- Generate board with shuffling algorithm.
- Add/Removing words
- Word validation (Word should exist in dictionary)
- Game Rule check algorithm
    1.  Duplicate words not allowed
    2.  No using the same letter twice
    3.  Word length 3 or greater
    4.  Words have to be diagonally or side by side to connect.
- Scoring Screen
    1.  Show each player's submitted words
    2.  highlights **Unique** vs **Shared** entries
    3.  display total score per player and winner/loser/ties

#### 3.2 Multiplayer
- Users can create/join game rooms
- Real-time game state sync

#### 3.3 Accounts + stats
- Registered users can view stats
- Guests can play without saving long-term stats

### 4. Tech Stack

#### Frontend
- React/React Native

#### Backend
- Java
- Spring Boot
- REST API 
- Real-time communication **TBD**

#### Database
- SQL
- **Cloud base TBD**

#### Tooling
- Docker (development consistency)
- Gitlab (Versioning)
- Postman (API Testing)
- JUNIT + Mockito (backend unit/integration tests)

#### Source
- **Dictionary database TBD**

### 5. Repository Structure (Current)

- API_DOCUMENTS/
  - GAME_API_ENDPOINTS.md
  - USER_API_ENDPOINTS.md
  - GameController_v2.md
  - UserController_v2.md

- BoggleSpringBoot/
  - src/main/java/com.example.Boggle/
    - Model/
      - Controllers/
      - Tables/
    - repository/
    - Service/
    - Security/
    - util/
    - BoggleApplication.java
  - src/main/resources/
    - db/
      - BoggleDB.sql
      - DictionaryData.sql
    - application.properties
  - src/test/

- Docker/
  - docker-compose.yml
  - Dockerfile

- frontend/
  - src/
    - pages/
    - components/
    - services/
  - public/

- RESEARCH/
- README.md
- ROLES.md
- STYLE.md
- FILE_STRUCTURE.md

### 6. Walking Skeleton Plan

The Walking Skeleton will prove end-to-end integration between the **frontend**, **backend**, and **database** using a minimal feature.

1. The frontend will provide a way to **join as guest** (e.g., a “Play as Guest” button).
2. When the user joins as guest, the frontend sends a request to the backend to **create a temporary guest session**.
3. The backend creates the session, stores the guest session in the database, and returns a response confirming the session was created, including a **guest session ID**.
4. The database must be able to **store the guest session ID** (at minimum) so the backend can retrieve it and prove persistence.




