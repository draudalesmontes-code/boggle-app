# Frontend API — Games

## Quick links: How to make HTTP requests
- Fetch (MDN): https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch
- Axios: https://axios-http.com/docs/intro

---

## Base URL
`/api/game`

---

## Error response format
Spring returns standard error responses for validation and missing-resource errors. A typical example looks like:

```json
{
  "timestamp": "2026-03-16T12:34:56.789+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game id not found",
  "path": "/api/game/123"
}
```

---

## Supported game modes
The backend supports these game modes:

- `SOLO`
- `BOT`
- `MULTIPLAYER`

These are the values accepted by the `mode` field when creating a game.

---

## Create game

### API call
`POST /api/game`

### Request body
```json
{
  "mode": "SOLO | BOT | MULTIPLAYER",
  "playerId": 12
}
```

### Notes
- `playerId` is required by the controller for all create requests.
- `mode` is required.
- The controller passes `mode` and `playerId` to `gameService.createGame(...)`.

### Example request
```http
POST /api/game
Content-Type: application/json
```

```json
{
  "mode": "SOLO",
  "playerId": 12
}
```

### Success response
**Status:** `201 Created`

```json
{
  "gameId": 50,
  "player1Id": 12,
  "player2Id": null,
  "boardId": "0df1b1b4-9b0e-4f53-bb5a-2c84f2a4a4e7",
  "status": "IN_PROGRESS",
  "createdAt": "2026-03-16T15:00:00",
  "startedAt": "2026-03-16T15:00:00",
  "finishedAt": null
}
```

### Validation / failures
**400 Bad Request**
- `"Body is required"`
- `"playerId is required"`
- `"Mode is required"`

Other service-level errors may also be returned depending on invalid player or invalid game creation conditions.

---

## Join existing game

### API call
`POST /api/game/{gameId}/join`

### Request body
```json
{
  "playerId": 34
}
```

### Notes
- This is the current multiplayer join flow.
- `playerId` is required in the body.
- The controller delegates to `gameService.joinGame(gameId, request.playerId)`.

### Example request
```http
POST /api/game/50/join
Content-Type: application/json
```

```json
{
  "playerId": 34
}
```

### Success response
**Status:** `200 OK`

```json
{
  "gameId": 50,
  "player1Id": 12,
  "player2Id": 34,
  "boardId": "0df1b1b4-9b0e-4f53-bb5a-2c84f2a4a4e7",
  "status": "IN_PROGRESS",
  "createdAt": "2026-03-16T15:00:00",
  "startedAt": "2026-03-16T15:05:00",
  "finishedAt": null
}
```

### Validation / failures
**400 Bad Request**
- `"Body is required"`
- `"playerId is required"`

Other service-level errors may be returned if the game does not exist, is full, or cannot be joined.

---

## Get game summary

### API call
`GET /api/game/{gameId}`

### Example request
```http
GET /api/game/50
```

### Success response
**Status:** `200 OK`

```json
{
  "gameId": 50,
  "player1Id": 12,
  "player2Id": 34,
  "boardId": "0df1b1b4-9b0e-4f53-bb5a-2c84f2a4a4e7",
  "status": "IN_PROGRESS",
  "createdAt": "2026-03-16T15:00:00",
  "startedAt": "2026-03-16T15:05:00",
  "finishedAt": null
}
```

### Failures
**404 Not Found**
- `"Game id not found"`

---

## Get board for a game

### API call
`GET /api/game/{gameId}/board`

### Example request
```http
GET /api/game/50/board
```

### Success response
**Status:** `200 OK`

```json
{
  "boardId": "0df1b1b4-9b0e-4f53-bb5a-2c84f2a4a4e7",
  "boardString": "ABCDEFGHIJKLMNOP"
}
```

### Notes
- The controller delegates to `gameService.getBoard(gameId)`.
- `BoardResponse` includes only `boardId` and `boardString`.

---

## Submit a word

### API call
`POST /api/game/{gameId}/submit-word`

