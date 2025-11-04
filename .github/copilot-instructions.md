# jfmt Code Style Guide

This file contains instructions for GitHub Copilot and AI assistants working on the jfmt codebase.

For human developers, see [CONTRIBUTING.adoc](../CONTRIBUTING.adoc).

## General Principles

### No `else` Keyword
- **Do not use the `else` keyword** in this codebase.  
Code should use guard clauses and early returns instead of if–else or nested conditionals.
- Sequential if returns are allowed.  
You may use multiple if statements in a row if each one returns or exits early.
This pattern is acceptable:
```java
if (conditionA) return A;
if (conditionB) return B;
return C;
```
- Avoid indentation pyramids.  
Prefer one level of indentation per method. If you find yourself nesting ifs, extract a method or use early returns.
- Goal: Flatten control flow, not forbid normal branching logic.  
The rule targets keyword usage (else), not the logical pattern of mutually exclusive if branches.

```java
// ❌ Bad
if (isVerbose()) {
    return VERBOSE;
} else if (isSilent()) {
    return SILENT;
} else {
    return DEFAULT;
}

// ✅ Good
if (isVerbose()) return VERBOSE;
if (isSilent()) return SILENT;
return DEFAULT;
```

### Code Organization
- Keep methods short and focused (easily comprehensible by human brain)
- Follow the "given-when-then" pattern in tests:
  - `// given` - setup test data
  - `// when` - execute the code under test
  - `// then` - verify the results

### DRY Principle
- Do not repeat yourself - extract common code to utility classes or methods
- Consider creating JUnit extensions for common test patterns
- Comment about future improvements where appropriate

### Test Code Quality
- Remove all debug output (System.out.println, etc.) from tests
- Keep test methods concise and readable
- Use meaningful variable names
- Extract helper methods for complex operations

### Comments
- Add comments explaining non-obvious design decisions
- Document why a particular approach was chosen over alternatives
- Use JavaDoc for public APIs

## Build Commands for AI Assistants

When making code changes, use these commands:

```bash
# Apply code style (always run before compiling or committing)
./mvnw spotless:apply

# Run CLI module tests (fast, no integration tests)
./mvnw -am -pl cli test

# Run jfmt locally after building
./cli/target/jreleaser/assemble/jfmt/java-archive/work/jfmt-*/bin/jfmt --help
```
