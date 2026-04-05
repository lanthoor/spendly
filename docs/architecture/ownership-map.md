# Spendly Ownership Map

This document describes package ownership boundaries for contributors.

## Package owners

- `app/*`: app shell and bootstrap owners
- `core/model`: shared domain model owners
- `core/ui`: shared UI system owners
- `core/common`: shared technical utility owners
- `feature/*`: feature owners per feature package

## Feature package map

- `feature/accounts`
- `feature/analytics`
- `feature/budgets`
- `feature/dashboard`
- `feature/datamanagement`
- `feature/expenses`
- `feature/income`
- `feature/recurring`
- `feature/settings`
- `feature/transactions`

## Contribution rules

- Cross-feature usage must go through `feature/<feature>/api` contracts.
- Shared stable models belong in `core/model`.
- Shared reusable UI belongs in `core/ui`.
- Do not add new business ownership types under `utils`.
- Keep architecture-impacting changes validated by `checkArchitectureBoundaries`.
