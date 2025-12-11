# Spendly - Personal Expense Tracker Execution Plan

## Project Setup & Infrastructure
1. ✓ Initialize Android project with Kotlin and Jetpack Compose
2. ✓ Configure Gradle build system with version catalogs
3. ✓ Add Room database dependency and configure (v2.6.1 with KSP v2.0.21-1.0.28)
4. ✓ Add Vico chart library dependency (v2.0.0-alpha.28 with Material 3 integration)
5. ✓ Add DataStore for preferences management (v1.1.1 - preferences and core)
6. ✓ Configure Hilt for dependency injection (v2.51.1 with Navigation Compose v1.2.0)
7. ✓ Set up build variants (debug/release) and ProGuard rules (Room, Hilt, DataStore, Coroutines)
8. ✓ Create project package structure (data/local/entities, data/local/dao, data/repository, data/datastore, domain/model, domain/repository, ui/screens/*, ui/components, ui/navigation, di/, utils/)
9. ✓ Set up GitHub Actions workflow for CI/CD (3 parallel jobs: Build, Test & Lint, Instrumented Tests)
10. ✓ Configure GitHub Actions to run on push and pull requests (main branch)
11. ✓ Add build job to GitHub Actions (./gradlew build) - runs in parallel
12. ✓ Add unit test job to GitHub Actions (./gradlew test) - runs with lint in Test & Lint job
13. ✓ Add lint check job to GitHub Actions (./gradlew lint) - runs with tests in Test & Lint job
14. ✓ Add instrumented test job to GitHub Actions (./gradlew connectedAndroidTest) with Android emulator (API 31, google_apis, x86_64)
15. ✓ Configure caching for Gradle dependencies in GitHub Actions (v4.3.0 with proper cache keys)
16. ✓ Add status badge to README.md for build status (https://github.com/lanthoor/spendly)

## Database Design & Implementation (Phase 2 - Complete)
1. ✓ Create Room database class and version management (v4, SQLCipher encryption deferred to Security & Data Protection section)
2. ✓ Create Expense entity with Room annotations (id: Long, amount: Long (paise), category_id: Long nullable, date: Long (timestamp), description: String, account_id: Long, created_at: Long, modified_at: Long)
3. ✓ Create Receipt entity (id: Long, expense_id: Long, file_path: String, file_type: String (JPG/PNG/WebP/PDF), file_size_bytes: Long max 5MB, compressed: Boolean)
4. ✓ Create Income entity (id: Long, amount: Long (paise), source: String, date: Long, description: String, is_recurring: Boolean, linked_expense_id: Long nullable (for refunds), category_id: Long nullable, account_id: Long, created_at: Long, modified_at: Long)
5. ✓ Create Category entity (id: Long, name: String, icon: String (Phosphor Icon name), color: Int, is_custom: Boolean, sort_order: Int, type: String) - includes 13 expense + 10 income categories
6. ✓ Create Budget entity (id: Long, category_id: Long nullable (null = overall budget), amount: Long (paise), month: Int, year: Int, notification_75_sent: Boolean, notification_100_sent: Boolean)
7. ✓ Create RecurringTransaction entity (id: Long, transaction_type: String (expense/income), amount: Long (paise), category_id: Long, description: String, frequency: String (daily/weekly/monthly), next_date: Long, last_processed: Long nullable)
8. ✓ Create Tag entity and TransactionTag junction entity with cross-references (many-to-many)
9. ✓ Create Account entity (id: Long, name: String, type: String, icon: String, color: Int, is_custom: Boolean, sort_order: Int, created_at: Long, modified_at: Long)
10. ✓ Create DAO interfaces for each entity with CRUD operations, use Flow for reactive queries
**Note:** Database v4 uses destructive migration for development. Migration logic removed - will be added before app release. Database backup/restore moved to Data Import/Export section.

## Core Data Models & State Management (Phase 3 - Complete)
1. ✓ Create Kotlin data classes for Expense model with amount helper functions (fromPaise, toPaise, displayAmount)
2. ✓ Create Kotlin data classes for Income model with refund linking support
3. ✓ Create Kotlin data classes for Receipt model
4. ✓ Create Kotlin data classes for Category model with predefined list constant (13 categories)
5. ✓ Create Kotlin data classes for Budget model with progress calculation and notification threshold helpers
6. ✓ Create Kotlin data classes for Tag model with TransactionTag junction model
7. ✓ Create Repository interface for expenses with Flow/StateFlow, default sort by date DESC (newest first)
8. ✓ Create Repository interface for income with Flow/StateFlow
9. ✓ Create Repository interface for categories with Flow/StateFlow, include predefined seed data
10. ✓ Create Repository interface for budgets with Flow/StateFlow, include notification tracking
11. ✓ Create Repository interface for tags with transaction-tag association methods
12. ✓ Implement ExpenseRepositoryImpl with entity-to-model mapping and DAO integration
13. ✓ Implement IncomeRepositoryImpl with refund filtering support
14. ✓ Implement CategoryRepositoryImpl with predefined category seeding logic (checks "Misc")
15. ✓ Implement BudgetRepositoryImpl with notification flag tracking
16. ✓ Implement TagRepositoryImpl with many-to-many junction table operations
17. ✓ Configure Hilt DatabaseModule - provides Room database and all 8 DAOs
18. ✓ Configure Hilt RepositoryModule - binds all 5 repository implementations to interfaces
19. ✓ Configure Hilt DataStoreModule - provides Preferences DataStore for app settings
20. ✓ Configure Hilt AppModule - provides application context and coroutine scope
21. ✓ Create SpendlyApplication with @HiltAndroidApp and category seeding on first launch
22. ✓ Update MainActivity with @AndroidEntryPoint annotation
23. ✓ Verify build success and all tests pass
**Note:** DataStore settings keys defined: theme, default_payment_method, notification_preferences, calendar_view_mode, budget_alert_75_enabled, budget_alert_100_enabled, sms_auto_detection_enabled

## Expense Management Features (Phase 4 - Complete)
1. ✓ Create ExpenseViewModel with StateFlow for UI state (Loading, Success, Error)
2. ✓ Create expense entry Compose form with validation (category optional, defaults to Misc)
3. ✓ Implement add expense functionality with Room insert, store amount in paise (Long)
4. ✓ Implement edit expense functionality with Room update, track modified_at timestamp
5. ✓ Implement delete expense with Material 3 confirmation dialog (DeleteConfirmDialog)
6. ✓ Create expense list LazyColumn with default sort: newest first (date DESC)
7. ✓ Implement receipt attachment using ActivityResultContracts for file picker (JPG, PNG, WebP, PDF)
8. ✓ Implement receipt photo capture using CameraX v1.5.2 with async initialization
9. ✓ Implement receipt image compression to 1920px max width, 85% quality, EXIF rotation handling
10. ✓ Create receipt storage using internal storage directory (encryption deferred to Security phase)
11. ✓ Support unlimited receipts per expense (one-to-many relationship with cascade delete)
12. Implement recurring expense setup Compose UI (daily, weekly, monthly frequency) - **Deferred to Phase 5**
13. Create app startup check for recurring expense processing (check last 3 months for missed occurrences) - **Deferred to Phase 5**
14. Implement expense search functionality with Room FTS (Full-Text Search) - **Deferred to Phase 5**
15. ✓ Implement expense filter by date range using DateRangePicker (UI ready in ViewModel, sheet pending)
16. ✓ Implement expense filter by category using FilterChip (UI ready in ViewModel, sheet pending)
17. ✓ Implement expense filter by payment method using dropdown menu (UI ready in ViewModel, sheet pending)

**Phase 4 Completed Components:**
- ✓ Navigation: Screen sealed class + SpendlyNavHost with type-safe routes
- ✓ ExpenseViewModel: Complete state management (UI state, form state, filter state)
- ✓ Shared UI Components: AmountTextField, CategoryDropdown, PaymentMethodDropdown, DatePickerField, SpendlyTopAppBar, EmptyState, LoadingIndicator, IconMapper
- ✓ ExpenseFormFields: Reusable form component for Add/Edit screens
- ✓ AddExpenseScreen: Modal bottom sheet with complete create flow and validation
- ✓ ExpenseListScreen: List view with empty/loading/error states, launches Add/Edit in bottom sheets
- ✓ EditExpenseScreen: Modal bottom sheet with complete update flow and pre-populated form
- ✓ ExpenseListItem: Material 3 list item with category icon, description, date, payment method, amount
- ✓ DeleteConfirmDialog: Confirmation dialog with cascade deletion (receipts auto-deleted via foreign key)
- ✓ CategorySelectionDialog: 3-column grid layout with icons, colors, visual selection (no radio buttons)
- ✓ PaymentMethodSelectionDialog: 3-column grid layout with payment icons and visual selection
- ✓ MainActivity: Updated with NavigationSuiteScaffold integration
- ✓ Dashboard route: Temporarily shows ExpenseList until Dashboard implemented in Phase 5
- ✓ Phosphor Icons: Replaced all Material Icons throughout the app (v1.0.0)
- ✓ InteractionSource: Proper click handling for read-only text fields

**UI/UX Improvements Implemented:**
- Modal bottom sheets for Add/Edit expense instead of separate screens (better mobile UX)
- Grid-based selection dialogs for categories and payment methods
- Visual selection indicators using colored backgrounds + borders (no radio buttons)
- Icons displayed for all categories (with category colors) and payment methods
- InteractionSource-based click detection for proper interaction with read-only fields
- Snackbar feedback with 2-second duration before navigation on edit success
- LazyVerticalGrid for scrollable category/payment selection with 3 columns
- Phosphor Icons used throughout for consistent modern iconography

**Technical Implementation Notes:**
- Icons: Phosphor Icons library (com.adamglin:phosphor-icon:1.0.0)
- Dialogs: AlertDialog with LazyVerticalGrid for scrollable content
- Selection: GridCells.Fixed(3) with aspectRatio(1f) for square items
- Colors: Material 3 primaryContainer + primary border for selection
- Interaction: MutableInteractionSource with PressInteraction.Release for clicks
- Navigation: ModalBottomSheet with skipPartiallyExpanded = true
- Filter logic: Implemented in ViewModel, sheet UI deferred to Phase 5
- Tests: Build successful, unit tests passing

## Income Tracking Features
1. ✓ Create IncomeViewModel with StateFlow
2. ✓ Create income entry Compose form (IncomeFormFields with category support)
3. ✓ Implement add income functionality with Room, store amount in paise (Long)
4. ✓ Implement edit income functionality
5. ✓ Implement delete income functionality
6. ✓ Create income list LazyColumn (IncomeListScreen with modal bottom sheets)
7. Implement recurring income setup Compose UI (daily, weekly, monthly) - **Deferred to Phase 6**
8. Create app startup check for recurring income processing (check last 3 months) - **Deferred to Phase 6**
9. ✓ Implement income source categorization - now uses category system instead of enum sources
10. ✓ Create Refund/Return income type with expense linking (linked_expense_id field)
11. ✓ Create income vs expense comparison view (Dashboard FinancialSummaryCard shows combined summary)

## Accounts System (Phase 6 - Complete)
1. ✓ Create AccountEntity with fields: id, name, type, icon, color, is_custom, sort_order, created_at, modified_at
2. ✓ Create AccountDao with CRUD operations, Flow-based queries, transaction count queries, and reassignment methods
3. ✓ Create Account domain model with DEFAULT_ACCOUNT_ID constant and PREDEFINED list
4. ✓ Create AccountRepository interface with CRUD, seeding, and validation methods (with excludeId for edit mode)
5. ✓ Implement AccountRepositoryImpl with entity-to-domain mapping, deletion with reassignment, and name uniqueness check
6. ✓ Add AccountType enum to Enums.kt: BANK, CARD, WALLET, CASH, LOAN, INVESTMENT
7. ✓ Update SpendlyDatabase: increment version to 4, add AccountEntity, add accountDao(), fallbackToDestructiveMigration, remove migration logic
8. ✓ Update DatabaseModule: add provideAccountDao(), remove addMigrations() call
9. ✓ Update RepositoryModule: add bindAccountRepository()
10. ✓ Update SpendlyApplication: seed predefined "My Account" (Bank type) on first launch
11. ✓ Update ExpenseEntity: replace paymentMethod with accountId, add FK constraint, update indexes
12. ✓ Update IncomeEntity: add accountId field, add FK constraint, add index
13. ✓ Update Expense domain model: replace paymentMethod with accountId
14. ✓ Update Income domain model: add accountId field
15. ✓ Update ExpenseRepository: replace getExpensesByPaymentMethod with getExpensesByAccount
16. ✓ Update IncomeRepository: add getIncomeByAccount
17. ✓ Update ExpenseRepositoryImpl: update entity mapping for accountId
18. ✓ Update IncomeRepositoryImpl: update entity mapping for accountId
19. ✓ Create AccountDropdown component
20. ✓ Create AccountSelectionDialog component with 3-column grid
21. ✓ Create AccountViewModel with state management and form validation
22. ✓ Create AccountListScreen with grouped account display
23. ✓ Create AddAccountScreen modal bottom sheet
24. ✓ Create EditAccountScreen modal bottom sheet with delete
25. ✓ Create AccountFormFields reusable component
26. ✓ Create AccountListItem component
27. ✓ Create AccountTypeSelectionDialog
28. ✓ Create DeleteAccountDialog with reassignment
29. ✓ Update ExpenseViewModel: inject AccountRepository, load accounts, update form state with accountId
30. ✓ Update IncomeViewModel: inject AccountRepository, load accounts, update form state with accountId
31. ✓ Update ExpenseFormFields: replace PaymentMethodDropdown with AccountDropdown
32. ✓ Update IncomeFormFields: add AccountDropdown
33. ✓ Update ExpenseListItem: show account name in subheading (format: "date • account name")
34. ✓ Update IncomeListItem: show account name in subheading (format: "date • account name")
35. ✓ Update RecentTransactionsWidget: show account name in subheading for recent transactions
36. ✓ Update TransactionListScreen: show account name in subheading for all transactions
37. ✓ Update DashboardViewModel: inject AccountRepository, load accounts, pass to UI
38. ✓ Update Screen.kt: add AccountList and AccountEdit routes
39. ✓ Update SpendlyNavHost: wire up account management screens
40. ✓ Deprecate PaymentMethodDropdown and PaymentMethodSelectionDialog
41. ✓ Fix account edit validation: exclude current account from uniqueness check
42. ✓ Update documentation: README.md, PLAN.md, CLAUDE.md

## Categories & Tags System
1. ✓ Implement predefined categories seed data - **Updated with separate expense/income categories:**
   - ✓ Expense categories (13, IDs 1-13): Food & Dining, Travel, Rent, Utilities, Services, Shopping, Entertainment, Healthcare, Gifts, Education, Investments, Groceries, Uncategorized
   - ✓ Income categories (10, IDs 101-110): Salary, Freelance, Business, Investment, Gift, Refund, Rental, Interest, Bonus, Other
   - ✓ Uses Phosphor Icons instead of Material Icons
   - ✓ CategoryType enum (EXPENSE/INCOME) in domain model
   - ✓ Database migration v1→v2 added type column and seeded income categories
2. Create category management Compose screen - **Deferred to future phase**
3. Implement custom category creation form with Phosphor Icons picker - **Deferred to future phase**
4. Implement custom category color picker using Compose ColorPicker - **Deferred to future phase**
5. ✓ Use Phosphor Icons library for category icons (icon stored as String name)
6. Implement category edit functionality in ViewModel - **Deferred to future phase**
7. Implement category delete with transaction reassignment dialog - **Deferred to future phase**
8. Create tag management Compose screen - **Deferred to future phase**
9. Implement tag creation functionality - **Deferred to future phase**
10. Implement tag assignment to transactions using junction table (many-to-many) - **Deferred to future phase**
11. Create tag-based filtering using FilterChip and Room queries - **Deferred to future phase**

## Budget Management Features
1. Create BudgetViewModel with budget calculation logic (amounts in paise)
2. Create budget setup Compose form with category selection dropdown (null = overall budget)
3. Implement monthly budget creation (per category or overall)
4. Implement budget edit functionality
5. Implement budget delete functionality
6. Create budget vs actual spending calculation using Room aggregate queries
7. Create LinearProgressIndicator component for budget progress with color coding
8. Implement budget overview dashboard Card composables
9. Create overspending alert logic in ViewModel - trigger at 75% and 100% thresholds
10. Implement budget notification system using NotificationCompat with notification_75_sent and notification_100_sent flags
11. Create budget comparison Compose view with Vico charts

## Analytics & Reporting
1. Implement data aggregation repository functions using Room queries (amounts in paise, convert for display)
2. Create pie chart Compose component using Vico for category breakdown
3. Create bar chart for monthly spending comparison using Vico
4. Create line chart for spending trends over time using Vico
5. Implement time period selector using SegmentedButton or TabRow
6. Create category-wise spending analysis Compose screen
7. Implement income vs expense trend analysis with Vico charts
8. Create monthly summary report generator using Kotlin
9. Create yearly summary report generator
10. Implement PDF export using PdfDocument API for monthly/yearly summaries
11. Implement CSV export - fields: Date, Amount, Category, Description, Account, Tags (comma-separated)
12. Create spending insights section within Analytics screen showing:
    - Top spending category for selected period
    - Month-over-month spending trend (increase/decrease %)
    - Budget vs actual comparison with visual indicators

## Dashboard & Main UI
1. ✓ Design main dashboard layout with Scaffold - set as landing screen (first screen on app open)
2. ✓ Create spending overview Card composable (FinancialSummaryCard with income/expenses/net balance)
3. Create budget status Card composable - **Deferred to Phase 6**
4. ✓ Create recent transactions LazyColumn widget (RecentTransactionsWidget showing last 5 transactions)
5. ✓ Create top spending categories Card with mini Vico chart (TopCategoriesChart)
6. ✓ Create FloatingActionButton for quick add expense (expandable FAB with income/expense options)
7. ✓ Implement pull-to-refresh using PullToRefreshBox
8. ✓ Use NavigationSuiteScaffold for adaptive navigation (bottom bar/rail/drawer)
9. ✓ Update AppDestinations enum: Home (Dashboard), Transactions (All), Analytics, Settings
10. ✓ Implement responsive layout using WindowSizeClass for tablets

## Calendar & Timeline Views
1. Create calendar Compose component using Material 3 DatePicker or custom calendar
2. Implement user-configurable calendar view mode stored in DataStore (expenses only / income only / both)
3. Implement transaction display on calendar dates with badges (different colors/icons for expense vs income)
4. Create day view with transaction details in BottomSheet
5. Implement month navigation with IconButtons
6. Create timeline/history LazyColumn of all transactions
7. Implement date range selection using DateRangePicker
8. Add toggle in calendar view to switch between display modes (expenses/income/both)

## Search & Filter System
1. Implement global search using Room FTS across all transactions
2. Create advanced filter ModalBottomSheet or Dialog
3. Implement filter by multiple categories using FilterChip group
4. Implement filter by amount range using RangeSlider
5. Implement filter by account using dropdown
6. Implement filter by tags using FilterChip
7. Create saved filter presets using DataStore
8. Implement search result highlighting using AnnotatedString

## Currency Configuration (REMOVED - INR Only)
~~127-132: Multi-currency support removed. App uses INR only. All amounts stored in paise (Long), displayed in ₹ format.~~

## SMS Auto-Detection Feature
1. Request SMS read permissions (Android READ_SMS)
2. Create SMS listener service using BroadcastReceiver (triggered on new SMS received)
3. Implement expense detection regex patterns for all major Indian banks (SBI, HDFC, ICICI, Axis, Kotak, PNB, BOB, Canara, Union, IDBI)
4. Implement expense detection regex patterns for UPI (NPCI, BHIM, PayTM, PhonePe, GPay)
5. Implement expense detection regex patterns for credit cards (Scapia, Federal Bank, etc.)
6. Implement income detection regex patterns (salary credits, refunds, UPI received)
7. Create SMS parsing engine to extract amount (parse to paise), date, merchant/description
8. Auto-create expense/income transactions from SMS (fully editable and deletable, no special restrictions)
9. Create SMS detection settings and toggle in app preferences
10. Store SMS source info in transaction description or metadata for reference

## Data Import/Export
1. Implement database backup and restore using Room export/import to JSON with metadata (version, export_date, currency: INR) - moved from Phase 2
2. Implement JSON export - single file with metadata section (version, export_date, currency: INR) and nested arrays for all entities
3. Implement JSON import functionality with validation and version checking
4. Implement CSV export for expenses - columns: Date, Amount (in ₹), Category, Description, Account, Tags (comma-separated)
5. Implement CSV export for income - columns: Date, Amount (in ₹), Source, Description, Linked Expense ID
6. Implement CSV import with column mapping UI
7. Create data export settings (select date range, categories, transaction types)
8. Implement data validation during import (check amount formats, date formats, category existence)

## Theme & UI Customization
1. Implement dark theme color scheme using Material 3 dynamic colors
2. Implement light theme color scheme using Material 3 dynamic colors
3. Create theme selector in settings with three options: Light, Dark, System Default
4. Implement theme persistence using DataStore
5. Apply theme colors to all components (follow current SpendlyTheme pattern)
6. Create theme-aware Vico chart colors

## Notification System
1. Implement Android notification permissions handling
2. Create notification service using Android NotificationManager
3. Implement budget overspending notifications
4. Implement recurring transaction reminder notifications
5. Create notification settings and preferences
6. Implement notification scheduling for recurring alerts

## Settings & Preferences
1. Create settings screen layout using Compose (part of Profile navigation destination)
2. ~~Remove currency preference setting (INR only)~~
3. Implement default account setting (select from user's accounts)
4. Implement notification preferences (enable/disable, budget alerts 75%/100%)
5. Implement theme selector (Light/Dark/System Default)
6. Implement calendar view mode preference (expenses/income/both)
7. Implement SMS auto-detection toggle
8. Implement data management section (backup to JSON, restore from JSON, clear all data with confirmation)
9. Implement about section with app version
10. Create export data options in settings (JSON or CSV with date range selection)
11. Create import data option in settings (JSON or CSV with validation)

## Security & Data Protection
1. Implement SQLite database encryption using SQLCipher
2. Create app lock/PIN protection option
3. Implement biometric authentication using BiometricPrompt
4. Create data encryption for receipt files
5. Implement secure storage for sensitive settings using EncryptedSharedPreferences

## Testing & Quality Assurance
1. Write unit tests for database operations
2. Write unit tests for transaction calculations
3. Write unit tests for budget calculations
4. Write instrumented tests for expense workflows
5. Write instrumented tests for income workflows
6. Test SMS auto-detection with various bank formats
7. Test data import/export functionality
8. Remove multi-currency test (not applicable - INR only)
9. Perform UI/UX testing on various Android devices
10. Test offline functionality thoroughly
11. Test notification delivery and timing
12. Perform security testing for data encryption

## Performance Optimization
1. Optimize database queries for large transaction volumes using indexes
2. Implement pagination for transaction lists using Paging 3 library
3. Optimize chart rendering for large datasets
4. Implement lazy loading for receipt images using Coil
5. Optimize app startup time with App Startup library
6. Reduce memory footprint and monitor with LeakCanary
7. Implement Room migrations for database versioning - moved from Phase 2 (v1 baseline established)

## Documentation & Polish
1. Create user documentation/help section in Compose
2. Write developer documentation for codebase
3. Create inline code comments for complex logic
4. Design app icon and branding
5. Create onboarding tutorial for first-time users using ViewPager2
6. Implement empty state designs for all views
7. Create error handling and user-friendly error messages
8. Implement loading states and progress indicators

## Build & Deployment
1. Configure Android build settings and Gradle configuration
2. Set up app signing and release configuration
3. Test debug builds on physical Android devices
4. Create release build APK/AAB
5. Perform final QA on release build
6. Prepare app store assets (screenshots, description)
7. Submit to Google Play Store (optional - deferred to end)

## Future Enhancements (Post-MVP)
1. Add widget support for Android home screen using Glance
2. Implement data backup to local file system
3. Create advanced analytics with AI-powered insights
4. Add support for split transactions
5. Implement merchant/payee management
6. Create custom report builder
7. Add support for cash flow projections
