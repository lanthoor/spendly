# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spendly is a native Android personal expense tracker application built with Kotlin and Jetpack Compose. The app is designed as an **offline-only** application that tracks expenses, income, budgets, and provides analytics without any cloud synchronization or bank integration.

**Package:** `in.mylullaby.spendly`

## Build Commands

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests in.mylullaby.spendly.ExampleUnitTest

# Run a specific instrumented test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=in.mylullaby.spendly.ExampleInstrumentedTest
```

### Clean build
```bash
./gradlew clean
```

### Install debug build on device
```bash
./gradlew installDebug
```

### Lint checks
```bash
./gradlew lint
```

### Generate release APK
```bash
./gradlew assembleRelease
```

### Generate release AAB (Android App Bundle)
```bash
./gradlew bundleRelease
```

## CI/CD

### GitHub Actions
The project uses GitHub Actions for continuous integration and deployment:

**Workflow Configuration:** `.github/workflows/android.yml`

**✅ Status:** Fully configured and operational
**Badge:** [![Android CI](https://github.com/lanthoor/spendly/actions/workflows/android.yml/badge.svg)](https://github.com/lanthoor/spendly/actions/workflows/android.yml)

**Architecture:** 3 parallel jobs for faster feedback:
1. **Build Job:** Runs `./gradlew build` - verifies project compiles
2. **Test & Lint Job:** Runs `./gradlew test` + `./gradlew lint` - unit tests and code quality
3. **Instrumented Tests Job:** Runs `./gradlew connectedAndroidTest` with Android emulator (API 31, google_apis, x86_64)

**Actions Used (commit-hash pinned for security):**
- `actions/checkout@v6.0.1` (latest)
- `actions/setup-java@v5.1.0` (latest, JDK 21 LTS)
- `actions/cache@v4.3.0` (latest, Gradle caching)
- `actions/upload-artifact@v5.0.0` (latest, test/lint reports)
- `reactivecircus/android-emulator-runner@v2.35.0` (latest)

**Caching:** Gradle dependencies and wrapper cached with proper cache keys

**Triggers:** Workflow runs on:
- Push to `main` branch
- Pull requests to `main` branch

**Artifacts:** Test and lint reports uploaded on every run (accessible even on failure)

**Note:** Play Store deployment is NOT automated - deferred to end of development (manual process as per PLAN.md task 210)

## Architecture

### Technology Stack
- **Language:** Kotlin 2.0.21
- **Java:** 21 LTS (Temurin distribution)
- **UI Framework:** Jetpack Compose with Material 3
- **Navigation:** Material 3 Adaptive Navigation Suite (bottom nav/rail/drawer based on screen size)
- **Build System:** Gradle with Kotlin DSL
- **Min SDK:** 31 (Android 12)
- **Target SDK:** 36

### Architecture Pattern
- **MVVM:** Model-View-ViewModel with ViewModel and StateFlow (in progress)
- **Clean Architecture:** Separation of data, domain, and presentation layers
- **Dependency Injection:** Hilt v2.51.1 for all dependencies
- **Dependency Management:** Version catalog in `gradle/libs.versions.toml`
- **Compose Navigation:** Material 3 Adaptive Navigation Suite for responsive layouts

### Project Structure
```
app/src/
├── main/
│   ├── java/in/mylullaby/spendly/
│   │   ├── MainActivity.kt                    # Main entry point with NavigationSuiteScaffold
│   │   ├── data/                              # Data layer
│   │   │   ├── local/                         # Room database
│   │   │   │   ├── entities/                  # Room entities (to be implemented)
│   │   │   │   └── dao/                       # Data Access Objects (to be implemented)
│   │   │   ├── repository/                    # Repository implementations (to be implemented)
│   │   │   └── datastore/                     # DataStore preferences (to be implemented)
│   │   ├── domain/                            # Domain layer
│   │   │   ├── model/                         # Domain models (to be implemented)
│   │   │   └── repository/                    # Repository interfaces (to be implemented)
│   │   ├── ui/                                # Presentation layer
│   │   │   ├── screens/                       # Feature screens
│   │   │   │   ├── dashboard/                 # Dashboard/Home (to be implemented)
│   │   │   │   ├── expenses/                  # Expense management (to be implemented)
│   │   │   │   ├── income/                    # Income tracking (to be implemented)
│   │   │   │   ├── budgets/                   # Budget management (to be implemented)
│   │   │   │   ├── analytics/                 # Analytics & charts (to be implemented)
│   │   │   │   └── settings/                  # Settings (to be implemented)
│   │   │   ├── components/                    # Reusable composables (to be implemented)
│   │   │   ├── navigation/                    # Navigation setup (to be implemented)
│   │   │   └── theme/                         # ✅ Material 3 theming (Color, Type, Theme)
│   │   ├── di/                                # Dependency injection modules
│   │   │   ├── DatabaseModule.kt              # Database providers (placeholder)
│   │   │   ├── RepositoryModule.kt            # Repository bindings (placeholder)
│   │   │   └── AppModule.kt                   # App-level dependencies (placeholder)
│   │   └── utils/                             # Helper utilities
│   │       └── Extensions.kt                  # Extension functions (placeholder)
│   ├── res/                                   # Resources (layouts, drawables, values)
│   └── AndroidManifest.xml
├── test/                                      # Unit tests
└── androidTest/                               # Instrumented tests
```

### Current Implementation Status

**✅ Phase 1 Complete: Project Setup & Infrastructure (Tasks 1-16)**
- ✅ Basic MainActivity with Material 3 adaptive navigation (Home/Favorites/Profile destinations)
- ✅ Material 3 theming with dynamic color support (Android 12+)
- ✅ Edge-to-edge UI enabled
- ✅ Placeholder "Hello Android" greeting screen
- ✅ **All dependencies configured:** Room v2.6.1, Vico v2.0.0-alpha.28, DataStore v1.1.1, Hilt v2.51.1
- ✅ **ProGuard rules configured** for all libraries (Room, Hilt, DataStore, Coroutines)
- ✅ **Complete package structure** following clean architecture (data, domain, ui, di, utils)
- ✅ **GitHub Actions CI/CD** with 3 parallel jobs (Build, Test & Lint, Instrumented Tests)
- ✅ **Latest GitHub Actions** with commit-hash pinning for security

**🚧 Next Phase: Database Foundation (Tasks 17-27)**
- Room database entities (Expense, Income, Category, Budget, Receipt, RecurringTransaction, Tag)
- DAOs with Flow-based reactive queries
- Repository layer with clean architecture

### Tech Stack (Configured & Ready)
- ✅ **Database:** Room v2.6.1 (SQLite wrapper) - SQLCipher encryption deferred to task 173
- ✅ **Charts:** Vico v2.0.0-alpha.28 with Material 3 integration
- ✅ **Dependency Injection:** Hilt v2.51.1 with Navigation Compose v1.2.0
- ✅ **Preferences:** DataStore v1.1.1 (preferences and core)
- ⏳ **Image Loading:** Coil (to be added when needed)
- ⏳ **Background Work:** WorkManager for recurring transactions (to be added)
- ⏳ **Permissions:** SMS read for auto-detection (to be added)
- ⏳ **Security:** BiometricPrompt, EncryptedSharedPreferences (to be added)
- ⏳ **Pagination:** Paging 3 library (to be added)
- ✅ **CI/CD:** GitHub Actions with parallel jobs, latest actions (v6/v5), commit-hash pinned

## Core Features (Planned)

### Data Management
- **Database:** SQLite with encryption
- **Models:** Expense, Income, Category, Budget, Tag, RecurringTransaction
- All data stored locally with no cloud sync

### Key Functionality
1. **Expense & Income Tracking:** CRUD operations with categories, tags, payment methods (Cash/UPI/Debit Card/Credit Card/Net Banking/Wallet), unlimited receipt attachments (JPG/PNG/WebP/PDF, max 5MB per file, compressed to 1920px)
2. **Budget Management:** Per-category or overall monthly budgets with overspending alerts at 75% and 100% thresholds
3. **Analytics:** Vico charts (pie/bar/line) for spending trends, category breakdowns, monthly/yearly comparisons. Insights include: top spending category, month-over-month trends, budget vs actual
4. **SMS Auto-Detection:** Parse bank SMS from all major Indian banks + UPI + credit cards. Auto-creates transactions (fully editable/deletable)
5. **Currency:** INR only. All amounts stored as Long (paise), displayed in ₹ format
6. **Data Import/Export:** JSON with metadata, CSV with all fields
7. **Theme:** Three options - Light, Dark, System Default
8. **Calendar View:** User-configurable (expenses only, income only, or both)
9. **Refunds:** Tracked as income with link to original expense

### Explicitly Out of Scope
- Bank account integration/auto-import
- Cloud synchronization
- Bill splitting with friends
- Investment portfolio tracking
- iOS version

## Development Guidelines

### Compose UI Patterns
- Use `@PreviewScreenSizes` for responsive previews across different device sizes
- Leverage Material 3 adaptive components:
  - `NavigationSuiteScaffold` automatically adapts navigation UI (bottom bar on phones, navigation rail on tablets, drawer on large screens)
  - See MainActivity.kt for reference implementation with `AppDestinations` enum
- Theme uses Material 3 dynamic colors on Android 12+ devices by default
- All new screens should follow the existing pattern: Composable functions with state hoisting and ViewModel integration

### Package Naming
The package uses backticks due to `in` being a Kotlin keyword: `` `in`.mylullaby.spendly ``

### Database Design (Planned)
Refer to PLAN.md for complete schema. Key entities:

**Core Tables:**
- **Expense:** id: Long, amount: Long (paise), category_id: Long nullable, date: Long, description: String, payment_method: String, created_at: Long, modified_at: Long
- **Receipt:** id: Long, expense_id: Long, file_path: String, file_type: String, file_size_bytes: Long (max 5MB), compressed: Boolean (one-to-many with Expense)
- **Income:** id: Long, amount: Long (paise), source: String, date: Long, description: String, is_recurring: Boolean, linked_expense_id: Long nullable (for refunds), created_at: Long, modified_at: Long
- **Category:** id: Long, name: String, icon: String (Material Icon name), color: Int, is_custom: Boolean, sort_order: Int
  - **Predefined (14):** Food & Dining (restaurant), Travel (flight), Rent (home), Utilities (lightbulb), Services (build), Shopping (shopping_cart), Entertainment (movie), Healthcare (local_hospital), Gifts (card_giftcard), Education (school), Investments (trending_up), Groceries (local_grocery_store), Others (more_horiz), Uncategorized (category)
- **Budget:** id: Long, category_id: Long nullable (null = overall), amount: Long (paise), month: Int, year: Int, notification_75_sent: Boolean, notification_100_sent: Boolean
- **RecurringTransaction:** id: Long, transaction_type: String, amount: Long (paise), category_id: Long, description: String, frequency: String (daily/weekly/monthly), next_date: Long, last_processed: Long nullable
- **Tag & TransactionTag:** Many-to-many relationship via junction table

**Architecture notes:**
- All amounts stored as Long in paise (₹1.00 = 100 paise) to avoid floating-point precision issues
- Use Room DAOs with Flow/StateFlow for reactive data
- Implement proper Room migrations
- SQLCipher encryption for database
- Category field optional (defaults to Uncategorized)
- Default sort: date DESC (newest first)
- Recurring transactions processed at app startup, check last 3 months for missed occurrences

## Testing
- Unit tests in `app/src/test/`
- Instrumented tests in `app/src/androidTest/`
- Planned test coverage: database operations, transaction calculations, budget calculations, SMS parsing, data import/export

## Security Considerations
- Implement SQLCipher for SQLite database encryption before production
- Add app lock/PIN protection option
- Support biometric authentication using BiometricPrompt API
- Encrypt receipt files on disk in internal storage
- Use EncryptedSharedPreferences for sensitive settings
- Handle SMS permissions carefully (READ_SMS required for auto-detection)
- Never commit sensitive data or API keys to version control

## Key Constraints & Specifications
- **Offline-only:** No network requests, no cloud sync, no external APIs
- **Currency:** INR only (no multi-currency support). All amounts in paise (Long).
- **Android-only:** No iOS version planned
- **Local storage only:** All data persists in SQLite (encrypted with SQLCipher) and internal storage (encrypted)
- **Payment methods (predefined):** Cash, UPI, Debit Card, Credit Card, Net Banking, Wallet
- **Categories (14 predefined):** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Entertainment, Healthcare, Gifts, Education, Investments, Groceries, Others, Uncategorized
- **Receipt limits:** Unlimited per expense, max 5MB per file, compressed to 1920px, formats: JPG/PNG/WebP/PDF
- **Dashboard:** Landing screen with 5 recent transactions, spending overview, budget status, top categories chart
- **Navigation:** Home (Dashboard), Analytics (with Insights), Profile/Settings
- **Theme:** Material 3 with three options (Light/Dark/System Default)
- **SMS Banks:** All major Indian banks + UPI (NPCI, BHIM, PayTM, PhonePe, GPay) + credit cards (Scapia, Federal Bank, etc.)
- **Budget alerts:** Notify at 75% and 100% thresholds
- **Refunds:** Tracked as income with dedicated category, linked to original expense
- **Category deletion:** Requires user to reassign transactions to another category (including Uncategorized)
- **Export formats:**
  - JSON: Single file with metadata (version, export_date, currency: INR) and all entities
  - CSV: Date, Amount (in ₹), Category, Description, Payment Method, Tags (comma-separated)