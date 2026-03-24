# Changelog

All notable changes to Spendly will be documented in this file.

---

## [0.9.2-beta] - 2026-03-24

### Added
- Added heuristic SMS account matching to improve account auto-selection from sender/body hints
- Added SMS deduplication across realtime processing and historical scans using strict and semantic fingerprint checks

### Changed
- Improved Scapia/Federal SMS parsing for transaction type detection, account hint extraction, and merchant parsing
- Improved SMS category inference with rule-based keyword scoring and deterministic fallback behavior
- Bumped app version to `0.9.2-beta` with `versionCode` 92
- Updated release metadata across build config and documentation

---

## [0.9.1-beta] - 2026-03-23

### Changed
- Bumped app version to `0.9.1-beta` with `versionCode` 91
- Updated release metadata across build config, docs, and Play release notes

---

## [0.9.0-beta] - 2026-03-22

### Changed
- Bumped app version to `0.9.0-beta` with `versionCode` 90
- Updated release metadata across build config, docs, and Play release notes

---

## [0.8.0-beta] - 2025-12-22

### Added
- Track expenses and income with categories, accounts, notes, and timestamps
- Attach receipts (photos or PDFs) to transactions
- SMS auto-detection for Indian banks and UPI with toggle control in settings
- Interactive pie chart showing spending breakdown by category with tap interactions
- Line chart for income/expense trends over time with interactive tap popups
- View analytics for financial year, or calendar year
- Create custom accounts (Bank, Card, Wallet, Cash, Loan, Investment)
- Track balance and transaction history per account
- 19 predefined categories with for expenses and income
- Set monthly budgets by category with 75%/100% notification thresholds
- Set up daily, weekly, or monthly recurring transactions with auto-creation
- Export all data to JSON format (includes receipts as Base64)
- Import data from JSON backup with validation
- Biometric app lock with configurable timeout (immediate, 1min, 5min, 15min)
- Light/Dark/System theme support
- Dashboard with month/year selector and financial summary
- Filter transactions by date range, category, and account
- Material 3 design with adaptive layouts
- Fast app startup (500-800ms) with parallel data loading
- Optimized chart rendering at 60 FPS

---

## Links

- **Repository**: https://github.com/lanthoor/spendly
- **Issues**: https://github.com/lanthoor/spendly/issues

---

[0.9.2-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.2-beta
[0.9.1-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.1-beta
[0.9.0-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.0-beta
[0.8.0-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.8.0-beta
