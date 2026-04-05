# AGENTS.md

This file contains guidelines for agentic coding agents operating in this Spendly repository.

## Build, Lint, and Test Commands

### Build Commands
```bash
# Build the project
./gradlew build

# Install debug build on device
./gradlew installDebug

# Generate release AAB
./gradlew bundleRelease

# Run lint checks
./gradlew lint
```

### Test Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate test coverage report (unit tests only)
./gradlew jacocoTestReport

# Run a single test class
./gradlew test --tests "dev.lanthoor.spendly.utils.SmsParserTest"

# Run a single test method
./gradlew test --tests "dev.lanthoor.spendly.utils.SmsParserTest.testParseHdfcSms"
```

## Code Style Guidelines

## Commit and PR Title Conventions

- Use Conventional Commit format for commit messages (e.g., `feat: ...`, `fix: ...`, `docs: ...`, `chore: ...`).
- Use Conventional Commit format for pull request titles as well.
- For architecture migration phases, prefer: `chore(architecture): phase <n> <scope>`.

### Kotlin Conventions
- Use `val` for immutable variables, `var` for mutable ones
- Prefer `private` visibility over `internal` when possible
- Use `@HiltViewModel` for ViewModels, `@Inject constructor()` for dependency injection
- Use `@HiltWorker` for WorkManager workers
- Follow standard Kotlin naming conventions: PascalCase for classes, camelCase for functions/variables
- Use `Long` for all monetary amounts (paise) to avoid floating-point precision issues
- Use `Dispatchers.IO` for all I/O operations

### Imports and Formatting
- Organize imports with standard Kotlin import ordering (Java standard, then Android, then project-specific)
- Use 4-space indentation (not tabs)
- Prefer explicit type declarations when it improves readability
- Use `@file:JvmName` annotation when needed for Java interoperability
- Align parameters in multi-line function calls

### Naming Conventions
- Use PascalCase for class and interface names
- Use camelCase for functions and variables
- Use ALL_CAPS for constants
- Prefix UI components with `Ui` (e.g., `UiState`, `UiEvent`)
- Use descriptive names that explain intent over implementation

### Error Handling
- Use Kotlin's built-in null safety (`?`, `!!`, `let`, `run`)
- Prefer `sealed classes` for error handling where appropriate
- Use `try-catch` blocks for operations that might fail
- Avoid using `throw` for control flow

### Architecture Patterns
- Follow MVVM + Clean Architecture with clear separation of concerns
- Repository pattern: Data layer abstracts data sources
- Flow-based reactive streams using Kotlin Flow
- State hoisting in ViewModels (expose StateFlow<UiState>)
- Use `@HiltViewModel` and `@Inject constructor()` for dependency injection
- Separate Room entities from domain models
- All data flows are reactive using Kotlin Flow
