# Spendly - Personal Expense Tracker Execution Plan

## Project Setup & Infrastructure
1. ✓ Initialize Android project with Kotlin and Jetpack Compose
2. ✓ Configure Gradle build system with version catalogs
3. Add Room database dependency and configure
4. Add Vico chart library dependency
5. Add DataStore for preferences management
6. Configure Hilt for dependency injection
7. Set up build variants (debug/release) and ProGuard rules
8. Create project package structure (ui, data, domain, utils)
9. Set up GitHub Actions workflow for CI/CD
10. Configure GitHub Actions to run on push and pull requests
11. Add build job to GitHub Actions (./gradlew build)
12. Add unit test job to GitHub Actions (./gradlew test)
13. Add lint check job to GitHub Actions (./gradlew lint)
14. Add instrumented test job to GitHub Actions (./gradlew connectedAndroidTest) with Android emulator
15. Configure caching for Gradle dependencies in GitHub Actions
16. Add status badge to README.md for build status

## Database Design & Implementation
17. Create Room database class and version management with SQLCipher encryption
18. Create Expense entity with Room annotations (id: Long, amount: Long (paise), category_id: Long nullable (default Uncategorized), date: Long (timestamp), description: String, payment_method: String (enum: Cash/UPI/Debit Card/Credit Card/Net Banking/Wallet), created_at: Long, modified_at: Long)
19. Create Receipt entity (id: Long, expense_id: Long, file_path: String, file_type: String (JPG/PNG/WebP/PDF), file_size_bytes: Long max 5MB, compressed: Boolean)
20. Create Income entity (id: Long, amount: Long (paise), source: String, date: Long, description: String, is_recurring: Boolean, linked_expense_id: Long nullable (for refunds), created_at: Long, modified_at: Long)
21. Create Category entity (id: Long, name: String, icon: String (Material Icon name), color: Int, is_custom: Boolean, sort_order: Int) - includes 13 predefined + Uncategorized
22. Create Budget entity (id: Long, category_id: Long nullable (null = overall budget), amount: Long (paise), month: Int, year: Int, notification_75_sent: Boolean, notification_100_sent: Boolean)
23. Create RecurringTransaction entity (id: Long, transaction_type: String (expense/income), amount: Long (paise), category_id: Long, description: String, frequency: String (daily/weekly/monthly), next_date: Long, last_processed: Long nullable)
24. Create Tag entity and TransactionTag junction entity with cross-references (many-to-many)
25. Implement Room migrations for database versioning
26. Create DAO interfaces for each entity with CRUD operations, use Flow for reactive queries
27. Implement database backup and restore using Room export/import to JSON with metadata (version, export_date, currency: INR)

## Core Data Models & State Management
28. Create Kotlin data classes for Expense model with amount helper functions (fromPaise, toPaise, displayAmount)
29. Create Kotlin data classes for Income model with refund linking support
30. Create Kotlin data classes for Receipt model
31. Create Kotlin data classes for Category model with predefined list constant
32. Create Kotlin data classes for Budget model
33. Create Kotlin data classes for Tag model
34. Create Repository classes for expenses with Flow/StateFlow, default sort by date DESC (newest first)
35. Create Repository classes for income with Flow/StateFlow
36. Create Repository classes for categories with Flow/StateFlow, include predefined seed data
37. Create Repository classes for budgets with Flow/StateFlow, include notification tracking
38. Set up DataStore for application settings (theme: Light/Dark/System, default_payment_method, notification_preferences, calendar_view_mode: expenses/income/both)

## Expense Management Features
39. Create ExpenseViewModel with StateFlow for UI state
40. Create expense entry Compose form with validation (category optional, defaults to Uncategorized)
41. Implement add expense functionality with Room insert, store amount in paise (Long)
42. Implement edit expense functionality with Room update, track modified_at timestamp
43. Implement delete expense with Material 3 confirmation dialog
44. Create expense list LazyColumn with default sort: newest first (date DESC)
45. Implement receipt attachment using ActivityResultContracts for file picker (JPG, PNG, WebP, PDF)
46. Implement receipt photo capture using CameraX
47. Implement receipt image compression to 1920px max width, 85% quality, max 5MB per file
48. Create receipt storage using internal storage directory with encryption
49. Support unlimited receipts per expense (one-to-many relationship)
50. Implement recurring expense setup Compose UI (daily, weekly, monthly frequency)
51. Create app startup check for recurring expense processing (check last 3 months for missed occurrences)
52. Implement expense search functionality with Room FTS (Full-Text Search)
53. Implement expense filter by date range using DateRangePicker
54. Implement expense filter by category using FilterChip
55. Implement expense filter by payment method using dropdown menu (Cash/UPI/Debit Card/Credit Card/Net Banking/Wallet)

