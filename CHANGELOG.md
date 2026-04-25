# Changelog

All notable changes to Spendly will be documented in this file.

---

## [0.9.6] - 2026-04-25

### Changed
- chore(architecture): decouple domain usecases from UI layer (#44)
- feat(transactions): add AI SMS enrichment flow (#45)
- chore(deps): bump org.jetbrains.kotlinx:kotlinx-serialization-json from 1.10.0 to 1.11.0 (#47)
- chore(deps): bump actions/create-github-app-token from 2.2.1 to 3.1.1 (#46)
- chore(deps): bump softprops/action-gh-release from 2.5.0 to 3.0.0 (#48)

---

## [0.9.5] - 2026-04-14

### Changed
- fix: cleanup and supply chain resilience (#31)
- docs: refactoring plan (#32)
- chore(architecture): phase 0 baseline and guardrails (#34)
- chore(architecture): phase 1 decompose utils ownership (#35)
- chore(architecture): phase 2 shared contracts decoupling (#36)
- chore(architecture): phase 3 file decomposition (#37)
- chore(architecture): phase 4 use case introduction (#38)
- chore(architecture): phase 5 package first isolation (#39)
- chore(architecture): phase 6 optional modularization (#40)
- chore(architecture): phase 7 hardening and cleanup (#41)
- docs: reconcile architecture migration plan status (#42)
- chore: add local agent skills manifests (#43)

---

## [0.9.4] - 2026-04-05

### Changed
- feat: improve mutual fund investment category detection (#20)
- chore(deps): bump actions/checkout from 6.0.1 to 6.0.2 (#21)
- chore(deps): bump github/codeql-action from 4.31.9 to 4.35.1 (#24)
- chore(deps): bump androidx.biometric:biometric from 1.4.0-alpha05 to 1.4.0-alpha06 (#26)
- chore(deps): bump androidx.core:core-ktx from 1.17.0 to 1.18.0 (#23)
- chore(ci): release preparation pipeline (#28)
- chore: bump version to 0.9.3
- chore(deps): bump org.jetbrains.kotlinx:kotlinx-serialization-json from 1.7.3 to 1.10.0 (#22)
- docs: minimize README and add AGPL + contributing guide (#29)
- chore(deps): bump android-actions/setup-android from 4.0.0 to 4.0.1 (#30)

---

## [0.9.3] - 2026-04-03

### Changed
- feat: improve mutual fund investment category detection (#20)
- chore(deps): bump actions/checkout from 6.0.1 to 6.0.2 (#21)
- chore(deps): bump github/codeql-action from 4.31.9 to 4.35.1 (#24)
- chore(deps): bump androidx.biometric:biometric from 1.4.0-alpha05 to 1.4.0-alpha06 (#26)
- chore(deps): bump androidx.core:core-ktx from 1.17.0 to 1.18.0 (#23)
- chore(ci): release preparation pipeline (#28)

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

[0.9.6]: https://github.com/lanthoor/spendly/releases/tag/v0.9.6
[0.9.5]: https://github.com/lanthoor/spendly/releases/tag/v0.9.5
[0.9.4]: https://github.com/lanthoor/spendly/releases/tag/v0.9.4
[0.9.3]: https://github.com/lanthoor/spendly/releases/tag/v0.9.3
[0.9.2-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.2-beta
[0.9.1-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.1-beta
[0.9.0-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.9.0-beta
[0.8.0-beta]: https://github.com/lanthoor/spendly/releases/tag/v0.8.0-beta
