# GameController

## Summary
`GameController` is the REST controller responsible for creating and reading `Game` data, as well as retrieving the `Board` associated with a game. It supports three game creation modes—**SOLO**, **BOT**, and **MULTIPLAYER**—and centralizes the rules for:
- validating game creation input
- resolving players (by user id or by session usernames)
- generating and persisting a new board
- persisting the game and returning a minimal response DTO for the frontend

---

## Project-defined dependencies used (non-JDK, non-Spring)

### Entities / domain models
- `Board`
- `Game`
- `GameStatus`
- `User`

### Repositories
- `BoardRepository`
- `GameRepository`
- `FoundWordRepository` (injected; not used directly in current endpoints)
- `SessionRepository`
- `UserRepository`

### Session + utilities
- `Session` (project session object used to obtain multiplayer usernames)
- `ShuffleUtil` (board generation)

---

## Public endpoints (controller methods)

## `createGame(...)`
### Summary
Creates a new game in one of the supported modes and returns a compact JSON summary (`GameResponse`) suitable for immediate frontend use.

### Inputs (DTO)
`CreateGameRequest`
- `mode` (required): `SOLO | BOT | MULTIPLAYER` (enum `GameMode`)
- `playerId` (required for `SOLO` and `BOT`)
- `sessionCode` (required for `MULTIPLAYER`)

### What this method coordinates
- **Mode-driven player resolution**
  - `SOLO`: requires a real `User` for `playerId`
  - `BOT`: requires a real `User` for `playerId` and ensures a bot `User` exists
  - `MULTIPLAYER`: requires a valid session and resolves *two* usernames from the session into `User` rows
- **Board lifecycle**
  - Generates a fresh board, persists it, and attaches it to the new game
- **Game lifecycle**
  - Creates the game, sets `status = IN_PROGRESS`, sets server-side `startedAt`, persists it

### Output (DTO)
`GameResponse` contains:
- `gameId`
- `player1Id`
- `player2Id` (nullable in SOLO)
- `boardId`
- `status` (string enum)

---

## `getGame(...)`
### Summary
Returns a `GameResponse` for a given `gameId`. This is designed as a lightweight “game header” fetch rather than a full game state dump.

### Output (DTO)
`GameResponse` (same shape as `createGame` response)

---

## `getBoard(...)`
### Summary
Returns the `BoardResponse` for the board attached to a game. This provides the minimum board data needed for UI rendering.

### Output (DTO)
`BoardResponse` contains:
- `boardId`
- `boardString` (flattened board representation)

---

## `getBoardSample(...)`
### Summary
Creates and persists a board without creating a game, then returns `BoardResponse`. This endpoint is useful for frontend development/testing (board UI) without requiring a full game flow.

---

## Internal DTOs and why they exist

## `CreateGameRequest`
Provides a stable JSON contract for game creation. Using an enum (`GameMode`) ensures invalid modes are detected early and makes the backend the source of truth for supported modes.

## `GameResponse`
Centralizes “what the frontend needs immediately after game creation” and avoids leaking internal entity shape (JPA fields/relationships). It also normalizes enum state into a simple string.

## `BoardResponse`
Returns only what the frontend needs to render a board, avoiding accidental coupling to the full `Board` entity.

---

## Helper methods (internal) and why they exist

## `createAndSaveBoard()`
Creates a new board by calling `ShuffleUtil.shuffle_board()` and persists it via `BoardRepository`. Keeping this as a dedicated helper prevents subtle drift where different endpoints generate boards differently or forget to persist them.

## `require(String value, String field)`
A single consistent place to enforce non-empty string inputs. This keeps client-facing errors predictable and keeps “bad request” handling out of core logic paths.

## `requireUser(Integer userId, String fieldName)`
Enforces that id-based game modes only proceed if the referenced `User` actually exists. This is important because the DB schema uses foreign keys from games to users; letting a request proceed with a “phantom id” would create inconsistent behavior.

## `getOrCreateBot()`
Implements the policy that BOT games should “just work” without requiring the frontend to pre-provision a bot user. This method makes BOT mode a one-call flow and keeps the bot identity consistent across games.
