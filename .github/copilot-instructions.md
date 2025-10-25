# jfmt Code Style Guide

This file contains instructions for GitHub Copilot and AI assistants working on the jfmt codebase.

For human developers, see [CONTRIBUTING.adoc](../CONTRIBUTING.adoc).

## General Principles

### No `else` Keyword
- **Never use the `else` keyword** in this codebase
- Use guard statements (early returns) instead
- Extract methods if needed to avoid `else` blocks
- This applies to all code: production code, tests, and utilities

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
