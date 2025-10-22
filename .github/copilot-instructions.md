# jfmt Code Style Guide

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
