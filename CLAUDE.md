# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Spendly** is an Android expense tracker built with Kotlin and Jetpack Compose. It's an offline-only application for personal finance management with no cloud sync or bank integration.

**Package:** `dev.lanthoor.spendly`

**Current Version:** 0.9.0-beta (versionCode 90)

## Build & Test Commands

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Install debug build on device
./gradlew installDebug

# Generate release AAB
./gradlew bundleRelease

# Generate test coverage report (unit tests only)
./gradlew jacocoTestReport
```

**Note:** Instrumented tests run with orchestrator disabled on CI. Test animations are disabled for consistency.

## Architecture

The project follows **MVVM + Clean Architecture** with clear separation of concerns:

```
app/src/main/java/dev/lanthoor/spendly/
├── data/                      # Data layer
│   ├── local/                 # Room database
│   │   ├── dao/              # Data Access Objects
│   │   ├── entities/         # Room entities (database tables)
│   │   ├── SpendlyDatabase.kt # Database + migrations
│   │   └── Converters.kt     # Type converters
│   └── repository/           # Repository implementations
├── domain/                    # Domain layer (pure Kotlin)
│   ├── model/                # Domain models (business entities)
│   └── repository/           # Repository interfaces
├── ui/                       # Presentation layer
│   ├── screens/              # Feature screens
│   │   ├── dashboard/
│   │   ├── expenses/
│   │   ├── income/
│   │   ├── analytics/
│   │   ├── transactions/
│   │   ├── budgets/
│   │   ├── accounts/
│   │   ├── recurring/
│   │   └── settings/
│   ├── components/           # Reusable UI components
│   ├── navigation/           # Navigation setup
│   ├── theme/                # Theme, colors, typography
│   └── viewmodels/           # App-level ViewModels
├── di/                       # Dependency Injection (Hilt modules)
├── utils/                    # Utilities
│   ├── parsers/              # Bank SMS parsers (HDFC, ICICI, SBI, etc.)
│   ├── SmsParser.kt          # Main SMS parsing coordinator
│   ├── CurrencyUtils.kt      # Currency formatting
│   ├── FileUtils.kt          # File operations
│   ├── ImageCompressor.kt    # Receipt image compression
│   └── BiometricAuthManager.kt
└── workers/                  # Background tasks (WorkManager)
```

### Key Architectural Patterns

1. **Repository Pattern**: Repositories abstract data sources, exposing Flow-based reactive streams
2. **StateFlow/Flow**: All data flows are reactive using Kotlin Flow
3. **Hilt DI**: All dependencies injected via Hilt (ViewModels, Repositories, DAOs)
4. **Entity-Model Separation**: Room entities in `data/local/entities`, domain models in `domain/model`
5. **ViewModel State Management**: ViewModels expose UI state via StateFlow

## Database

**Current Version:** 3
**Strategy:** Proper migrations enabled (NO destructive migrations)

### Entities (7 total)

- `CategoryEntity`: Unified categories for expenses/income (19 predefined)
- `ExpenseEntity`: Expense transactions with SMS metadata
- `IncomeEntity`: Income transactions with SMS metadata
- `ReceiptEntity`: Attached receipt files (images/PDFs)
- `BudgetEntity`: Monthly budgets per category
- `RecurringTransactionEntity`: Recurring transaction configurations
- `AccountEntity`: Financial accounts (Bank, Card, Wallet, Cash, etc.)

### Currency Handling

**CRITICAL:** All monetary amounts are stored as `Long` in **paise** (₹1.00 = 100 paise) to avoid floating-point precision issues.

```kotlin
// Correct
val amountInPaise: Long = 10050L  // ₹100.50
val formatted = CurrencyUtils.formatAmount(amountInPaise) // "₹100.50"

