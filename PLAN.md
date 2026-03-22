# Spendly - Development Plan

**Current Version:** 0.8.0-beta (versionCode 80)
**Database Version:** 3 (SMS timestamp fix, established 2025-12-20)
**Last Updated:** 2025-12-21

---

## ✅ Completed Phases

### Phase 1: Project Setup & Infrastructure ✓
1. ✓ Initialize Android project with Kotlin and Jetpack Compose
2. ✓ Configure Gradle with version catalogs
3. ✓ Add Room v2.6.1 (KSP v2.1.0-1.0.29)
4. ✓ Add Charts library v2.0.1 by dautovicharis (production-ready, Material 3)
5. ✓ Add DataStore v1.1.1 (preferences + core)
6. ✓ Configure Hilt v2.51.1 (+ Navigation Compose v1.2.0)
7. ✓ Set up build variants + ProGuard rules
8. ✓ Create package structure (data, domain, ui, di, utils)
9. ✓ GitHub Actions CI/CD (parallel: Build, Test & Lint)
10. ✓ Configure caching for Gradle dependencies
11. ✓ Add status badge to README

### Phase 2: Database Foundation ✓
1. ✓ Create Room database (v2 with unified categories)
2. ✓ Create 7 entities: Category, Expense, Receipt, Income, Budget, RecurringTransaction, Account
3. ✓ Create 7 DAOs with Flow-based reactive queries
4. ✓ Implement foreign keys (CASCADE/SET_NULL)
5. ✓ Integer-only currency (amounts as Long in paise)
6. ✓ Audit timestamps (createdAt, modifiedAt)
7. ✓ Proper migrations enabled (see MIGRATIONS.md)

**Entities:**
- CategoryEntity: 19 unified categories (no expense/income distinction), icons, colors
- ExpenseEntity: Transactions with SMS metadata fields
- ReceiptEntity: Unlimited attachments (JPG/PNG/WebP/PDF, 5MB each)
- IncomeEntity: Income with refund linking support
- BudgetEntity: Monthly budgets with notification flags
- RecurringTransactionEntity: Daily/weekly/monthly configurations
- AccountEntity: 6 account types (Bank/Card/Wallet/Cash/Loan/Investment)

### Phase 3: Repository Layer & Domain Models ✓
1. ✓ Create 8 domain models (Expense, Income, Category, Budget, Receipt, Account, RecurringTransaction, SmsPendingTransaction)
2. ✓ Create 9 repository interfaces
3. ✓ Implement 9 repositories with entity-to-model mapping
4. ✓ Configure Hilt modules (Database, Repository, DataStore, App)
5. ✓ SpendlyApplication with category/account seeding
6. ✓ MainActivity with @AndroidEntryPoint

### Phase 4: Expense Management UI ✓
1. ✓ ExpenseViewModel with state management (UI/form/filter states)
2. ✓ Navigation (Screen sealed class + SpendlyNavHost)
3. ✓ Add/Edit screens (modal bottom sheets)
4. ✓ ExpenseFormFields (reusable component)
5. ✓ Receipt management (file picker, CameraX v1.5.2, compression)
6. ✓ Receipt components (ReceiptThumbnail, ReceiptPickerSheet, CameraCapture)
7. ✓ Shared components (AmountTextField, CategoryDropdown, DatePickerField, etc.)
8. ✓ Grid selection dialogs (categories, accounts)
9. ✓ DeleteConfirmDialog with cascade deletion
10. ✓ Performance optimizations (IO dispatcher, async camera, thumbnails)
11. ✓ Phosphor Icons v1.0.0 integration

### Phase 5: Dashboard, Income & Navigation ✓
1. ✓ Dashboard screen (financial summary + widgets)
2. ✓ DashboardViewModel (combined expense/income summary)
3. ✓ Dashboard components (FinancialSummaryCard, RecentTransactionsWidget, TopCategoriesChart)
4. ✓ Income CRUD (modal bottom sheets)
5. ✓ IncomeFormFields with category support
6. ✓ Separate category system (13 expense, 10 income)
7. ✓ TransactionListScreen (combined expenses + income)
8. ✓ 4-item bottom navigation (Home, Transactions, Analytics, Settings)
9. ✓ Color-coded amounts (green income, red expense)
10. ✓ Currency fix (paiseToRupeeString with integer-only arithmetic)