### Request body
```json
{
  "playerId": 12,
  "word": "apple"
}
```

### Notes
- `playerId` is required.
- `word` is required.
- The controller delegates to `wordSubmissionService.submitWord(gameId, request.playerId, request.word)`.

### Example request
```http
POST /api/game/50/submit-word
Content-Type: application/json
```

```json
{
  "playerId": 12,
  "word": "apple"
}
```

### Success response
**Status:** `200 OK`

```json
{
  "accepted": true,
  "reason": "ACCEPTED",
  "normalizedWord": "APPLE",
  "points": 2
}
```

### Validation / failures
**400 Bad Request**
- `"Body is required"`
- `"playerId is required"`
- `"word is required"`

### Notes on response fields
- `accepted` is a boolean.
- `reason` is the enum name from the backend result.
- `normalizedWord` is the normalized word returned by the service.
- `points` is the awarded score for that submission.

---

## Get current score totals

### API call
`GET /api/game/{gameId}/score`

### Example request
```http
GET /api/game/50/score
```

### Success response
**Status:** `200 OK`

The response is whatever `GameScoreService.Totals` returns. The controller returns that object directly. Example shape may look like:

```json
{
  "player1Points": 10,
  "player2Points": 7,
  "leaderPlayerId": 12,
  "isTie": false
}
```

### Notes
The exact JSON fields depend on the `GameScoreService.Totals` class definition. The controller does not wrap or rename them.

---

## Finish game

### API call
`POST /api/game/{gameId}/finish`

### Example request
```http
POST /api/game/50/finish
```

### Success response
**Status:** `200 OK`

The response is whatever `GameScoreService.Totals` returns after the game is finalized. Example shape may look like:

```json
{
  "player1Points": 14,
  "player2Points": 11,
  "leaderPlayerId": 12,
  "isTie": false
}
```

### Notes
- The controller delegates to `gameScoreService.finishGame(gameId)`.
- This endpoint finalizes the game and returns final totals.

---

## Frontend implementation notes

- Use the Vite proxy and call relative paths such as `/api/game` instead of hardcoding `http://localhost:8080`.
- `player2Id`, `boardId`, `startedAt`, and `finishedAt` may be `null` depending on game state.
- `createdAt`, `startedAt`, and `finishedAt` are backend-generated timestamps.
- For non-2xx responses, check `response.status` explicitly in `fetch`, since `fetch` does not throw automatically for HTTP 400/401/404 responses.

---

# AI Prompt used

**User prompt:**
> I’m documenting our Boggle backend for frontend developers. I’m going to upload `GameController.java` and an older `GAME_API_ENDPOINTS.md`. Please refactor the markdown document so it matches the controller exactly.  
>  
> Requirements:
> - Keep it frontend-facing and easy to read
> - Use the real endpoint paths and request/response fields from the controller
> - Remove anything that no longer exists
> - Add any missing endpoints from the controller
> - Include validation errors the controller explicitly throws
> - Keep the structure consistent across endpoints: API call, request body, example request, success response, failures/notes
> - Use markdown only
> - Do not invent fields unless the controller clearly returns them
> - If a service return shape is not fully visible from the controller, say that the exact JSON depends on the returned DTO/class
>  
> Please output the fully rewritten `GAME_API_ENDPOINTS.md`.

**Assistant response:**
> Sure — I’ll align the endpoint document directly to `GameController.java`, preserve a frontend-oriented structure, remove outdated fields like `sessionCode`, and add the missing endpoints such as `join`, `submit-word`, `score`, and `finish`. Then I’ll give you a clean markdown file you can paste into your repo.

**User prompt:**
> Also make sure the game summary response includes the timestamp fields if they are present in the controller DTO, and mention where fields may be null depending on game state.

**Assistant response:**
> Understood. I’ll include `createdAt`, `startedAt`, and `finishedAt` in the game summary response, and I’ll call out nullable fields like `player2Id`, `boardId`, and unfinished timestamps where appropriate.