## Income Tracking Features
56. Create IncomeViewModel with StateFlow
57. Create income entry Compose form with refund/return linking option
58. Implement add income functionality with Room, store amount in paise (Long)
59. Implement edit income functionality
60. Implement delete income functionality
61. Create income list LazyColumn
62. Implement recurring income setup Compose UI (daily, weekly, monthly)
63. Create app startup check for recurring income processing (check last 3 months)
64. Implement income source categorization (Salary, Freelance, Investments, Refund/Return, Other)
65. Create Refund/Return income type with expense linking (linked_expense_id field)
66. Create income vs expense comparison Compose view with charts

## Categories & Tags System
67. Implement predefined categories seed data (13 categories): Food & Dining (restaurant icon), Travel (flight icon), Rent (home icon), Utilities (lightbulb icon), Services (build icon), Shopping (shopping_cart icon), Entertainment (movie icon), Healthcare (local_hospital icon), Gifts (card_giftcard icon), Education (school icon), Investments (trending_up icon), Groceries (local_grocery_store icon), Others (more_horiz icon), plus Uncategorized (category icon)
68. Create category management Compose screen
69. Implement custom category creation form with Material Icons picker
70. Implement custom category color picker using Compose ColorPicker
71. Use Material Icons library for category icons (icon stored as String name)
72. Implement category edit functionality in ViewModel
73. Implement category delete with transaction reassignment dialog - user must select replacement category (including Uncategorized option)
74. Create tag management Compose screen
75. Implement tag creation functionality
76. Implement tag assignment to transactions using junction table (many-to-many)
77. Create tag-based filtering using FilterChip and Room queries

## Budget Management Features
78. Create BudgetViewModel with budget calculation logic (amounts in paise)
79. Create budget setup Compose form with category selection dropdown (null = overall budget)
80. Implement monthly budget creation (per category or overall)
81. Implement budget edit functionality
82. Implement budget delete functionality
83. Create budget vs actual spending calculation using Room aggregate queries
84. Create LinearProgressIndicator component for budget progress with color coding
85. Implement budget overview dashboard Card composables
86. Create overspending alert logic in ViewModel - trigger at 75% and 100% thresholds
87. Implement budget notification system using NotificationCompat with notification_75_sent and notification_100_sent flags
88. Create budget comparison Compose view with Vico charts

## Analytics & Reporting
89. Implement data aggregation repository functions using Room queries (amounts in paise, convert for display)
90. Create pie chart Compose component using Vico for category breakdown
91. Create bar chart for monthly spending comparison using Vico
92. Create line chart for spending trends over time using Vico
93. Implement time period selector using SegmentedButton or TabRow
94. Create category-wise spending analysis Compose screen
95. Implement income vs expense trend analysis with Vico charts
96. Create monthly summary report generator using Kotlin
97. Create yearly summary report generator
98. Implement PDF export using PdfDocument API for monthly/yearly summaries
99. Implement CSV export - fields: Date, Amount, Category, Description, Payment Method, Tags (comma-separated)
100. Create spending insights section within Analytics screen showing:
    - Top spending category for selected period
    - Month-over-month spending trend (increase/decrease %)
    - Budget vs actual comparison with visual indicators

## Dashboard & Main UI
101. ✓ Design main dashboard layout with Scaffold - set as landing screen (first screen on app open)
102. Create spending overview Card composable (total spent this month in INR, converted from paise)
103. Create budget status Card composable
104. Create recent transactions LazyColumn widget showing last 5 transactions with 'View All' link
105. Create top spending categories Card with mini Vico chart
106. Create FloatingActionButton for quick add expense
107. Implement pull-to-refresh using PullToRefreshBox
108. ✓ Use NavigationSuiteScaffold for adaptive navigation (bottom bar/rail/drawer)
109. Update AppDestinations enum: Home (Dashboard), Analytics (with Insights), Profile/Settings
110. ✓ Implement responsive layout using WindowSizeClass for tablets

## Calendar & Timeline Views
111. Create calendar Compose component using Material 3 DatePicker or custom calendar
112. Implement user-configurable calendar view mode stored in DataStore (expenses only / income only / both)
113. Implement transaction display on calendar dates with badges (different colors/icons for expense vs income)
114. Create day view with transaction details in BottomSheet
115. Implement month navigation with IconButtons
116. Create timeline/history LazyColumn of all transactions
117. Implement date range selection using DateRangePicker
118. Add toggle in calendar view to switch between display modes (expenses/income/both)

## Search & Filter System
119. Implement global search using Room FTS across all transactions
120. Create advanced filter ModalBottomSheet or Dialog
121. Implement filter by multiple categories using FilterChip group
122. Implement filter by amount range using RangeSlider
123. Implement filter by payment method using dropdown
124. Implement filter by tags using FilterChip
125. Create saved filter presets using DataStore
126. Implement search result highlighting using AnnotatedString

## Currency Configuration (REMOVED - INR Only)
~~127-132: Multi-currency support removed. App uses INR only. All amounts stored in paise (Long), displayed in ₹ format.~~

