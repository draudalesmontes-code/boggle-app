# Research Report

## PasswordUtil.java — PBKDF2WithHmacSHA256 Password Hashing Research

### Summary of Work

This research explains the method used in `PasswordUtil.java` to protect user passwords. The class does **not encrypt** passwords; instead, it **hashes** them using the algorithm name `PBKDF2WithHmacSHA256`, together with a random salt, an iteration count of `120_000`, and a derived key length of `256` bits. The class has three main responsibilities: generate a secure password hash in `hash(...)`, verify a login attempt in `verify(...)`, and derive the underlying key material in `pbkdf2(...)`. The goal of this research is to explain the method name, show the step-by-step logic of the hashing process with examples, explain why this approach is good for password storage, and point out its limitations as implemented in this class.

### Motivation

I needed to understand the exact password-protection strategy used by `PasswordUtil.java` so the documentation would use the correct security terminology and accurately describe the class. This mattered because the class is about **hashing and verification**, not reversible encryption. Since the code stores a formatted string containing the algorithm label, iteration count, salt, and derived key, I also needed to understand what each component means and why the implementation chose PBKDF2 with HMAC-SHA256. That research helps explain the logic of the class to teammates and makes the code easier to maintain.

### Time Spent

I spent time reading the three key methods in the class and tracing the data flow from a plain-text password to the stored hash string and then through the verification path. I also broke down the meaning of the constants `SALT_BYTES`, `ITERATIONS`, and `KEY_BITS`, and analyzed why the returned hash format is `pbkdf2:<iterations>:<salt>:<derivedKey>`. After that, I organized the findings into a step-by-step explanation with examples so the report could serve as a clear reference for future development.

### Results

#### Class information this research pertains to

This research pertains to the `PasswordUtil` class in `com.bogglespringboot.Security`. The class is declared `final`, which means it is not intended to be extended, and it has a private constructor, which prevents instantiation. That design indicates it is a **utility class** made only for static helper methods. Its documented purpose is to securely hash and verify passwords using PBKDF2 with HMAC-SHA256, a random salt, and a configurable iteration count. In the current implementation, the important class-level components are: `SecureRandom RNG`, `SALT_BYTES = 16`, `ITERATIONS = 120_000`, and `KEY_BITS = 256`. These values control how password hashes are generated and checked. The class behavior and constants are visible directly in the uploaded source file.[^1]

#### Name of the method used for encoding

The method used by this class is **PBKDF2WithHmacSHA256**.[^1] More precisely, this is a **password-based key derivation function**, not a general-purpose text encoding scheme and not reversible encryption. The name can be broken down like this:

- **PBKDF2** = Password-Based Key Derivation Function 2
- **HmacSHA256** = the pseudorandom function used internally by PBKDF2 is HMAC built on SHA-256

The class calls this method in `pbkdf2(...)` by asking `SecretKeyFactory` for `PBKDF2WithHmacSHA256`.[^1] The output of PBKDF2 is a derived byte array that depends on the password, the salt, the iteration count, and the target key length.

#### How the method works

At a high level, PBKDF2 takes a password and intentionally makes the computation expensive. Instead of hashing the password once, it repeatedly applies a keyed hash construction many times. The repeated work slows down attackers who try to guess many passwords offline. The salt ensures that two users with the same password do not automatically have the same stored hash. The derived key is then stored rather than the original password.

In this class, the hashing flow in `hash(String password)` is:

1. Reject invalid input. If the password is `null` or blank, the method throws `IllegalArgumentException`.[^1]
2. Generate a random salt. The class allocates `16` bytes and fills them with secure randomness using `SecureRandom`.[^1]
3. Derive a key with PBKDF2. The method converts the password to a character array and calls `pbkdf2(password.toCharArray(), salt, 120_000, 256)`.[^1]
4. Encode binary data for storage. The salt and derived key are converted to Base64 strings.[^1]
5. Return one formatted storage string: `pbkdf2:<iterations>:<salt>:<derivedKey>`.[^1]

That means the class stores enough information to verify later: the method label, the cost parameter, the salt, and the expected derived key.

#### Step-by-step logic with examples

##### Example 1: Hashing a new password

Suppose the user chooses the password:

`Falcon2026!`

The method does **not** store that text directly.

Step A: A random salt is generated. Since the salt is random, it could look like this in raw bytes:

`[A1, 4F, 92, ...]`

After Base64 encoding, it might look something like:

`oU+Sx3wz7J4Q2mN1d8KfLQ==`

Step B: PBKDF2 is run using:

- password = `Falcon2026!`
- salt = random 16 bytes
- iterations = `120000`
- key length = `256` bits

Step C: The resulting derived key bytes are also Base64-encoded, for example:

`YJcD2g7n1m9s8QfA0Q1YJY4m8x5U0n6v6WQwLr+2z2I=`

Step D: The class stores the final string in this format:

`pbkdf2:120000:oU+Sx3wz7J4Q2mN1d8KfLQ==:YJcD2g7n1m9s8QfA0Q1YJY4m8x5U0n6v6WQwLr+2z2I=`

The exact salt and derived key will be different every time because the salt is random.

##### Example 2: Why the salt matters

Assume two users both pick the password:

`Badger123!`

