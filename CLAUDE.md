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
│   │   ├── MainActivity.kt                    # ✅ Main entry point with @AndroidEntryPoint
│   │   ├── SpendlyApplication.kt              # ✅ Application class with @HiltAndroidApp
│   │   ├── data/                              # Data layer
│   │   │   ├── local/                         # Room database
│   │   │   │   ├── entities/                  # ✅ Room entities (8 entities)
│   │   │   │   ├── dao/                       # ✅ Data Access Objects (8 DAOs)
│   │   │   │   └── SpendlyDatabase.kt         # ✅ Room database configuration
│   │   │   ├── repository/                    # ✅ Repository implementations (6 repositories)
│   │   │   └── datastore/                     # DataStore preferences (to be implemented)
│   │   ├── domain/                            # Domain layer
│   │   │   ├── model/                         # ✅ Domain models (6 models)
│   │   │   └── repository/                    # ✅ Repository interfaces (6 interfaces)
│   │   ├── ui/                                # Presentation layer
│   │   │   ├── screens/                       # Feature screens
│   │   │   │   ├── dashboard/                 # Dashboard/Home (to be implemented)
│   │   │   │   ├── expenses/                  # ✅ Expense management UI
│   │   │   │   │   ├── ExpenseViewModel.kt    # ✅ State management with receipt handling
│   │   │   │   │   ├── ExpenseListScreen.kt   # ✅ List view with bottom sheet integration
│   │   │   │   │   ├── AddExpenseScreen.kt    # ✅ Modal bottom sheet for add
│   │   │   │   │   ├── EditExpenseScreen.kt   # ✅ Modal bottom sheet for edit with receipts
│   │   │   │   │   └── components/            # ✅ Expense-specific components
│   │   │   │   │       ├── ExpenseFormFields.kt      # ✅ Reusable form component
│   │   │   │   │       ├── ExpenseListItem.kt        # ✅ List item with category icon
│   │   │   │   │       ├── CategorySelectionDialog.kt # ✅ 3-column grid dialog
│   │   │   │   │       ├── PaymentMethodSelectionDialog.kt # ✅ 3-column grid dialog
│   │   │   │   │       ├── DeleteConfirmDialog.kt    # ✅ Confirmation dialog
│   │   │   │   │       ├── ReceiptThumbnail.kt       # ✅ Optimized image thumbnail
│   │   │   │   │       ├── ReceiptPickerSheet.kt     # ✅ File/camera picker
│   │   │   │   │       └── CameraCapture.kt          # ✅ Full-screen camera preview
│   │   │   │   ├── income/                    # Income tracking (to be implemented)
│   │   │   │   ├── budgets/                   # Budget management (to be implemented)
│   │   │   │   ├── analytics/                 # Analytics & charts (to be implemented)
│   │   │   │   └── settings/                  # Settings (to be implemented)
│   │   │   ├── components/                    # ✅ Reusable composables
│   │   │   │   ├── AmountTextField.kt         # ✅ Currency input with validation
│   │   │   │   ├── CategoryDropdown.kt        # ✅ Category selection field
│   │   │   │   ├── PaymentMethodDropdown.kt   # ✅ Payment method field
│   │   │   │   ├── DatePickerField.kt         # ✅ Date selection field
│   │   │   │   ├── SpendlyTopAppBar.kt        # ✅ Consistent app bar
│   │   │   │   ├── EmptyState.kt              # ✅ Empty state component
│   │   │   │   ├── LoadingIndicator.kt        # ✅ Loading component
│   │   │   │   └── IconMapper.kt              # ✅ Category icon mapping
│   │   │   ├── navigation/                    # ✅ Navigation setup
│   │   │   │   ├── Screen.kt                  # ✅ Screen sealed class
│   │   │   │   └── SpendlyNavHost.kt          # ✅ Navigation host
│   │   │   └── theme/                         # ✅ Material 3 theming (Color, Type, Theme)
│   │   ├── di/                                # ✅ Dependency injection modules (4 modules)
│   │   │   ├── DatabaseModule.kt              # ✅ Database & DAO providers (8 DAOs)
│   │   │   ├── RepositoryModule.kt            # ✅ Repository bindings (6 repositories)
│   │   │   ├── DataStoreModule.kt             # ✅ DataStore preferences provider
│   │   │   └── AppModule.kt                   # ✅ App-level dependencies
│   │   └── utils/                             # ✅ Helper utilities
│   │       ├── CurrencyUtils.kt               # ✅ Paise/Rupee conversion utilities
│   │       ├── Enums.kt                       # ✅ PaymentMethod, IncomeSource, etc.
│   │       ├── FileUtils.kt                   # ✅ File operations and validation
│   │       ├── ImageCompressor.kt             # ✅ EXIF-aware image compression
│   │       └── PermissionUtils.kt             # ✅ Camera permission helper
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

