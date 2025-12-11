# Personal Expense Tracker - Feature List & Scope

[![Android CI](https://github.com/lanthoor/spendly/actions/workflows/android.yml/badge.svg)](https://github.com/lanthoor/spendly/actions/workflows/android.yml)

## Core Features

### 1. **Expense Management**
- Add, edit, and delete expense entries
- Capture: amount (stored as paise/smallest unit), category (optional, defaults to Misc), date, description, payment method
- **Payment methods:** Cash, UPI, Debit Card, Credit Card, Net Banking, Wallet (predefined)
- Support for recurring expenses (daily, weekly, monthly) - processed at app startup, checks last 3 months
- Attach unlimited receipts (JPG, PNG, WebP, PDF) - compressed to 1920px max, 5MB per file
- Automatic detection of expenses from SMS (auto-creates transactions, fully editable/deletable)
- **SMS support:** All major Indian banks + UPI formats + credit cards (Scapia, Federal Bank, etc.)
- Default sort: Newest first

### 2. **Income Tracking**
- Record income sources and amounts
- Track salary, freelance, investments, refunds/returns (linked to original expenses)
- Recurring income support (processed at app startup)
- Automatic detection of income from SMS
- **Refunds:** Show as income with dedicated category, link to original expense transaction

### 3. **Categories & Tags**
- **Predefined categories (13):** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Media, Healthcare, Gifts, Education, Investments, Groceries, Misc (default for unassigned)
- Each category has predefined Material Icon and customizable color
- Custom category creation with icon picker and colors
- Tagging system for cross-category organization
- Category deletion requires reassignment to another category (including Misc)

### 4. **Budget Management**
- Set monthly budgets (per category or overall)
- Budget vs. actual spending comparison
- Visual progress bars (LinearProgressIndicator)
- **Overspending notifications:** Alert at 75% threshold and at 100% (budget exceeded)

### 5. **Analytics & Insights**
- Interactive charts (pie, bar, line graphs) using Vico library
- **Spending insights:** Top spending category, spending trends (month-over-month), budget vs actual comparison
- Insights integrated as part of Analytics screen
- Category breakdowns
- Monthly/yearly comparisons
- **Export reports:**
  - PDF: Monthly/yearly summaries
  - CSV: All fields (Date, Amount, Category, Description, Payment Method, Tags)

### 6. **Data Management**
- Local SQLite database storage
- Data export/import (JSON with metadata, CSV)
- **Currency:** INR only (no multi-currency support needed)

### 7. **User Interface**
- **Dashboard (landing screen):** Spending overview, recent 5 transactions, budget status, top spending categories
- **Calendar view:** User-configurable (expenses only, income only, or both) with visual indicators
- Search and filter functionality (global search, advanced filters)
- **Theme:** Three options - Light, Dark, or System Default
- **Navigation:** Material 3 Adaptive Navigation Suite (bottom bar/rail/drawer based on screen size)
- Main navigation: Home (Dashboard), Analytics (with insights), Profile/Settings

## Project Scope

### **In Scope:**
- Android-first native mobile application
- Offline-only architecture
- Local data persistence with encryption
- Responsive UI optimized for phones and tablets
- Basic notification system for budget alerts
- Automated CI/CD pipeline with GitHub Actions (build, test, lint)

### **Out of Scope (Never do these):**
- Bank account integration/auto-import
- Cloud synchronization across devices
- Bill splitting with friends
- Investment portfolio tracking
- iOS version (focus on Android MVP)

### **Technical Stack:**
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room (SQLite wrapper)
- **Charts:** Vico
- **Navigation:** Material 3 Adaptive Navigation Suite
- **Architecture:** MVVM with ViewModel and StateFlow
- **CI/CD:** GitHub Actions for automated build and testing

## Implementation Status

### ✅ Phase 1: Project Setup & Infrastructure (Complete)
- Basic project structure with Kotlin and Jetpack Compose
- Material 3 theming with dynamic color support
- All dependencies configured (Room, Vico, Hilt, DataStore)
- GitHub Actions CI/CD with 3 parallel jobs
- ProGuard rules configured for all libraries

### ✅ Phase 2: Database Foundation (Complete)
- SpendlyDatabase with 8 entities: Expense, Income, Category, Budget, Receipt, RecurringTransaction, Tag, TransactionTag
- 8 DAOs with Flow-based reactive queries
- Complete CRUD operations with complex queries
- Foreign keys with CASCADE/SET_NULL
- Integer-only currency (amounts in paise as Long)
- Audit timestamps (createdAt, modifiedAt)

### ✅ Phase 3: Repository Layer & Domain Models (Complete)
- 6 domain models: Expense, Income, Category, Budget, Tag, Receipt
- 5 repository interfaces: ExpenseRepository, IncomeRepository, CategoryRepository, BudgetRepository, TagRepository
- 5 repository implementations with full CRUD operations
- Hilt DI modules configured: DatabaseModule, RepositoryModule, DataStoreModule, AppModule
- SpendlyApplication with @HiltAndroidApp and category seeding on first launch

### ✅ Phase 4: Expense Management UI (Complete)
**Completed:**
- ✅ Navigation architecture with type-safe routes (Screen sealed class + SpendlyNavHost)
- ✅ ExpenseViewModel with complete state management (UI state, form state, filter state, receipt management)
- ✅ Shared UI components: AmountTextField, CategoryDropdown, PaymentMethodDropdown, DatePickerField, SpendlyTopAppBar, EmptyState, LoadingIndicator, IconMapper
- ✅ ExpenseFormFields reusable component for Add/Edit screens
- ✅ AddExpenseScreen: Modal bottom sheet with complete create flow and validation
- ✅ ExpenseListScreen: List view with empty/loading/error states, opens Add/Edit in bottom sheets
- ✅ EditExpenseScreen: Modal bottom sheet with complete update flow, pre-populated form, and receipt management
- ✅ ExpenseListItem: Material 3 list item with category icon, date, amount, payment method
- ✅ DeleteConfirmDialog: Confirmation dialog with cascade deletion
- ✅ CategorySelectionDialog: 3-column grid layout with icons, colors, and visual selection indicators
- ✅ PaymentMethodSelectionDialog: 3-column grid layout with payment method icons and visual selection
- ✅ MainActivity integration with NavigationSuiteScaffold
- ✅ Phosphor Icons integration (replaced Material Icons throughout app)
- ✅ InteractionSource-based click handling for read-only text fields
- ✅ All UI screens use modal bottom sheets instead of navigation for better UX
- ✅ **Receipt Management:** File picker, camera capture (CameraX 1.5.2), image compression (1920px max, 85% quality), internal storage
- ✅ **Receipt UI Components:** ReceiptThumbnail (Coil with size optimization), ReceiptPickerSheet, CameraCapture with async initialization
- ✅ **Receipt Repository:** Full CRUD operations with file deletion on receipt removal
- ✅ **Performance Optimizations:** IO dispatcher for file operations, async camera init, thumbnail sizing (240px for 120dp display), proper caching
- ✅ **Utilities:** FileUtils (file operations, size validation), ImageCompressor (EXIF-aware compression), PermissionUtils (camera permissions)
- ✅ Build successful, unit tests passing, instrumented tests passing

**UI/UX Improvements:**
- Modal bottom sheets for Add/Edit expense (better mobile experience)
- Grid-based selection dialogs for categories and payment methods
- Visual selection indicators (colored backgrounds + borders, no radio buttons)
- Icons displayed for all categories and payment methods
- Proper InteractionSource handling for clickable read-only fields
- Snackbar feedback with 2-second duration before navigation
- Full-screen camera preview with loading indicators
- Optimized image thumbnails with lazy loading
- Info cards guiding users to save expenses before attaching receipts

### ✅ Phase 5: Dashboard, Income Tracking & Navigation (Complete)
**Completed:**
- ✅ **Dashboard Screen:** Landing screen with financial summary, recent 5 transactions (mixed expenses/income), top 3 spending categories chart
- ✅ **DashboardViewModel:** Complete state management with combined financial summary (income/expenses/net balance), month-over-month percentages
- ✅ **Dashboard Components:** FinancialSummaryCard, RecentTransactionsWidget, TopCategoriesChart with Vico
- ✅ **Income Tracking:** Full CRUD operations with IncomeViewModel, AddIncomeScreen, EditIncomeScreen (modal bottom sheets)
- ✅ **Income Category System:** Separate categories for income (IDs 101-110) and expenses (IDs 1-13), using CategoryType enum
- ✅ **Income Components:** IncomeFormFields, IncomeListScreen, IncomeListItem, refund linking support
- ✅ **Database Migration v1→v2:** Added type column to categories table, seeded 10 income categories
- ✅ **Database Migration v2→v3:** Added category_id column to income table with foreign key
- ✅ **Navigation Restructuring:** 4-item bottom navigation (Home/Dashboard, Transactions, Analytics, Settings)
- ✅ **TransactionListScreen:** Shows all transactions (expenses + income) in chronological order with edit/delete
- ✅ **UI Improvements:** Color-coded amounts (green +income, red -expense), payment method display, fixed scientific notation
- ✅ **Currency Utils:** Integer-only paiseToRupeeString() function for guaranteed decimal notation
- ✅ **Enum Extensions:** toDisplayName() for PaymentMethod, toDisplayString() for IncomeSource with proper title case

**Income Categories (10):**
- Salary (briefcase), Freelance (laptop), Business (storefront), Investment (trending_up), Gift (gift)
- Refund (arrow_u_up_left), Rental (buildings), Interest (percent), Bonus (trophy), Other (dots_three)

**Deferred to Future Phases:**
- Recurring transactions UI and background processing
- Full-text search with Room FTS
- Filter bottom sheet UI (logic implemented in ViewModel)
- Budget management screens

### ⏳ Next: Phase 6 - Recurring Transactions, Search, Filters, Budget Management
