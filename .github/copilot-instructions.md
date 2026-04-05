# Copilot Instructions - Spendly

## Project
**Android expense tracker** (Kotlin + Jetpack Compose, offline-only)
- Package: `dev.lanthoor.spendly`
- Version: 0.9.2-beta (versionCode 92), DB v4
- Min/Target SDK: 31/36

## Tech Stack
- **UI:** Compose + Material 3 Adaptive Navigation
- **DB:** Room
- **DI:** Hilt
- **Async:** WorkManager + Hilt Work
- **Charts:** Custom Canvas (Pie/Line with tap interactions)
- **Icons:** Phosphor Icons

## Architecture
**MVVM + Clean Architecture**
- `data/`: Room entities, DAOs, repos (implementations)
- `domain/`: Models, repo interfaces
- `ui/`: ViewModels, Compose screens/components
- `di/`: Hilt modules
- `utils/`: Helpers (Currency, File, Image, SMS parsing)

## Key Constraints
- **Offline-only** (no network/cloud/bank integration)
- **Currency:** INR only, stored as Long (paise) for precision
- **Amounts:** Always Long (paise), never floating-point
- **All I/O:** Use `Dispatchers.IO`

## Database
- Version 4 (adds SMS timestamp indexes for dedup/history scan performance)
- NO destructive migrations - always preserve data
- Proper indexes on foreign keys

## Development
- **Build:** `./gradlew build test lint connectedAndroidTest`
- never commit changes
- always touch minimal files, no need to update README, CHANGELOG, etc. on each interaction. explicit instructions will be given if needed.

## Commit and PR Title Conventions
- Use Conventional Commit format for commit messages.
- Use Conventional Commit format for pull request titles as well.
- For architecture migration phases, prefer: `chore(architecture): phase <n> <scope>`.

## Code Patterns
- Flow-based reactive queries
- State hoisted to ViewModels
- Modal bottom sheets for add/edit
- Theme-aware colors with `adjustForTheme()`
- Generic reusable components

## Never Implement
❌ Bank integration, cloud sync, bill splitting, multi-currency, iOS version
