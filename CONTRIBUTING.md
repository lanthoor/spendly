# Contributing to Spendly

Thanks for your interest in contributing.

## Before You Start

- Read the public documentation: https://lanthoor.dev/spendly
- Open an issue for significant changes before sending a pull request.
- Keep pull requests focused and easy to review.

## Development Setup

- Use Android Studio (latest stable) with Android SDK installed.
- Clone the repository and open it as a Gradle project.
- Run checks before opening a PR:

```bash
./gradlew build
./gradlew test
./gradlew lint
```

Optional commands:

```bash
./gradlew installDebug
./gradlew connectedAndroidTest
```

## Commit and PR Guidelines

- Use Conventional Commits (for example: `feat: ...`, `fix: ...`, `docs: ...`).
- Use a Conventional Commit formatted PR title as well (for example: `feat: ...`, `fix: ...`, `chore(architecture): ...`).
- Include tests for behavior changes when possible.
- Update user-facing docs when behavior changes.
- PR description should explain:
  - why the change is needed
  - what changed
  - how it was tested

## Code Style

- Follow existing project patterns and naming conventions.
- Keep changes minimal and avoid unrelated refactors.
- Prefer clear, readable code over clever code.

## License and Copyleft

By contributing, you agree that your contributions are licensed under
the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).

If you distribute modified versions, or run modified versions for users
over a network, you must provide corresponding source code under the same
license terms.
