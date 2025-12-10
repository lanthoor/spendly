# Personal Expense Tracker - Feature List & Scope

## Core Features

### 1. **Expense Management**
- Add, edit, and delete expense entries
- Capture: amount (stored as paise/smallest unit), category (optional, defaults to Uncategorized), date, description, payment method
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
- **Predefined categories (13):** Food & Dining, Travel, Rent, Utilities, Services, Shopping, Entertainment, Healthcare, Gifts, Education, Investments, Groceries, Others, Uncategorized (default for unassigned)
- Each category has predefined Material Icon and customizable color
- Custom category creation with icon picker and colors
- Tagging system for cross-category organization
- Category deletion requires reassignment to another category (including Uncategorized)

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