// Never do this
val amountInRupees: Double = 100.50  // ❌ WRONG
```

### Migrations

All migrations are defined in `SpendlyDatabase.kt`:
- **MIGRATION_1_2**: Unified category system (removed type column)
- **MIGRATION_2_3**: Fixed SMS timestamp handling

When adding new migrations:
1. Increment version in `@Database` annotation
2. Create `MIGRATION_X_Y` object in `SpendlyDatabase.kt`
3. Add to `DatabaseModule.provideSpendlyDatabase()`
4. Test thoroughly before release

## Key Technical Constraints

1. **Offline-Only**: No network calls, no cloud sync, no bank API integration
2. **Currency**: INR only (₹), stored as Long (paise)
3. **Target SDK**: 31-36 (Android 12+)
4. **I/O Operations**: Always use `Dispatchers.IO` for database/file operations
5. **No Destructive Migrations**: Database migrations must preserve all user data

## Feature Domains

### SMS Auto-Detection

Located in `utils/parsers/`:
- Bank-specific parsers for HDFC, ICICI, SBI, Axis, Kotak
- UPI parsers for NPCI, Paytm, PhonePe, GPay
- `SmsParser.kt` coordinates parsing with confidence scoring (0.0-1.0)
- Transactions with confidence ≥ 0.7 are shown to users

### Analytics

Custom Canvas-based charts (no external chart library for analytics):
- `CustomPieChart.kt`: Interactive donut chart with tap-to-show-details
- `CustomLineChart.kt`: Income/expense/net worth trend lines with tap interactions
- `ChartMath.kt`: Mathematical utilities for chart calculations
- 60 FPS rendering with smooth animations (600ms pie, 800ms line)
- Full TalkBack accessibility support

### Recurring Transactions

- Processed via `RecurringTransactionWorker` at app startup
- Frequencies: Daily, Weekly, Monthly
- `RecurringTransactionProcessor.kt` handles creation of actual transactions

### Budget Notifications

- `BudgetNotificationWorker` runs every 6 hours via WorkManager
- Alerts at 75% and 100% thresholds
- Notifications via `NotificationManagerCompat`

### App Lock

- Biometric authentication (fingerprint/face unlock)
- Configurable timeout: Immediately, 1min, 5min, 15min
- Lifecycle-aware via `AppLockViewModel`

### Receipt Management

- Supported formats: JPG, PNG, WebP, PDF
- Images compressed to max 1920px, 5MB per file
- Files stored in app-private storage via `FileUtils`
- Deletion cascades when expense is deleted

### Import/Export

- Complete JSON backup/restore functionality
- Exports all data: transactions, receipts (Base64), budgets, categories, accounts
- 500MB file size limit
- Validation and progress tracking during import

## UI Components & Patterns

### Navigation

- Material 3 Adaptive Navigation Suite (adaptive for tablets)
- 4 main destinations: Dashboard, Transactions, Analytics, Settings
- Central "+" FAB opens `AddTransactionBottomSheet`
- Navigation defined in `SpendlyNavHost.kt`

### Common UI Patterns

1. **Modal Bottom Sheets** for add/edit flows (expenses, income, accounts, budgets)
2. **State Hoisting**: ViewModels expose `StateFlow<UiState>`, UI collects as state
3. **Theme-Aware Colors**: Use `adjustForTheme()` for semantic colors
4. **Resource Strings**: All user-facing text in `res/values/strings.xml`

### Theme System

- Supports Light, Dark, System Default (persistent via DataStore)
- Dynamic color disabled (explicit color scheme)
- Defined in `ui/theme/Theme.kt`

## Important Utilities

### CurrencyUtils

```kotlin
CurrencyUtils.formatAmount(amountInPaise: Long): String  // "₹100.50"
CurrencyUtils.parseAmount(text: String): Long?          // "100.50" -> 10050L
```

### FileUtils

```kotlin
FileUtils.saveReceiptFile(context, uri): String  // Returns file path
FileUtils.deleteReceiptFile(context, filePath)
FileUtils.getReceiptUri(context, filePath): Uri
```

### ImageCompressor

```kotlin
ImageCompressor.compressImage(context, uri, maxSize = 1920): Uri
```

## Testing

### Unit Tests

- Located in `app/src/test/`
- Notable: `SmsParserTest.kt` with extensive bank SMS samples
- Run: `./gradlew test`

### Instrumented Tests

- Located in `app/src/androidTest/`
- DAO tests for all entities
- Chart component tests (`CustomPieChartTest.kt`, `CustomLineChartTest.kt`)
- Run: `./gradlew connectedAndroidTest`

## Dependency Injection (Hilt)

All modules in `di/`:
- `AppModule`: Application-level dependencies (Context, CoroutineScope)
- `DatabaseModule`: Room database, DAOs
- `DataStoreModule`: Preferences DataStore

ViewModels are injected with `@HiltViewModel` and `@Inject constructor()`.

## Background Work

WorkManager tasks in `workers/`:
- `RecurringTransactionWorker`: Processes recurring transactions (runs at startup)
- `BudgetNotificationWorker`: Checks budget thresholds (periodic, every 6 hours)

Both integrated with Hilt via `@HiltWorker`.

## Commit Conventions

This project uses **Conventional Commits**:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation
- `refactor:` - Code restructuring
- `test:` - Test additions/changes
- `chore:` - Maintenance (version bumps, dependency updates)

## Release Process

1. Update `versionName` and `versionCode` in `app/build.gradle.kts`
2. Commit: `chore: bump version to X.Y.Z`
3. Tag: `git tag vX.Y.Z && git push origin vX.Y.Z`
4. GitHub Actions handles build and Play Store upload

## Known Limitations

- **No iOS version**
- **No cloud sync or multi-device support**
- **No bank account integration**
- **No bill splitting**
- **No multi-currency support**
- **INR only**

## Development Notes

- MainActivity uses `FragmentActivity` (required for BiometricPrompt)
- App initialization flow: `SplashScreen` → initialization checks → `MainActivity` with optional lock screen overlay
- Receipts stored in app-private directory (`context.filesDir`)
- DataStore used for preferences (theme, app lock settings, financial year type)
