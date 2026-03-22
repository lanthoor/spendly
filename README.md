# Spendly - Personal Expense Tracker

[![Android CI](https://github.com/lanthoor/spendly/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/lanthoor/spendly/actions/workflows/android.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=lanthoor_spendly&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lanthoor_spendly)

**Version:** 0.9.0-beta (versionCode 90)

A native Android expense tracker built with Kotlin and Jetpack Compose. Offline-only architecture with local data persistence.

## Core Features

### 1. Expense Management ✅
- Add, edit, and delete expense entries
- Amount stored as paise (Long) for zero precision loss
- Categories (optional, defaults to Others), date, description, account
- **Accounts:** Customizable with types: Bank, Card, Wallet, Cash, Loan, Investment
- **Default account:** "My Account" (Bank type)
- Recurring expenses (daily, weekly, monthly) - processed at app startup
- Attach unlimited receipts (JPG, PNG, WebP, PDF) - compressed to 1920px, 5MB per file
- SMS auto-detection from Indian banks + UPI + credit cards
- Default sort: Newest first

### 2. Income Tracking ✅
- Record income with categories and accounts
- Track salary, freelance, investments, refunds (linked to expenses)
- Recurring income support
- SMS auto-detection for income

### 3. Categories & Tags ✅
- **19 unified categories:** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Media, Healthcare, Gifts, Education, Investments, Groceries, Salary, Freelance, Business, Refund, Interest, Bonus, Others
- Unified system removes expense/income distinction for cleaner organization
- Each category has Phosphor Icon and customizable color
- Category deletion requires reassignment

### 4. Budget Management ✅
- Set monthly budgets (per category or overall)
- Budget vs. actual spending comparison
- Visual progress bars with color coding
- **Overspending notifications:** Alerts at 75% and 100% thresholds
- Background monitoring (WorkManager every 6 hours)

### 5. Analytics & Insights ✅
- Dashboard with month/year selector for historical data viewing
- Financial summary card with Full Year and monthly comparisons
- Calendar Year (Jan-Dec) and Financial Year (Apr-Mar) toggle
- Recent 5 transactions widget
- **Custom Canvas-based analytics charts:**
  - Interactive pie chart (donut style) for category spending breakdown with tap-to-show-details
  - Line chart for income/expense trends over time with tap interactions
  - Time period selection: Current Month / Financial Year / Calendar Year
  - Category breakdown with percentages and transaction counts
  - Income/Expense/Net Worth trend visualization
  - 60 FPS rendering with smooth animations (600ms pie, 800ms line)
  - Full TalkBack accessibility support
  - 34 automated tests ensuring reliability (13 unit + 21 instrumented)
- **Pending:** PDF/CSV export

### 6. Data Management ✅
- Local SQLite database with Room
- **Currency:** INR only (₹)
- Database migrations enabled (v3 with SMS timestamp fix)
- **Import/Export:** Complete JSON backup and restore
  - Export all data: transactions, receipts, budgets, recurring transactions, categories, accounts
  - Import with validation and progress tracking
  - Receipt files included as Base64 in export for portability
  - 500MB file size limit
- **Pending:** CSV export, PDF reports

### 7. User Interface ✅
- **Dashboard:** Landing screen with overview and widgets
- **4-item navigation:** Home, Transactions, Analytics, Settings
- **Theme:** Light, Dark, or System Default (persistent preference)
- Material 3 Adaptive Navigation Suite (responsive layouts)
- Modal bottom sheets for add/edit flows
- Search and filter functionality (date range, categories, accounts)

### 8. Security ✅
- **App Lock:** Biometric authentication (fingerprint/face unlock)
- **Lock Timeout:** Configurable (Immediately, 1min, 5min, 15min)
- **Lifecycle-aware:** Automatically locks based on time spent in background
- **Lock Screen:** Full-screen overlay with automatic biometric prompt

## Project Scope

### In Scope ✅
- Android-first native mobile application
- Offline-only architecture
- Local data persistence
- Responsive UI (phones and tablets)
- Budget notifications
- SMS auto-detection (Indian banks + UPI)
- GitHub Actions CI/CD (build, test, lint)

### Out of Scope ❌
- Bank account integration/auto-import
- Cloud synchronization
- Bill splitting
- Investment portfolio tracking
- iOS version

## Technical Stack

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room v2.6.1 (SQLite wrapper)
- **Charts:** Vico v2.0.0-alpha.28
- **Navigation:** Material 3 Adaptive Navigation Suite
- **Architecture:** MVVM with ViewModel and StateFlow
- **Dependency Injection:** Hilt v2.51.1
- **Background Tasks:** WorkManager v2.9.0 + Hilt Work v1.1.0
- **Image Loading:** Coil v2.5.0
- **Camera:** CameraX v1.5.2
- **Icons:** Phosphor Icons v1.0.0
- **CI/CD:** GitHub Actions

## Development Status

**Current Version:** 0.9.0-beta (code 90)
**Status:** Beta - Core features complete with import/export

**Completed Phases:**
- ✅ Foundation & Infrastructure (Phases 1-3)
- ✅ Core UI & Features (Phases 4-6)
- ✅ Theme, Settings & Polish (Phases 7-7.5)
- ✅ Advanced Features (Phases 8-10)
- ✅ App Lock & Initialization (Phases 10.5-10.6)
- ✅ Analytics & Insights (Phase 11) - Custom Canvas charts
- ✅ Data Export/Import (Phase 13) - JSON backup & restore

**Next:** Calendar view (Phase 12)

See [PLAN.md](PLAN.md) for detailed roadmap.

## Database

**Version:** 2 (unified categories, established 2025-12-16)
**Entities:** 7 (Category, Expense, Receipt, Income, Budget, RecurringTransaction, Account)
**Strategy:** Proper migrations enabled - see [MIGRATIONS.md](MIGRATIONS.md) for guide

## Build Commands

```bash
# Build
./gradlew build

# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint

# Install debug
./gradlew installDebug

# Generate release AAB
./gradlew bundleRelease
```

## Commit Message Conventions

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for clear, organized commit history.

### Commit Format

```
<type>: <description>

[optional body]

[optional footer]
```

### Commit Types

| Commit Type | Purpose | Example |
|-------------|---------|---------|
| `feat:` | New features | `feat: add SMS auto-detection` |
| `fix:` | Bug fixes | `fix: resolve crash on startup` |
| `docs:` | Documentation changes | `docs: update installation guide` |
| `chore:` | Maintenance tasks | `chore: update dependencies` |
| `style:` | Code formatting | `style: format code` |
| `refactor:` | Code restructuring | `refactor: simplify database queries` |
| `perf:` | Performance improvements | `perf: optimize image loading` |
| `test:` | Test additions/changes | `test: add unit tests for parser` |
| `build:` | Build system changes | `build: update gradle version` |
| `ci:` | CI/CD changes | `ci: fix workflow syntax` |

### Examples

```bash
# Feature addition
git commit -m "feat: add budget notifications"

# Bug fix
git commit -m "fix: resolve null pointer in expense list"

# Documentation update
git commit -m "docs: add release instructions to README"

# Maintenance
git commit -m "chore: bump version to 0.5.0 (code 30) [skip ci]"
```

**Note:** This project follows Conventional Commits for clarity. CHANGELOG.md is maintained manually - update it before creating releases.

## Creating Releases

### Quick Steps

```bash
# Using helper script (recommended)
./scripts/release.sh 0.5.0

# Or manually:
git pull origin main
# Edit app/build.gradle.kts (update versionName and versionCode)
git add app/build.gradle.kts
git commit -m "chore: bump version to 0.5.0"
git push origin main
git tag v0.5.0
git push origin v0.5.0
```

**Tag Format:** `vX.Y.Z` (e.g., `v0.5.0`, `v1.0.0-beta`)

**What Happens:**
- Release workflow validates version, builds APK/AAB, uploads to Play Store, creates GitHub Release
- CHANGELOG.md should be updated manually before creating a release

**Important:** Commit version updates WITHOUT `[skip ci]` marker

## Project Links

- **Repository:** https://github.com/lanthoor/spendly
- **CI/CD:** GitHub Actions (see badge above)
- **Documentation:** See `CLAUDE.md`, `PLAN.md`, `MIGRATIONS.md`