### Phase 6: Accounts System ✓
1. ✓ AccountEntity + AccountDao (database layer)
2. ✓ Account domain model + repository
3. ✓ Replace payment methods with accounts in expenses/income
4. ✓ AccountViewModel with state management
5. ✓ Account management screens (list, add, edit, delete)
6. ✓ AccountDropdown + AccountSelectionDialog
7. ✓ Account display in transaction lists (date • account name)
8. ✓ Delete with reassignment logic
9. ✓ Seed default "My Account" (Bank type)

### Phase 7: Theme Management & Settings ✓
1. ✓ PreferencesRepository + DataStore integration
2. ✓ AppTheme enum (LIGHT/DARK/SYSTEM)
3. ✓ SettingsViewModel (theme persistence)
4. ✓ Real-time theme switching
5. ✓ SemanticColors (WCAG AA compliant financial colors)
6. ✓ BudgetColors (good/warning/critical)
7. ✓ ThemeSegmentedButton component
8. ✓ Enhanced SettingsScreen (4 sections)
9. ✓ AboutScreen (app info, version, links)

### Phase 7.5: UI Refinements & Code Quality ✓
1. ✓ Keyboard handling (PredictiveBackHandler)
2. ✓ WindowInsets integration (keyboard detection)
3. ✓ GenericGridSelectionDialog (code deduplication)
4. ✓ FormActionButtons component
5. ✓ State extraction (ExpenseUiState, ExpenseFormState)
6. ✓ Dimens.kt (standardized spacing)
7. ✓ Rename "Uncategorized" → "Others"
8. ✓ App icon update

### Phase 8: Recurring Transactions, Budgets & Filters ✓
1. ✓ BudgetNotificationService (75%/100% thresholds)
2. ✓ BudgetNotificationWorker (WorkManager, 6-hour checks)
3. ✓ Android 13+ notification permissions (POST_NOTIFICATIONS)
4. ✓ RecurringTransactionViewModel + CRUD UI
5. ✓ RecurringTransactionWorker (daily processing)
6. ✓ Recurring screens (list, add, edit)
7. ✓ TransactionTypeSelectionButton (Expense/Income)
8. ✓ FrequencySelectionButton (Daily/Weekly/Monthly)
9. ✓ FilterBottomSheet (date range, categories, accounts)
10. ✓ Filter integration in expense/income lists
11. ✓ Active filter chips and badges

### Phase 9: SMS Auto-Detection ✓
1. ✓ SmsReceiver BroadcastReceiver (SMS_RECEIVED)
2. ✓ SmsTransactionCreationWorker (auto-create in background)
3. ✓ SmsParser (HDFC, ICICI, SBI, Axis, Kotak + UPI formats)
4. ✓ Confidence scoring (0.7 threshold)
5. ✓ SMS metadata storage (smsSourceId, smsBody, smsConfidence, smsTimestamp)
6. ✓ Auto-create transactions directly (no review queue)
7. ✓ SmsNotificationService (transaction created alerts)
8. ✓ Settings integration (SMS toggle)
9. ✓ ProGuard rules for Telephony API
10. ✓ SMS metadata preservation during edits
11. ✓ SmsMetadataTest instrumented tests (12 test cases)
12. ✓ Phase 10 cleanup (removed old review workflow remnants)

### Phase 10: Financial Insights & Period Selection ✓
1. ✓ YearType enum (Calendar vs Financial Year)
2. ✓ Month/year selector in dashboard (MonthPickerDialog)
3. ✓ DashboardViewModel with month selection state
4. ✓ Financial summary card with Full Year and monthly views
5. ✓ Year type toggle (Cal/FY segmented button in summary card)
6. ✓ PreferencesRepository year type persistence
7. ✓ Database migration v1→v2 (unified category system)
8. ✓ Remove category type distinction (expense/income)
9. ✓ Consolidate duplicate categories (Others, Investments, Gifts, Rent)
10. ✓ Update all ViewModels for unified categories
11. ✓ DateTimePickerField and DateRangePickerModal components
12. ✓ YearTypeSelectionDialog component

### Phase 10.5: Security - App Lock ✓
**Priority:** High (security feature)
**Estimated Effort:** Medium

**Goal:** Secure app with biometric authentication and configurable lock timeout