## SMS Auto-Detection Feature
133. Request SMS read permissions (Android READ_SMS)
134. Create SMS listener service using BroadcastReceiver (triggered on new SMS received)
135. Implement expense detection regex patterns for all major Indian banks (SBI, HDFC, ICICI, Axis, Kotak, PNB, BOB, Canara, Union, IDBI)
136. Implement expense detection regex patterns for UPI (NPCI, BHIM, PayTM, PhonePe, GPay)
137. Implement expense detection regex patterns for credit cards (Scapia, Federal Bank, etc.)
138. Implement income detection regex patterns (salary credits, refunds, UPI received)
139. Create SMS parsing engine to extract amount (parse to paise), date, merchant/description
140. Auto-create expense/income transactions from SMS (fully editable and deletable, no special restrictions)
141. Create SMS detection settings and toggle in app preferences
142. Store SMS source info in transaction description or metadata for reference

## Data Import/Export
143. Implement JSON export - single file with metadata section (version, export_date, currency: INR) and nested arrays for all entities
144. Implement JSON import functionality with validation and version checking
145. Implement CSV export for expenses - columns: Date, Amount (in ₹), Category, Description, Payment Method, Tags (comma-separated)
146. Implement CSV export for income - columns: Date, Amount (in ₹), Source, Description, Linked Expense ID
147. Implement CSV import with column mapping UI
148. Create data export settings (select date range, categories, transaction types)
149. Implement data validation during import (check amount formats, date formats, category existence)

## Theme & UI Customization
150. Implement dark theme color scheme using Material 3 dynamic colors
151. Implement light theme color scheme using Material 3 dynamic colors
152. Create theme selector in settings with three options: Light, Dark, System Default
153. Implement theme persistence using DataStore
154. Apply theme colors to all components (follow current SpendlyTheme pattern)
155. Create theme-aware Vico chart colors

## Notification System
156. Implement Android notification permissions handling
157. Create notification service using Android NotificationManager
158. Implement budget overspending notifications
159. Implement recurring transaction reminder notifications
160. Create notification settings and preferences
161. Implement notification scheduling for recurring alerts

## Settings & Preferences
162. Create settings screen layout using Compose (part of Profile navigation destination)
163. ~~Remove currency preference setting (INR only)~~
164. Implement default payment method setting (Cash/UPI/Debit Card/Credit Card/Net Banking/Wallet)
165. Implement notification preferences (enable/disable, budget alerts 75%/100%)
166. Implement theme selector (Light/Dark/System Default)
167. Implement calendar view mode preference (expenses/income/both)
168. Implement SMS auto-detection toggle
169. Implement data management section (backup to JSON, restore from JSON, clear all data with confirmation)
170. Implement about section with app version
171. Create export data options in settings (JSON or CSV with date range selection)
172. Create import data option in settings (JSON or CSV with validation)

## Security & Data Protection
173. Implement SQLite database encryption using SQLCipher
174. Create app lock/PIN protection option
175. Implement biometric authentication using BiometricPrompt
176. Create data encryption for receipt files
177. Implement secure storage for sensitive settings using EncryptedSharedPreferences

## Testing & Quality Assurance
178. Write unit tests for database operations
179. Write unit tests for transaction calculations
180. Write unit tests for budget calculations
181. Write instrumented tests for expense workflows
182. Write instrumented tests for income workflows
183. Test SMS auto-detection with various bank formats
184. Test data import/export functionality
185. Remove multi-currency test (not applicable - INR only)
186. Perform UI/UX testing on various Android devices
187. Test offline functionality thoroughly
188. Test notification delivery and timing
189. Perform security testing for data encryption

## Performance Optimization
190. Optimize database queries for large transaction volumes using indexes
191. Implement pagination for transaction lists using Paging 3 library
192. Optimize chart rendering for large datasets
193. Implement lazy loading for receipt images using Coil
194. Optimize app startup time with App Startup library
195. Reduce memory footprint and monitor with LeakCanary

## Documentation & Polish
196. Create user documentation/help section in Compose
197. Write developer documentation for codebase
198. Create inline code comments for complex logic
199. Design app icon and branding
200. Create onboarding tutorial for first-time users using ViewPager2
201. Implement empty state designs for all views
202. Create error handling and user-friendly error messages
203. Implement loading states and progress indicators

## Build & Deployment
204. Configure Android build settings and Gradle configuration
205. Set up app signing and release configuration
206. Test debug builds on physical Android devices
207. Create release build APK/AAB
208. Perform final QA on release build
209. Prepare app store assets (screenshots, description)
210. Submit to Google Play Store (optional - deferred to end)

## Future Enhancements (Post-MVP)
211. Add widget support for Android home screen using Glance
212. Implement data backup to local file system
213. Create advanced analytics with AI-powered insights
214. Add support for split transactions
215. Implement merchant/payee management
216. Create custom report builder
217. Add support for cash flow projections
