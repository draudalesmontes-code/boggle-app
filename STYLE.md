# Coding Style Guide

## Overview
This document outlines the coding style guidelines for our project. All team members must follow these conventions to ensure code consistency and maintainability.

**Note**: While we considered adopting the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), we decided to follow IntelliJ IDEA's default formatting standards, which align with broader Java community conventions (4-space indentation, 120-character line limit). This makes our codebase more familiar to Java developers and works seamlessly with IntelliJ's built-in formatting tools.

## Code Formatting

### IntelliJ Code Cleanup
**All code must be formatted using IntelliJ's Code Cleanup before committing or merging.**

To run Code Cleanup in IntelliJ:
- **Keyboard Shortcut**: `Ctrl + Alt + L` (Windows/Linux) or `Cmd + Option + L` (Mac)
- **Menu**: Code > Reformat Code
- **Before Commit**: Check "Reformat code" in the commit dialog

### Indentation
- Use **spaces**, not tabs
- Indentation size: **4 spaces**
- Continuation indent: **8 spaces**

### Line Length
- Maximum line length: **120 characters**
- Break long lines at logical points (after commas, before operators, etc.)

### Braces
- Use braces even for single-line blocks
- Opening brace on the same line as the statement
- Closing brace on a new line
```java
// Good
if (condition) {
    doSomething();
}

// Bad
if (condition)
    doSomething();
```

## Naming Conventions

### Classes and Interfaces
- Use **PascalCase**
- Class names should be nouns
- Interface names should describe behavior
```java
public class UserAccount { }
public interface Serializable { }
```

### Methods
- Use **camelCase**
- Method names should be verbs or verb phrases
- Should clearly describe what the method does
```java
public void calculateTotal() { }
public boolean isValid() { }
public String getUserName() { }
```

### Variables
- Use **camelCase**
- Use descriptive names, avoid single letters except for loop counters
- Boolean variables should use `is`, `has`, `can`, or `should` prefixes
```java
int studentCount;
boolean isActive;
boolean hasPermission;
```

### Constants
- Use **UPPER_SNAKE_CASE**
- Declared as `static final`
```java
public static final int MAX_CONNECTIONS = 100;
public static final String DEFAULT_USERNAME = "guest";
```

### Packages
- Use **lowercase**
- Use reverse domain notation
```java
package com.project.module;
```

## Code Organization

### Import Statements
- No wildcard imports (avoid `import java.util.*`)
- Remove unused imports
- IntelliJ will organize these automatically

### Class Structure
Order class members as follows:
1. Static variables (constants first)
2. Instance variables
3. Constructors
4. Methods (public methods first, then private)
5. Inner classes

### Method Length
- Keep methods focused and concise
- Ideal: under 20 lines
- Maximum: 50 lines (refactor if longer)

## Documentation

### JavaDoc Comments
- Required for all public classes, interfaces, and methods
- Use complete sentences with proper punctuation
- Include `@param`, `@return`, and `@throws` tags where applicable
```java
/**
 * Calculates the total price including tax.
 *
 * @param basePrice the base price before tax
 * @param taxRate the tax rate as a decimal (e.g., 0.08 for 8%)
 * @return the total price including tax
 * @throws IllegalArgumentException if basePrice or taxRate is negative
 */
public double calculateTotalPrice(double basePrice, double taxRate) {
    // implementation
}
```

### Inline Comments
- Use sparingly, only when code is not self-explanatory
- Explain *why*, not *what*
- Keep comments up to date with code changes

## Best Practices

### Variable Declaration
- Declare variables in the narrowest scope possible
- Initialize variables when declared when possible
- One variable declaration per line

### White Space
- One blank line between methods
- No trailing whitespace at end of lines
- Blank line at the end of file

### String Comparison
- Use `.equals()` for string comparison, not `==`
- Put constants on the left side to avoid `NullPointerException`
```java
// Good
if ("active".equals(status)) { }

// Bad
if (status == "active") { }
```

### Exception Handling
- Never catch exceptions without handling them
- Don't use empty catch blocks
- Always log or rethrow exceptions
```java
// Good
try {
    riskyOperation();
} catch (IOException e) {
    logger.error("Failed to perform operation", e);
    throw new RuntimeException("Operation failed", e);
}

// Bad
try {
    riskyOperation();
} catch (IOException e) {
    // silent failure
}
```

### Magic Numbers
- Avoid magic numbers in code
- Define constants with meaningful names
```java
// Good
private static final int MAX_RETRY_ATTEMPTS = 3;
if (attempts > MAX_RETRY_ATTEMPTS) { }

// Bad
if (attempts > 3) { }
```

## Version Control

### Branch Naming Convention

All branches must follow a consistent naming format to keep the repository organized and easy to navigate.

**Format:**
 <your_initials>/<short_description>

**Examples:**
- as/duplicate_word_prevention
- pp/backend_file_structure
- drm/fixing_springboot

**Guidelines:**
- Use your **initials in lowercase** as the prefix
- Use a **short, descriptive name** of the task or issue
- Separate words with **underscores**
- Keep names concise but clear
- Branch names should reflect the related issue or feature

**Best Practice:**
- Create branches from `main`
- One branch per issue or feature
- Delete branches after merge to keep the repo clean

### Commits
- Write clear, descriptive commit messages ("added feature")
- Reference issue numbers when applicable

**Commit Structure Guidelines:**
- Create **one commit per file changed or added** whenever possible
- If a single change is large (e.g., a long method or full feature implementation), it is acceptable to group it into one commit
- Avoid combining unrelated changes into a single commit
- Keep commits small and focused for easier code review and debugging

### Before Committing
**Required checklist:**
- [ ] Run IntelliJ Code Cleanup (`Ctrl + Alt + L` / `Cmd + Option + L`)
- [ ] Remove unused imports
- [ ] No commented-out code
- [ ] All tests pass
- [ ] No compiler warnings

### Code Reviews
- All code must be reviewed before merging
- Address all review comments
- Keep pull requests focused and reasonably sized

## IDE Configuration

### IntelliJ IDEA Settings
Ensure your IntelliJ is configured with:
- **Editor > Code Style > Java**: Set to project defaults
- **Editor > Inspections**: Enable all default inspections
- **Editor > General > Auto Import**: Optimize imports on the fly
- **Tools > Actions on Save**: Enable "Reformat code" (optional but recommended)

---
*Last Updated: Sprint 2*