1. ✓ BiometricAuthManager utility for biometric authentication
2. ✓ LockTimeout enum (Immediately, 1min, 5min, 15min)
3. ✓ AppLockViewModel with lifecycle-aware lock state management
4. ✓ LockScreen composable with biometric prompt
5. ✓ LockTimeoutDropdown component for settings
6. ✓ PreferencesRepository app lock settings (enabled, timeout)
7. ✓ Settings screen integration with app lock toggle
8. ✓ MainActivity lifecycle callbacks for lock timeout tracking
9. ✓ Lock screen overlay that blocks UI until authenticated
10. ✓ Automatic biometric prompt on lock screen appearance

**User Value:**
- Protect sensitive financial data with biometric security
- Flexible timeout options for convenience vs security balance
- Seamless integration with device biometrics (fingerprint/face)

### Phase 10.6: App Initialization & Loading ✓
**Priority:** High (performance and UX improvement)
**Estimated Effort:** Medium

**Goal:** Ensure app UI loads only after all required data is ready, preventing timing issues and race conditions

1. ✓ InitializationRepository interface and implementation
2. ✓ Parallel data loading (master data + preferences)
3. ✓ InitializationViewModel with eager initialization
4. ✓ SplashScreen composable with loading/error states
5. ✓ MainActivity integration with initialization flow
6. ✓ SpendlyApplication cleanup (remove redundant initialization)
7. ✓ Dependency injection setup for InitializationRepository
8. ✓ Background recurring transaction processing after initialization
9. ✓ Error handling with retry mechanism
10. ✓ Seamless integration with app lock functionality

**User Value:**
- Eliminates loading/timing issues and race conditions

### Phase 11: Analytics & Insights ✓
**Priority:** High (user-requested feature)
**Estimated Effort:** Medium-Large
**Status:** Completed (2025-12-20)
**Version:** 0.7.0-beta

**Goal:** Provide visual insights into spending patterns with custom Canvas-based charts

1. ✓ Create AnalyticsViewModel with data aggregation
2. ✓ Category spending pie chart (donut style, tap interactions)
3. ✓ Spending trend line chart (income/expense/net worth)
4. ✓ Time period selection (Month/Financial Year/Calendar Year)
5. ✓ Category analysis with percentages
6. ✓ Monthly comparison bar chart functionality
7. ✓ Summary cards with total spending insights
8. ✓ Chart animations (600ms pie entry, 800ms line drawing)
9. ✓ Tap gesture handling for interactions
10. ✓ Accessibility support (TalkBack semantics)
11. ✓ ChartDataTransformer utility for data processing
12. ✓ ChartMath utility for geometric calculations
13. ✓ Performance optimization (60 FPS, <100ms render time)
14. ✓ Comprehensive testing (34 tests: 13 unit + 21 instrumented)

**Components:**
- CustomPieChart: Donut-style with 55% center hole, 2° gaps, tap-to-show-details
- CustomLineChart: Multi-line with tap interactions, smart axis labels, currency formatting
- ChartGestureHandler: Touch interaction manager
- ChartMath: Geometric calculations for chart rendering

**User Value:**
- Understand spending habits with interactive visualizations
- Identify trends over different time periods
- Make informed financial decisions based on category analysis
- Full accessibility support for all users

**Technical Notes:**
- Migrated from Vico v2.0.0-alpha.28 to custom Canvas implementation for production stability
- Optimized rendering with memoization for smooth 60 FPS performance
- Full accessibility semantics for screen readers
- Comprehensive test coverage (unit + UI + accessibility)
- Provides proper splash screen during app startup
- Ensures data availability before UI renders
- Improved perceived performance with parallel loading (500-800ms startup)

### Phase 13: Data Export/Import ✓
**Priority:** High (data portability)
**Estimated Effort:** Medium
**Status:** Completed (2025-12-21)
**Version:** 0.8.0-beta

**Goal:** Allow users to backup and restore their complete app data

1. ✓ Create ExportImportRepository interface and implementation
2. ✓ JSON export with all app data (expenses, income, receipts, budgets, recurring transactions, categories, accounts)
3. ✓ Receipt files encoded as Base64 within export for portability
4. ✓ Export metadata (app version, database version, timestamp, record counts)
5. ✓ JSON import with comprehensive validation
6. ✓ Foreign key constraint checking during import
7. ✓ Progress tracking with cancellation support
8. ✓ ID remapping for categories and accounts during import
9. ✓ Receipt file restoration from Base64 data
10. ✓ DataManagementViewModel with state management
11. ✓ Data Management screen UI with export/import buttons
12. ✓ Progress dialogs with step-by-step feedback
13. ✓ Success/error handling with detailed messages
14. ✓ 500MB file size validation
15. ✓ Integration with Kotlinx Serialization