**✅ Phase 2 Complete: Database Foundation (Tasks 17-27)**
- ✅ **SpendlyDatabase:** Room database with 9 entities, version 4, schema export enabled
- ✅ **9 Room Entities:** ExpenseEntity, IncomeEntity, CategoryEntity, BudgetEntity, ReceiptEntity, RecurringTransactionEntity, TagEntity, TransactionTagEntity, AccountEntity
- ✅ **9 DAOs with Flow-based queries:** Full CRUD operations, complex queries with aggregations, date range filtering, category-based queries, account filtering
- ✅ **Proper schema design:** Foreign keys with CASCADE/SET_NULL, composite indexes for performance, proper normalization
- ✅ **Integer-only currency:** All amounts stored as Long (paise) for ZERO precision loss - no floating-point arithmetic
- ✅ **Audit timestamps:** createdAt and modifiedAt fields on all transaction entities
- ✅ **Many-to-many tags:** Junction table (TransactionTagEntity) for flexible tagging
- ✅ **Database Strategy:** Destructive migration for development (fallbackToDestructiveMigration), migration logic removed until pre-release

**✅ Phase 3 Complete: Repository Layer & Domain Models (Tasks 28-57)**
- ✅ **7 domain models:** Expense, Income, Category, Budget, Tag, Receipt, Account with proper type safety
- ✅ **7 repository interfaces:** ExpenseRepository, IncomeRepository, CategoryRepository, BudgetRepository, TagRepository, ReceiptRepository, AccountRepository
- ✅ **7 repository implementations:** Full CRUD with entity-to-model mapping, file management for receipts, account deletion with reassignment
- ✅ **Hilt DI modules:** DatabaseModule (9 DAOs), RepositoryModule (7 repositories), DataStoreModule, AppModule
- ✅ **SpendlyApplication:** Category and account seeding on first launch with @HiltAndroidApp

**✅ Phase 4 Complete: Expense Management UI (Tasks 59-77)**
- ✅ **Navigation:** Screen sealed class with type-safe routes + SpendlyNavHost
- ✅ **ExpenseViewModel:** Complete state management (UI state, form state, filter state, receipt management)
- ✅ **Core UI Screens:** ExpenseListScreen, AddExpenseScreen (modal bottom sheet), EditExpenseScreen (modal bottom sheet)
- ✅ **Shared Components:** AmountTextField, CategoryDropdown, PaymentMethodDropdown, DatePickerField, ExpenseFormFields
- ✅ **Dialogs:** CategorySelectionDialog (3-column grid), PaymentMethodSelectionDialog (3-column grid), DeleteConfirmDialog
- ✅ **Receipt Management:** File picker (ActivityResultContracts), Camera capture (CameraX 1.5.2), Image compression (1920px, 85% quality)
- ✅ **Receipt Components:** ReceiptThumbnail (Coil with size optimization), ReceiptPickerSheet, CameraCapture (async initialization)
- ✅ **Utilities:** FileUtils (file ops, validation), ImageCompressor (EXIF-aware), PermissionUtils, CurrencyUtils, Enums
- ✅ **Performance:** IO dispatcher for file ops, async camera init, thumbnail sizing (240px for 120dp), Coil caching
- ✅ **Icons:** Phosphor Icons v1.0.0 throughout the app
- ✅ **16 KB page size compatibility:** CameraX 1.5.2 + useLegacyPackaging = false

