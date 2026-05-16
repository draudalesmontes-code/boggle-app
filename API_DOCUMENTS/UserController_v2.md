# UserController

## Summary
`UserController` is the REST controller that manages user identity creation and authentication. It provides endpoints to:
- register a new account user
- login an existing account user
- create a guest user record (stored in the DB) so that games can reference the guest via `users.id`

The controller ensures input validity, enforces username/email uniqueness, delegates password hashing/verification, and returns a compact response DTO for frontend storage.

---

## Project-defined dependencies used (non-JDK, non-Spring)

### Entity / domain model
- `User`

### Repository
- `UserRepository`

### Security utility (project code)
- `PasswordUtil`
  - `PasswordUtil.hash(...)` (used during registration and guest creation)
  - `PasswordUtil.verify(...)` (used during login)

---

## Public endpoints (controller methods)

## `register(...)`
### Summary
Creates a new account user and returns a minimal user DTO.

### Inputs (DTO)
`RegisterRequest`
- `username` (required)
- `email` (required)
- `password` (required)

### What this method coordinates
- **Uniqueness enforcement**: checks both username and email before attempting insert
- **Password storage policy**: stores only a hash (never raw password)
- **Account classification**: explicitly marks the created user as *not* a guest (`setGuest(false)`)

### Output (DTO)
`UserResponse`:
- `id`
- `username`
- `email`
- `isGuest`

---

## `login(...)`
### Summary
Authenticates an account user by username/password.

### Inputs (DTO)
`LoginRequest`
- `username` (required)
- `password` (required)

### What this method coordinates
- **Identity lookup**: loads the `User` by username
- **Guest policy**: rejects logins for users marked as guests (guests do not authenticate with passwords)
- **Verification**: validates the supplied password using `PasswordUtil.verify(...)`

### Output (DTO)
`UserResponse` (same shape as register output)

---

## `guest(...)`
### Summary
Creates a guest user record and returns its `id`. This enables guest play while still satisfying DB foreign key requirements that games reference `users.id`.

### Inputs (DTO)
`GuestRequest` (optional)
- `username` (optional; blank triggers auto-generation)

### What this method coordinates
- **Name assignment**:
  - uses caller-provided username if non-blank
  - otherwise generates `Guest-<token>`
  - enforces uniqueness with `makeUniqueUsername(...)`
- **DB constraint satisfaction**:
  - generates a placeholder email (since schema requires NOT NULL email)
  - stores a hashed random value as `password_hash` (guests do not use it, but schema requires NOT NULL password_hash)
- **Guest classification**:
  - explicitly marks the created user as guest (`setGuest(true)`)

### Output (DTO)
`UserResponse`:
- `id`
- `username`
- `email`
- `isGuest`

---

## Internal DTOs and why they exist

## `RegisterRequest`, `LoginRequest`, `GuestRequest`
These request DTOs keep the REST contract explicit and stable, and prevent accidental coupling to the `User` entity’s internal fields.

## `UserResponse`
This response DTO deliberately omits sensitive fields (e.g., `passwordHash`) so the controller never risks exposing them in JSON.

---

## Helper methods (internal) and why they exist

## `require(String value, String field)`
Centralized required-field checking to keep error behavior consistent across endpoints.

## `safe(String s)`
Normalizes optional string inputs so the controller can treat “missing” and “blank” guest usernames uniformly.

## `makeUniqueUsername(String base)`
Resolves username collisions deterministically by appending random suffixes. This improves UX (one call yields a usable username) and reduces “try again” loops on the frontend.

## `shortToken()` and `generateGuestUsername()`
Generate short guest identifiers that are easier to display than full UUIDs while retaining enough randomness to avoid frequent collisions.