Without a salt, both stored hashes would be identical, which would reveal that the users share the same password. With this class, each user gets a different random salt, so the resulting stored strings are different even if the original password is the same.

User A might store:

`pbkdf2:120000:saltA:hashA`

User B might store:

`pbkdf2:120000:saltB:hashB`

Because `saltA != saltB`, the derived keys also differ.

##### Example 3: Verification flow

When a user logs in, `verify(String password, String stored)` does the following:

1. Reject `null` input immediately by returning `false`.[^1]
2. Split the stored string using `:`.[^1]
3. Make sure it has exactly four parts and begins with the label `pbkdf2`.[^1]
4. Parse the iteration count from the second part.[^1]
5. Base64-decode the salt and expected derived key from the stored string.[^1]
6. Re-run PBKDF2 on the login attempt using the **same salt** and **same iteration count**.[^1]
7. Compare the newly derived bytes with the stored expected bytes using `MessageDigest.isEqual(...)`.[^1]

If the two derived keys match, the password is correct; otherwise, verification fails.

So if the user types `Falcon2026!` again, the same salt and same parameters produce the same derived key, and the comparison succeeds. If the user types `Falcon2027!`, the derived key changes and the comparison fails.

#### Logic behind the algorithm

The logic behind PBKDF2 is to turn a password, which may be weak or human-memorable, into a stronger derived value that is expensive to recompute at scale. It does this by combining four inputs:

- the password
- a random salt
- an iteration count
- a target output length

PBKDF2 repeatedly applies a pseudorandom function based on HMAC-SHA256. The repeated application is intentional: it raises the cost of each password guess. This is useful because an attacker who steals the password database must test guesses one by one. If each guess is slower, large-scale brute-force attacks become more expensive.

The salt solves a different problem. It prevents precomputed lookup attacks, such as rainbow tables, and ensures identical passwords do not produce identical stored values across accounts. The output length lets the system request a fixed-size derived key, in this case 256 bits. In short, the algorithm is designed around **slowing down guessing** and **removing predictable patterns**.

#### Reasons why this method is good

This method is good for several reasons.

First, it is appropriate for password storage. The class never stores the original password, only a derived value.[^1] That means a database leak does not immediately expose plain-text passwords.

Second, the implementation uses a **random salt** generated with `SecureRandom`, which is strong practice because it makes identical passwords hash differently.[^1]

Third, the algorithm uses an intentionally high work factor with `120_000` iterations.[^1] That increases the cost of offline guessing attacks.

Fourth, the class stores the iteration count alongside the hash.[^1] That makes the format self-describing and easier to evolve later.

Fifth, the verify path uses `MessageDigest.isEqual(...)`, which is better than a naive equality check because it is intended for secure byte comparison.[^1]

Finally, PBKDF2 is widely supported in Java through standard cryptographic libraries, so it is practical, portable, and easier for teams to maintain than a custom scheme.

#### Reasons why this method is bad or limited

The method is solid, but it has limitations.

First, PBKDF2 is generally considered older than modern password-hashing algorithms like Argon2 or scrypt. Those alternatives are designed to be more resistant to GPU and specialized hardware attacks because they are more memory-hard. PBKDF2 mainly increases CPU cost, so attackers with optimized hardware may still test guesses faster than desired.

Second, the implementation uses fixed global settings inside the class. That is simple, but it means changing cost parameters later requires a conscious migration plan.

Third, the class uses a generic `catch (Exception e)` in both hashing and verification.[^1] That keeps the interface simple, but it hides the exact cause of failures.

Fourth, the code does not explicitly clear the password character array after derivation. Since it converts the string into a `char[]`, some security-sensitive codebases would wipe that array after use, even though Java still has other limitations around secret handling.

Fifth, the report should note a terminology limitation: calling this process “encoding” would be inaccurate. The class is really performing **password hashing with key derivation**, not ordinary encoding and not reversible encryption.

#### Final takeaway

`PasswordUtil` is a utility class whose security design is based on PBKDF2 with HMAC-SHA256, a random 16-byte salt, 120,000 iterations, and a 256-bit derived key.[^1] Its logic is: generate salt, derive a key from the password, store a structured string, and later verify by recreating the derived key from the login attempt. This is good because it is standard, salted, and intentionally slow. Its main weakness is that PBKDF2 is no longer the most attack-resistant password hashing choice compared with newer memory-hard alternatives.

### Sources

- Uploaded `PasswordUtil.java` source file used for class-specific analysis and implementation details.
- Oracle Java documentation for `SecretKeyFactory`, relevant to the `PBKDF2WithHmacSHA256` implementation approach.[^1]
- OWASP Password Storage Cheat Sheet for best practices and comparison to modern password-hashing recommendations.[^2]
- NIST Digital Identity Guidelines for general password and verifier guidance.[^3]

[^1]: [Oracle Java documentation for `SecretKeyFactory` and standard JCA usage.](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/javax/crypto/SecretKeyFactory.html?utm_source=chatgpt.com)  

[^2]: [OWASP Password Storage Cheat Sheet.](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) 

[^3]: [NIST SP 800-63B Digital Identity Guidelines.](https://pages.nist.gov/800-63-4/sp800-63b.html)