**✅ Phase 5 Complete: Dashboard, Income Tracking & Navigation (Partial)**
- ✅ **Dashboard Screen:** DashboardScreen with financial summary, recent transactions widget, top categories chart
- ✅ **DashboardViewModel:** Combined expense + income summary, month-over-month calculations, category spending
- ✅ **Dashboard Components:** FinancialSummaryCard, RecentTransactionsWidget, TopCategoriesChart (Vico)
- ✅ **Income Tracking:** IncomeViewModel, AddIncomeScreen, EditIncomeScreen, IncomeListScreen (all modal bottom sheets)
- ✅ **Income Components:** IncomeFormFields, IncomeListItem with category support
- ✅ **Category System:** Separate expense categories (IDs 1-13) and income categories (IDs 101-110) with CategoryType enum
- ✅ **TransactionListScreen:** Combined expense + income list with edit/delete via modal sheets
- ✅ **Navigation:** 4-item bottom navigation (Home/Dashboard, Transactions, Analytics, Settings) via NavigationSuiteScaffold
- ✅ **UI Enhancements:** Color-coded amounts (green +income, red -expense), payment method display, no arrows
- ✅ **Currency Fix:** paiseToRupeeString() with integer-only arithmetic (no scientific notation)
- ✅ **Enum Extensions:** toDisplayName() for PaymentMethod, toDisplayString() for IncomeSource

**✅ Phase 6 Complete: Accounts System**
- ✅ **Account Management:** Full CRUD for accounts with customizable types (BANK/CARD/WALLET/CASH/LOAN/INVESTMENT)
- ✅ **AccountEntity & AccountDao:** Database layer with proper indexes, foreign key constraints, transaction reassignment queries
- ✅ **Account Domain Model:** AccountRepository with seeding logic for "My Account" (default Bank account), name uniqueness validation with excludeId
- ✅ **Expense/Income Integration:** Replaced payment method field with account references (accountId) in all transactions
- ✅ **Account UI Components:** AccountDropdown, AccountSelectionDialog with 3-column grid and type badges
- ✅ **Account Management Screens:** AccountListScreen, AddAccountScreen, EditAccountScreen with deletion reassignment
- ✅ **Transaction Display:** Account names shown in subheadings (format: "date • account name") for recent and all transactions
- ✅ **Bug Fixes:** Account edit validation correctly excludes current account from uniqueness check
- ✅ **Database Strategy:** v4 with destructive migration for development, migration logic removed until pre-release

**🚧 Next Phase: Recurring Transactions, Search, Filters, Budget Management (Phase 7)**

### Tech Stack (Configured & Ready)
- ✅ **Database:** Room v2.6.1 (SQLite wrapper) - SQLCipher encryption deferred to task 173
- ✅ **Charts:** Vico v2.0.0-alpha.28 with Material 3 integration
- ✅ **Dependency Injection:** Hilt v2.51.1 with Navigation Compose v1.2.0
- ✅ **Preferences:** DataStore v1.1.1 (preferences and core)
- ✅ **Image Loading:** Coil v2.5.0 for receipt thumbnails with size optimization and caching
- ✅ **Camera:** CameraX v1.5.2 for receipt capture with async initialization (16 KB page size compatible)
- ✅ **Icons:** Phosphor Icons v1.0.0 (replaced Material Icons throughout app)
- ✅ **Background Work:** WorkManager v2.9.0 + Hilt Work v1.1.0 configured (recurring transactions implementation pending)
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
1. **Expense & Income Tracking:** CRUD operations with categories, tags, accounts (customizable with types: BANK/CARD/WALLET/CASH/LOAN/INVESTMENT), unlimited receipt attachments (JPG/PNG/WebP/PDF, max 5MB per file, compressed to 1920px)
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

## Receipt Management Architecture

### File Storage Strategy
- **Location:** Internal storage (`context.filesDir/receipts/`)
- **Naming:** `receipt_{expenseId}_{timestamp}.{extension}`
- **Size limit:** 5MB per file (validated before save)
- **Formats:** JPG, PNG, WebP, PDF (validated by file extension)
- **Security:** Files stored in app-private directory, deleted on app uninstall
- **Encryption:** Deferred to Security Phase (task 173 in PLAN.md)

