# Research Report

## Java Coding Style Guidelines

### Summary of Work

I researched Java coding conventions and style guidelines to create our team's STYLE.md file. This included investigating IntelliJ IDEA code cleanup features, the Google Java Style Guide, Oracle's Java conventions, and general best practices for code formatting and documentation.

### Motivation

Our team needs a STYLE.md file as part of Sprint 0 deliverables. We agreed as a team to use IntelliJ's Code Cleanup before all commits, so I needed to research what standards that enforces and what additional guidelines we should include to ensure consistent, professional code across our project.

### Time Spent

- ~20 minutes researching IntelliJ IDEA Code Cleanup features and shortcuts
- ~25 minutes reviewing the Google Java Style Guide and Oracle conventions
- ~15 minutes investigating best practices for naming, documentation, and code organization
- ~15 minutes compiling everything into the STYLE.md file
- Total: ~75 minutes (1 hour 15 minutes)

### Results

I investigated several style guides and tools to determine what standards work best for our team:

**IntelliJ IDEA Code Cleanup**: I researched IntelliJ's built-in formatting features[^1]. It automatically handles indentation (4 spaces), organizes imports, formats braces (K&R style), and enforces line length limits (120 characters by default). The keyboard shortcut is `Ctrl + Alt + L` on Windows/Linux or `Cmd + Option + L` on Mac.

**Google Java Style Guide**: I reviewed Google's comprehensive style guide[^2], which uses 2-space indentation and 100-character line limits. While very detailed, I determined it was too strict for our team's Sprint 0 needs. We decided to use IntelliJ's defaults (4 spaces, 120 characters) instead, which aligns better with standard Java community practices.

**Naming Conventions**: I compiled standard Java naming rules from Oracle[^3] and Google[^2]:
- Classes: PascalCase (e.g., `UserAccount`)
- Methods and variables: camelCase (e.g., `getUserName()`, `studentCount`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_CONNECTIONS`)
- Packages: lowercase (e.g., `com.project.module`)

**Best Practices**: I researched common Java best practices[^4] including never using empty catch blocks, using `.equals()` for string comparison, avoiding magic numbers, and requiring JavaDoc for all public methods.

Based on this research, I created our STYLE.md file with IntelliJ code cleanup instructions, naming conventions, formatting rules, documentation standards, and a pre-commit checklist. The file is structured as a quick reference guide that team members can easily consult during development.

### Sources

- IntelliJ IDEA Code Formatting Documentation[^1]
- Google Java Style Guide[^2]
- Oracle Code Conventions for Java[^3]
- Java Clean Code Best Practices[^4]

[^1]: https://www.jetbrains.com/help/idea/reformat-and-rearrange-code.html
[^2]: https://google.github.io/styleguide/javaguide.html
[^3]: https://www.oracle.com/java/technologies/javase/codeconventions-contents.html
[^4]: https://www.baeldung.com/java-clean-code