**Components:**
- ExportImportRepository: Core data export/import logic
- SpendlyExport: Comprehensive data model for export format
- DataManagementViewModel: State management and orchestration
- DataManagementScreen: UI for export/import operations
- Progress tracking: Step-by-step import process with cancellation

**User Value:**
- Complete data backup for safety
- Easy migration to new devices
- Data portability and ownership
- Peace of mind about data loss

**Technical Notes:**
- Uses Kotlinx Serialization for robust JSON handling
- Receipt files limited to 500MB total in export
- Automatic ID remapping ensures data integrity
- Proper transaction management during import
- Comprehensive validation before data operations

---

## 📋 Pending Features (Prioritized for Incremental Value)

### Phase 12: Calendar View
**Priority:** Medium (visual timeline for transactions)
**Estimated Effort:** Medium

**Goal:** Visual calendar with transaction indicators

1. Create calendar Compose component (Material 3 DatePicker or custom)
2. User-configurable view mode (expenses/income/both) in DataStore
3. Transaction badges on calendar dates (different colors)
4. Day view BottomSheet with transaction details
5. Month navigation with IconButtons
6. Date range selection (DateRangePicker)
7. Toggle for display mode switching
8. Settings preference for default calendar mode

**User Value:** Easy visual tracking of spending patterns over time

### Phase 14: Search Functionality
**Priority:** Medium (improves discoverability)
**Estimated Effort:** Small-Medium

**Goal:** Fast global search across transactions

1. Implement Room FTS (Full-Text Search) for transactions
2. Create global search field in app bar
3. Search result highlighting (AnnotatedString)
4. Filter search by type (expense/income/both)
5. Search history in DataStore
6. Recent searches dropdown

**User Value:** Quickly find specific transactions by description or merchant

### Phase 15: Category & Tag Management
**Priority:** Low (power user feature)
**Estimated Effort:** Medium

**Goal:** Allow custom categories and tagging

1. Category management screen (list, add, edit, delete)
2. Custom category creation with Phosphor Icons picker
3. Custom category color picker (Compose ColorPicker)
4. Category delete with transaction reassignment
5. Tag management screen (CRUD operations)
6. Tag assignment to transactions (junction table)
7. Tag-based filtering with FilterChip
8. Saved filter presets in DataStore

**User Value:** Personalize categories and organize transactions flexibly

### Phase 16: Performance Optimization
**Priority:** Medium (scalability)
**Estimated Effort:** Small-Medium

**Goal:** Handle large transaction volumes efficiently

1. Optimize database queries with proper indexes (review existing)
2. Implement pagination (Paging 3 library) for transaction lists
3. Optimize chart rendering for large datasets
4. Lazy loading for receipt images (Coil - already implemented)
5. Optimize app startup time (App Startup library)
6. Memory profiling with LeakCanary
7. Performance testing with 1000+ transactions

**User Value:** Smooth experience even with years of transaction data

### Phase 17: Security & Data Protection
**Priority:** High (before production release)
**Estimated Effort:** Medium-Large

**Goal:** Protect sensitive financial data

1. Implement SQLCipher for database encryption
2. App lock/PIN protection option
3. Biometric authentication (BiometricPrompt API)
4. Encrypt receipt files on disk
5. EncryptedSharedPreferences for sensitive settings
6. Security testing and audit
7. Add security documentation

**User Value:** Peace of mind about financial data privacy

### Phase 18: Notification Enhancements
**Priority:** Low (nice to have)
**Estimated Effort:** Small

**Goal:** Improve notification system

1. Recurring transaction reminder notifications
2. Notification settings (enable/disable by type)
3. Custom notification schedules
4. Notification action buttons (mark as paid, snooze)
5. Notification summary for daily/weekly recap

**User Value:** Proactive reminders for recurring expenses

### Phase 19: Settings Enhancements
**Priority:** Low (convenience)
**Estimated Effort:** Small

**Goal:** More user preferences

