# Package Boundaries

This document defines the architecture boundaries enforced during migration.

## Scope

- Package root: `dev.lanthoor.spendly`
- Source scope for checks: `app/src/main/java`
- Enforcement entrypoint: `./gradlew checkArchitectureBoundaries`

## Rules

### Rule 1: Domain must not import UI

- **Forbidden:** `domain -> ui` imports.
- **Why:** Domain contracts and logic must stay independent of presentation concerns.

Example:

- Forbidden: `dev.lanthoor.spendly.domain.*` importing `dev.lanthoor.spendly.ui.*`
- Allowed: `dev.lanthoor.spendly.ui.*` importing `dev.lanthoor.spendly.domain.*`

### Rule 2: Feature internals must not be imported cross-feature

- **Forbidden:** imports from `ui.screens.<featureB>.*` inside `ui.screens.<featureA>.*` when `featureA != featureB`.
- **Allowed exception:** imports from `ui.screens.<featureB>.api.*` are allowed as explicit public contracts.
- **Why:** Feature boundaries should be explicit and prevent hidden coupling.

Example:

- Forbidden: `ui.screens.transactions.*` importing `ui.screens.dashboard.RecentTransaction`
- Allowed: `ui.screens.settings.*` importing `ui.screens.budgets.api.BudgetManagementScreen`
- Allowed: `ui.navigation.*` importing feature screens for app-level routing

### Rule 3: No new data/enum type declarations under `utils`

- **Forbidden:** introducing new `data class` or `enum class` declarations under `utils` without explicit ownership migration.
- **Why:** `utils` should not continue to accumulate ownership-heavy domain/business types.

Example:

- Forbidden: adding `enum class` in `dev.lanthoor.spendly.utils.*` for business state
- Allowed: pure helper functions in `dev.lanthoor.spendly.utils.*`

### Rule 4: Do not import migrated ownership types from `utils`

- **Forbidden:** importing migrated ownership types/extensions from `utils` paths.
- **Why:** ownership has moved to explicit `core/model` and `core/ui` packages.

Current migrated paths:

- `core.model.finance`: `IncomeSource`, `RecurringFrequency`, `TransactionType`, `AccountType`
- `core.model.preferences`: `AppTheme`, `AppLanguage`, `YearType`, `TimePeriod`, `LockTimeout`
- `core.ui.format`: `toDisplayString`, `toDisplayName`, `getDefaultIcon`, `getDisplayRange`, `getDateRange`, `displayNameRes`

Example:

- Forbidden: `import dev.lanthoor.spendly.utils.YearType`
- Allowed: `import dev.lanthoor.spendly.core.model.preferences.YearType`

## Baseline Policy

Current legacy violations are tracked in `config/architecture/boundary-baseline.txt`.

- Regenerate baseline: `./gradlew generateArchitectureBoundaryBaseline`
- Verify no new violations: `./gradlew checkArchitectureBoundaries`

CI fails only on **new** violations compared to baseline. Existing baseline entries must be reduced over subsequent phases.

Temporary compatibility shims for moved enums/extensions are retained in `app/src/main/java/dev/lanthoor/spendly/utils/LegacyEnumShims.kt` and marked deprecated. They are scheduled for removal in later migration phases.

## CI Integration

- Workflow: `.github/workflows/architecture.yml`
- Required check name: `Architecture Boundaries`

## Ownership Notes

- Boundary rules are introduced in warning-to-enforcement style through baseline control.
- Tightening/removal of baseline entries happens incrementally in later architecture phases.