### Image Compression
- **Max dimension:** 1920px (width or height, aspect ratio preserved)
- **Quality:** 85% JPEG compression
- **EXIF handling:** Automatic rotation based on EXIF orientation tag
- **OOM prevention:** BitmapFactory.Options with proper scaling
- **Threading:** All compression on IO dispatcher using `withContext(Dispatchers.IO)`

### Camera Integration
- **Library:** CameraX v1.5.2 with async initialization
- **Architecture:** DisposableEffect for lifecycle-aware setup/cleanup
- **Preview:** Single PreviewView with `setSurfaceProvider()` connection
- **Capture mode:** `CAPTURE_MODE_MINIMIZE_LATENCY` for faster photos
- **Threading:** Listener runs on main executor, capture callbacks on main thread
- **Loading states:** Shows CircularProgressIndicator during initialization

### Image Loading Optimization
- **Library:** Coil v2.5.0 with Material 3 integration
- **Thumbnail sizing:** Decode to 240px for 120dp display (95% memory reduction)
- **Crossfade:** Disabled (`crossfade(false)`) for better performance
- **Caching:** Explicit memory + disk cache keys using file path
- **Content scale:** `ContentScale.Crop` for consistent aspect ratio

### Performance Best Practices
1. **File I/O:** All file operations wrapped in `withContext(Dispatchers.IO)`
2. **State updates:** UI state changes via `withContext(Dispatchers.Main)` after background work
3. **Camera init:** Async initialization with DisposableEffect, proper cleanup in onDispose
4. **Image decoding:** Size-constrained decoding (240px) instead of full resolution
5. **Cache strategy:** Coil handles automatic memory management and bitmap recycling

### URI Handling
- **Dual-path logic:** Handles both `file://` and `content://` URIs
- **File scheme:** Direct file path reading via `uri.path`
- **Content scheme:** ContentResolver queries for metadata
- **Extension detection:** MIME type first, fallback to display name parsing
- **Size detection:** File.length() for file://, ContentResolver for content://

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
- **Income:** id: Long, amount: Long (paise), source: String, date: Long, description: String, is_recurring: Boolean, linked_expense_id: Long nullable (for refunds), category_id: Long nullable, created_at: Long, modified_at: Long
- **Category:** id: Long, name: String, icon: String (Phosphor Icon name), color: Int, is_custom: Boolean, sort_order: Int, type: String (EXPENSE or INCOME)
  - **Predefined Expenses (13, IDs 1-13):** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Entertainment, Healthcare, Gifts, Education, Investments, Groceries, Uncategorized
  - **Predefined Income (10, IDs 101-110):** Salary, Freelance, Business, Investment, Gift, Refund, Rental, Interest, Bonus, Other
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
- **Accounts (customizable):** Users can create accounts with types: BANK, CARD, WALLET, CASH, LOAN, INVESTMENT
- **Default account:** "My Account" (Bank type) - all transactions default to this account
- **Categories (14 predefined):** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Entertainment, Healthcare, Gifts, Education, Investments, Groceries, Others, Uncategorized
- **Receipt limits:** Unlimited per expense, max 5MB per file, compressed to 1920px, formats: JPG/PNG/WebP/PDF
- **Dashboard:** Landing screen with 5 recent transactions, financial summary (income/expenses/net balance), top categories chart
- **Navigation:** Home (Dashboard), Transactions (All), Analytics (placeholder), Settings (placeholder)
- **Theme:** Material 3 with three options (Light/Dark/System Default)
- **SMS Banks:** All major Indian banks + UPI (NPCI, BHIM, PayTM, PhonePe, GPay) + credit cards (Scapia, Federal Bank, etc.)
- **Budget alerts:** Notify at 75% and 100% thresholds
- **Refunds:** Tracked as income with dedicated category, linked to original expense
- **Category deletion:** Requires user to reassign transactions to another category (including Uncategorized)
- **Export formats:**
  - JSON: Single file with metadata (version, export_date, currency: INR) and all entities
  - CSV: Date, Amount (in ₹), Category, Description, Account, Tags (comma-separated)
- never commit without explicit instruction
- use concise and short commit messages. no need to put test coverage/etc., next phase details, challenges, etc. in the commit message. also the first line should be less than 60 characters long.
- update PLAN.md and README.md after each phase completion