1. Default account setting (select from user's accounts)
2. Notification preferences (enable/disable, budget alerts)
3. Calendar view mode default preference
4. SMS auto-detection settings (per-bank toggle)
5. Data management options (backup frequency)
6. Default category preferences

**User Value:** Personalized app behavior

### Phase 20: Documentation & Polish
**Priority:** Medium (before production)
**Estimated Effort:** Small-Medium

**Goal:** Professional user experience

1. User documentation/help section in Compose
2. Developer documentation for codebase
3. Inline code comments for complex logic
4. Onboarding tutorial for first-time users (ViewPager2)
5. Empty state designs for all views (mostly done)
6. Error handling improvements
7. Loading states and progress indicators (mostly done)

**User Value:** Easy to learn and use

### Phase 21: Testing & Quality Assurance
**Priority:** High (before production)
**Estimated Effort:** Large

**Goal:** Ensure reliability and quality

1. Unit tests for database operations (expand existing)
2. Unit tests for transaction calculations
3. Unit tests for budget calculations
4. Instrumented tests for workflows (expand existing)
5. SMS auto-detection testing (various bank formats)
6. Data import/export testing
7. UI/UX testing on various devices
8. Offline functionality testing
9. Notification delivery testing
10. Security testing

**User Value:** Reliable, bug-free experience

### Phase 22: Build & Deployment
**Priority:** High (for release)
**Estimated Effort:** Small-Medium

**Goal:** Prepare for production release

1. Configure signing and release configuration
2. Test release builds on physical devices
3. Create release APK/AAB
4. Final QA on release build
5. Prepare app store assets (screenshots, description)
6. Google Play Store listing
7. Internal testing track
8. Beta testing track
9. Production release

**User Value:** Access to stable production app

---

## 🚀 Future Enhancements (Post-MVP)

These features are planned for future releases after the initial production launch:

1. **Widget Support** - Android home screen widgets using Glance
2. **Advanced Analytics** - AI-powered spending insights
3. **Split Transactions** - Divide single transaction across categories
4. **Merchant Management** - Save and categorize frequent merchants
5. **Custom Report Builder** - User-defined reports with filters
6. **Cash Flow Projections** - Predictive analytics for future spending
7. **Multi-Account Transfers** - Track transfers between accounts
8. **Receipt OCR** - Automatically extract data from receipt images
9. **Spending Limits** - Daily/weekly spending caps with alerts
10. **Bill Reminders** - Recurring bill due date notifications

---

## 🚫 Out of Scope (Never Implement)

These features are explicitly excluded from the project scope:

- Bank account integration/auto-import
- Cloud synchronization across devices
- Bill splitting with friends (collaborative features)
- Investment portfolio tracking
- iOS version
- Multi-currency support (INR only)
- Cryptocurrency tracking
- Credit score monitoring

---

## Development Notes

### Completed Milestones
- ✅ v0.1.0-alpha: Basic expense tracking
- ✅ v0.2.0-alpha: Dashboard + income tracking
- ✅ v0.2.1-alpha: Account system + theme management
- ✅ v0.3.0-beta: Recurring transactions + budgets + filters
- ✅ v0.3.1-beta: SMS auto-detection complete
- ✅ v0.4.0-beta → v0.4.4-beta: CI/CD workflow improvements, documentation cleanup
- ✅ v0.5.0-beta: Financial insights with period selection + unified categories
- ✅ v0.5.1-beta: UI/UX improvements, bottom menu, dropdowns
- ✅ v0.6.0-beta: App lock with biometric authentication + centralized initialization with splash screen
- ✅ v0.7.0-beta: Advanced analytics with custom Canvas charts
- ✅ v0.8.0-beta: Data export/import (JSON backup & restore)

### Next Milestones
- 🎯 v0.9.0: Calendar view
- 🎯 v0.10.0: CSV export + PDF reports
- 🎯 v0.11.0: Search functionality
- 🎯 v1.0.0-rc: Performance optimization + security + comprehensive testing
- 🎯 v1.0.0: Production release

### Technical Debt
- None currently identified

### Known Issues
- None currently blocking

---

## Contributing

See individual phase tasks for implementation details. All new features should:
1. Follow MVVM architecture
2. Use Jetpack Compose for UI
3. Include unit tests
4. Update documentation
5. Follow existing code style
6. Use proper database migrations (see MIGRATIONS.md)
