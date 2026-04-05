# Spendly Architecture Migration Plan

**Status:** Proposed  
**Scope:** Repository/package architecture restructuring  
**Primary Goal:** Move from a mixed single-module structure to a clear, enforceable, feature-oriented architecture with explicit ownership and boundaries.

## Migration Progress Summary

Use this as the quick "how far are we" dashboard. Update `% Complete` weekly (or per PR merge), then update `Weighted Progress` and the `Overall Progress` row.

| Phase | Name | Weight (%) | % Complete | Weighted Progress | Status | Current Focus / Blocker |
| --- | --- | --- | --- | --- | --- | --- |
| 0 | Baseline and Guardrails | 10 | 0% | 0.0% | Not Started | - |
| 1 | Decompose `utils` Ownership | 15 | 0% | 0.0% | Not Started | - |
| 2 | Shared Contracts Decoupling | 15 | 0% | 0.0% | Not Started | - |
| 3 | File Decomposition | 20 | 0% | 0.0% | Not Started | - |
| 4 | Use Case Introduction | 15 | 0% | 0.0% | Not Started | - |
| 5 | Package-First Isolation | 10 | 0% | 0.0% | Not Started | - |
| 6 | Optional Modularization | 10 | 0% | 0.0% | Not Started | Optional / Deferred by default |
| 7 | Hardening and Cleanup | 5 | 0% | 0.0% | Not Started | - |
| **Overall Progress** | **Whole Migration** | **100** | **0%** | **0.0%** | **Not Started** | **No active phase yet** |

### How to calculate progress

- **Per-phase `% Complete`:** `completed checklist items / total checklist items` for that phase.
- **Weighted Progress (per row):** `Weight * % Complete`.
  - Example: Weight `20`, `% Complete` `40%` -> Weighted Progress `8.0%`.
- **Overall Progress:** sum of all `Weighted Progress` values.

### Suggested status values

- `Not Started`
- `In Progress`
- `Blocked`
- `In Review`
- `Completed`

### Update cadence

- Update table after each migration PR merge.
- Reconcile `% Complete` against each phase's Definition of Done checklist.
- Keep `Current Focus / Blocker` short and specific (single sentence).

### Phase-to-PR tracking requirement

- Track one primary PR link per phase in this document (add under each phase section as `Evidence: PR #...`).
- Do not update a phase to `Completed` in the progress table until that phase PR is merged.

---

## 1) Executive Summary

Spendly has grown significantly and currently shows architecture drift: heavy use of broad shared packages (`utils`), cross-feature imports in UI, and oversized files that mix responsibilities. This plan introduces a staged migration to a sane target architecture while preserving release velocity and minimizing risk.

This is intentionally **incremental**:

1. Set architecture guardrails first.
2. Normalize package ownership and shared contracts.
3. Split oversized files and move dense business logic into use cases.
4. Optionally modularize Gradle projects once package boundaries are stable.

The migration is complete when boundaries are enforceable in CI and new features can be developed with minimal cross-feature touchpoints.

---

## 2) Why This Migration Is Needed

### Observed shortcomings

- `utils` contains unrelated concerns (business enums, display helpers, parsing helpers, Android-coupled types), making ownership ambiguous.
- Domain contracts currently rely on types located under non-domain packages (high coupling risk).
- Multiple UI features import each other directly, creating hidden dependencies and slowing refactoring.
- Several files are very large and combine orchestration, transformation, formatting, and business rules.
- Package naming indicates layers, but boundaries are not technically enforced.

### Impact if unchanged

- Higher bug risk when changing one feature.
- Harder onboarding for contributors.
- Increasing PR size and merge conflicts.
- Slower long-term iteration and architecture entropy.

---

## 3) Target Architecture (Desired State)

Two valid end states are supported by this plan:

- **A. Package-first (single module):** fastest stabilization, lower migration risk.
- **B. Package-first + multi-module:** stronger isolation, better incremental builds.

The recommended path is **A first, then B**.

### 3.1 Package target (single-module transitional target)

Within `:app`, converge to:

```text
dev.lanthoor.spendly
  app/                    # app bootstrap, activity, navigation wiring
  core/
    common/               # pure utils, date/time helpers, constants
    model/                # shared stable value types
    ui/                   # shared reusable composables/theme extensions
  feature/
    accounts/
    expenses/
    income/
    budgets/
    dashboard/
    analytics/
    transactions/
    recurring/
    settings/
    datamanagement/
```

### 3.2 Module target (final target)

```text
:app
:core:common
:core:model
:core:ui
:core:database
:core:datastore
:core:testing
:feature:accounts
:feature:expenses
:feature:income
:feature:budgets
:feature:dashboard
:feature:analytics
:feature:transactions
:feature:recurring
:feature:settings
:feature:datamanagement
```

### 3.3 Boundary rules (must hold in target)

- `domain` (or domain-equivalent packages) must not import `ui` or Android resources.
- `feature:X` must not import `feature:Y` internals directly.
- Shared models live in explicit shared locations (`core/model` or feature API contracts).
- `utils` may only contain narrow technical helpers; no business enums or feature state.
- Large business logic belongs in use cases/interactors, not UI screens.

---

## 4) Non-Goals

- No behavior changes to product features during pure structure moves.
- No forced rewrite of all screens to a new pattern in one pass.
- No broad renaming churn without ownership benefit.
- No immediate full modularization if package-first clean-up has not stabilized.

---

## 5) Migration Principles

1. **Behavior preservation first:** each architecture PR should keep runtime behavior unchanged unless explicitly scoped.
2. **Small PRs, clear ownership:** one concern per PR.
3. **Introduce guardrails early:** stop new drift while migration proceeds.
4. **Prefer extraction over rewrite:** move code with adapters first, then simplify.
5. **Measure progress:** use explicit architecture metrics in CI.

### Mandatory Branch and PR Model

Every phase must be implemented on a separate branch and merged through a separate PR.

- Branch naming convention: `arch/phase-<n>-<short-topic>`
  - Example: `arch/phase-2-shared-contracts`
- PR naming convention: Conventional Commit format.
  - Preferred for migration phases: `chore(architecture): phase <n> <scope>`
- One phase per PR:
  - Do not combine work from different phases in the same PR.
  - If needed, use stacked PRs under the same phase branch lineage, then merge one phase PR to `main`.
- Merge order is sequential (`Phase 0` -> `Phase 7`) unless an explicit exception is documented in the PR body.
- A phase is considered complete only after its phase-specific PR is merged.

---

## 6) Detailed Phase Plan

## Phase 0 - Baseline and Guardrails

**Duration:** 1-2 days  
**Risk:** Low  
**Purpose:** Freeze behavior and prevent new architecture regressions.

### Tasks

1. Capture baseline build health:
   - `./gradlew test`
   - `./gradlew lint`
2. Create architecture decision doc: `docs/architecture/package-boundaries.md`.
3. Define import and dependency rules (Detekt/custom lint/arch test):
   - Disallow `domain -> ui` imports.
   - Disallow `feature A -> feature B internal` imports.
   - Disallow new domain/business types under `utils`.
4. Add CI checks for boundary violations.

### Deliverables

- ADR + boundary rule file committed.
- CI job failing on rule violations.

### Exit Criteria

- Tests/lint pass on baseline.
- Architecture checks run on every PR.

### Definition of Done Checklist

- [ ] A dedicated Phase 0 branch is created from latest `main` using `arch/phase-0-...`.
- [ ] A dedicated Phase 0 PR is opened with only Phase 0 scope.
- [ ] Baseline execution logs for `./gradlew test` and `./gradlew lint` are captured and attached to the phase PR.
- [ ] `docs/architecture/package-boundaries.md` exists and documents allowed/forbidden dependencies with at least one example per rule.
- [ ] Boundary checks are wired into CI and visible as a distinct required check.
- [ ] At least one intentionally violating sample import is verified to fail the boundary check (locally or in CI dry run), then reverted.
- [ ] PR template includes architecture checklist items (boundary impact, ownership impact, test impact).
- [ ] No app behavior changes are introduced in this phase (verified by smoke test of core flows).
- [ ] Team agreement recorded (comment or approval) on the initial rule set before Phase 1 starts.
- [ ] Phase 0 PR is merged before Phase 1 PR is opened.

---

## Phase 1 - Decompose `utils` and Establish Explicit Ownership

**Duration:** 3-5 days  
**Risk:** Medium  
**Purpose:** Remove major ambiguity source and clarify model ownership.

### Tasks

1. Split `utils/Enums.kt` into concern-specific files:
   - Preference-related types.
   - Financial/time-period value types.
   - UI display extension helpers.
2. Move domain-safe value types to `core/model`.
3. Move display-oriented extensions to `core/ui` or feature-specific UI mappers.
4. Remove Android resource references from domain-safe types (if present).
5. Add temporary compatibility wrappers/typealiases (short-lived).
6. Migrate call sites and remove wrappers once usages are zero.

### Deliverables

- `utils/Enums.kt` deleted or reduced to truly generic helpers.
- New explicit packages for model vs UI extensions.

### Exit Criteria

- Domain packages no longer import ambiguous `utils` types for core contracts.
- No business enum additions occur under `utils`.

### Definition of Done Checklist

- [ ] A dedicated Phase 1 branch is created from latest `main` using `arch/phase-1-...`.
- [ ] A dedicated Phase 1 PR is opened with only Phase 1 scope.
- [ ] `utils/Enums.kt` is either removed or reduced to strictly generic helpers with no business/domain ownership ambiguity.
- [ ] Preference, financial, and time-period value types are moved to explicit ownership locations (`core/model` or equivalent).
- [ ] UI-only display extensions are moved to `core/ui` or feature UI packages.
- [ ] Domain-safe types no longer reference Android resources (`R.*`) or Android framework APIs.
- [ ] Temporary compatibility shims are explicitly marked deprecated and tracked with removal tasks.
- [ ] All call sites compile without importing removed paths; IDE/code search confirms no lingering old imports.
- [ ] Unit tests covering moved value types/extensions pass without behavior regression.
- [ ] Architecture checks still pass with stricter ownership rules enabled.
- [ ] Phase 1 PR is merged before Phase 2 PR is opened.

---

## Phase 2 - Shared Contracts and Cross-Feature Decoupling

**Duration:** 2-4 days  
**Risk:** Medium  
**Purpose:** Remove direct feature-to-feature internals coupling.

### Tasks

1. Identify shared data/state currently located inside feature packages.
2. Move shared types to one of:
   - `core/model` for broadly shared stable types.
   - `feature:<x>:api` package for feature-owned external contracts.
3. Replace direct imports from `feature Y` internals with shared/API contracts.
4. Add lint rules to block future internal cross-feature imports.

### Deliverables

- Shared contracts extracted.
- Reduced cross-feature imports.

### Exit Criteria

- Feature packages only depend on shared/API packages, not each other’s internals.

### Definition of Done Checklist

- [ ] A dedicated Phase 2 branch is created from latest `main` using `arch/phase-2-...`.
- [ ] A dedicated Phase 2 PR is opened with only Phase 2 scope.
- [ ] Cross-feature shared types are inventory-listed and each has a documented owner (`core/model` or `feature:<x>:api`).
- [ ] Direct imports from `feature:Y` internals to `feature:X` are replaced with shared/API contracts.
- [ ] New `api` packages (if introduced) expose only required public contracts; internal implementation types remain non-exported.
- [ ] Boundary rules include explicit prohibition of feature-internal cross-imports.
- [ ] Updated imports are validated with full project compile and test run.
- [ ] Existing behavior in affected screens is smoke-tested (navigation, rendering, basic interactions).
- [ ] Dependency graph snapshot is captured to show reduced cross-feature coupling.
- [ ] Phase 2 PR is merged before Phase 3 PR is opened.

---

## Phase 3 - File Decomposition (God-File Reduction)

**Duration:** 1-2 weeks  
**Risk:** Medium  
**Purpose:** Improve cohesion, testability, and maintainability.

### Priority candidates

- Export/import repository implementation.
- Large settings/data-management screens.
- Large dashboard/analytics/transaction ViewModels.
- Large custom chart files where logic and rendering are mixed.

### Tasks

1. Split repositories by concern:
   - serialization/deserialization
   - validation
   - mapping/remapping
   - orchestration
2. Split large screens into section composables + smaller state components.
3. Split ViewModels into:
   - event handlers
   - state reducers/assemblers
   - domain calculators/use cases
4. Introduce mapper packages (`domain/mapper`, `ui/mapper`) as needed.

### Deliverables

- Reduced file size and complexity across top offenders.

### Exit Criteria

- Most files below ~300 lines unless justified.
- ViewModels are orchestration-heavy, not business-logic-heavy.

### Definition of Done Checklist

- [ ] A dedicated Phase 3 branch is created from latest `main` using `arch/phase-3-...`.
- [ ] A dedicated Phase 3 PR is opened with only Phase 3 scope.
- [ ] Top offender files selected for this phase are listed with before/after line counts.
- [ ] Each split file has a clear single responsibility (orchestrator, mapper, validator, serializer, section UI, etc.).
- [ ] Repository decomposition preserves transactional/atomic behavior where required.
- [ ] Large screens are decomposed into section composables with unchanged UX behavior.
- [ ] ViewModels no longer contain formatting-heavy or calculation-heavy blocks that belong in dedicated collaborators.
- [ ] New collaborators have unit tests (or existing tests are updated) to preserve logic parity.
- [ ] Manual regression smoke test passes for all touched feature flows.
- [ ] No new file exceeds agreed size threshold unless rationale is documented in code review.
- [ ] Phase 3 PR is merged before Phase 4 PR is opened.

---

## Phase 4 - Introduce Use Cases for Dense Business Logic

**Duration:** 4-7 days  
**Risk:** Medium  
**Purpose:** Make business rules testable and reusable.

### Tasks

1. Introduce use cases for heavy calculations:
   - dashboard summary computation
   - analytics period transformation
   - budget progress and threshold checks
2. Move pure logic from ViewModels into use cases.
3. Unit test use cases directly.
4. Keep ViewModels focused on flow collection and event dispatch.

### Deliverables

- Use case classes with tests.

### Exit Criteria

- Business logic coverage primarily in use case tests.
- ViewModels significantly simplified.

### Definition of Done Checklist

- [ ] A dedicated Phase 4 branch is created from latest `main` using `arch/phase-4-...`.
- [ ] A dedicated Phase 4 PR is opened with only Phase 4 scope.
- [ ] Targeted business logic paths are extracted into use case classes with explicit input/output contracts.
- [ ] ViewModels delegate calculations/transforms to use cases instead of embedding complex logic.
- [ ] Use cases are platform-agnostic where possible (no Android UI dependencies).
- [ ] Unit tests exist for each new use case, including edge cases and error paths.
- [ ] Existing ViewModel tests are updated to validate orchestration and state transitions only.
- [ ] Coverage trend shows increased logic coverage in use-case tests relative to ViewModel tests.
- [ ] Performance remains acceptable (no obvious regressions from added abstraction layers).
- [ ] Architecture checks confirm no invalid dependency direction introduced by use case extraction.
- [ ] Phase 4 PR is merged before Phase 5 PR is opened.

---

## Phase 5 - Package-First Feature Isolation (Still Single Module)

**Duration:** 4-6 days  
**Risk:** Medium  
**Purpose:** Achieve target package shape before Gradle module extraction.

### Tasks

1. Move code into final package tree (`app/core/feature`).
2. Update imports in small batches by feature.
3. Keep temporary forwarding shims where necessary (short lifespan).
4. Validate each move with tests/lint.

### Deliverables

- Final package tree in place under `:app`.

### Exit Criteria

- Architecture layout readable and consistent across features.

### Definition of Done Checklist

- [ ] A dedicated Phase 5 branch is created from latest `main` using `arch/phase-5-...`.
- [ ] A dedicated Phase 5 PR is opened with only Phase 5 scope.
- [ ] Package tree under `:app` matches documented `app/core/feature` target structure.
- [ ] Moves are completed in small batches and each batch has green build/test/lint.
- [ ] Temporary forwarding shims are minimized and each has a dated removal note.
- [ ] Feature code is colocated (UI/domain/data for each feature) according to agreed structure.
- [ ] Shared code is only in `core/*` (or designated shared package), not hidden inside feature internals.
- [ ] Import statements and package declarations are normalized and consistent.
- [ ] Developer docs are updated with the new package map and contribution guidance.
- [ ] Team can implement a small sample change in one feature without touching unrelated feature packages.
- [ ] Phase 5 PR is merged before Phase 6 PR is opened.

---

## Phase 6 - Optional Gradle Modularization

**Duration:** 1-2 weeks  
**Risk:** Medium-High  
**Purpose:** Enforce stronger boundaries and improve build performance.

### Tasks

1. Create core modules first: `:core:model`, `:core:common`, `:core:ui`.
2. Migrate one feature module at a time.
3. Move persistence infra to `:core:database` and prefs to `:core:datastore`.
4. Update Hilt bindings and exposed APIs.
5. Prevent cycles with dependency constraints.

### Deliverables

- Core + feature module graph with no cycles.

### Exit Criteria

- Full build and tests pass with module graph checks.

### Definition of Done Checklist

- [ ] A dedicated Phase 6 branch is created from latest `main` using `arch/phase-6-...`.
- [ ] A dedicated Phase 6 PR is opened with only Phase 6 scope.
- [ ] Core modules (`:core:model`, `:core:common`, `:core:ui`) are created and integrated first.
- [ ] Feature modules are migrated one at a time with no cyclic dependencies.
- [ ] `settings.gradle.kts` and Gradle dependency graph reflect intended module boundaries.
- [ ] DI/Hilt bindings compile and runtime injection works across module boundaries.
- [ ] Module API surfaces are intentionally scoped (`api` vs `implementation`) to avoid leakage.
- [ ] Build time baseline is compared before/after modularization and documented.
- [ ] Full test suite and lint checks pass in modularized state.
- [ ] Release/debug app startup and critical flows work after modularization.
- [ ] Phase 6 PR is merged before Phase 7 PR is opened.

---

## Phase 7 - Hardening and Cleanup

**Duration:** 2-3 days  
**Risk:** Low  
**Purpose:** Remove migration artifacts and lock in architecture quality.

### Tasks

1. Remove temporary adapters/typealiases.
2. Tighten lint/Detekt thresholds.
3. Add architecture checks to PR template and CI required checks.
4. Publish updated contributor guide.

### Deliverables

- Cleaned codebase with enforced boundaries and updated docs.

### Exit Criteria

- No migration shims remain.
- Architecture checks are mandatory and green.

### Definition of Done Checklist

- [ ] A dedicated Phase 7 branch is created from latest `main` using `arch/phase-7-...`.
- [ ] A dedicated Phase 7 PR is opened with only Phase 7 scope.
- [ ] All temporary typealiases/adapters/forwarders introduced during migration are removed.
- [ ] Lint/Detekt thresholds are set to enforce target architecture rules strictly.
- [ ] CI marks architecture checks as required for merge (non-optional).
- [ ] Contributor documentation and PR templates fully reflect final architecture conventions.
- [ ] Ownership map is finalized and accessible to contributors.
- [ ] Final dependency graph report is generated and attached for traceability.
- [ ] Final migration retrospective captures lessons learned and follow-up debt (if any).
- [ ] A validation release candidate build is produced successfully from the migrated structure.
- [ ] Phase 7 PR is merged and linked from this document as final migration evidence.

---

## 7) Concrete Work Breakdown Structure (WBS)

Each item is independently shippable and should map to 1-3 PRs.

1. **Architecture Governance**
   - ADR creation
   - boundary check integration
   - PR checklist update
2. **Model Ownership Normalization**
   - enum/value type extraction
   - UI helper extraction
3. **Feature Contract Stabilization**
   - shared type extraction
   - cross-feature import replacement
4. **Complexity Reduction**
   - repository split
   - screen decomposition
   - ViewModel simplification
5. **Use Case Layering**
   - use case creation
   - ViewModel integration
   - tests
6. **Package/Module Restructure**
   - package moves
   - module extraction (optional)
7. **Hardening**
   - delete adapters
   - strict CI enforcement

---

## 8) Suggested PR Sequence (Strict Order)

1. ADR + CI boundary check scaffolding (no behavior changes).
2. Extract enums/value objects from `utils`.
3. Move UI display extensions out of domain-ish areas.
4. Extract shared cross-feature contracts.
5. Refactor export/import into collaborators.
6. Split dashboard/analytics heavy ViewModels and add use cases.
7. Decompose settings and data management screens.
8. Normalize package tree under `app/core/feature`.
9. (Optional) Create core modules.
10. (Optional) Move features into modules one by one.
11. Remove shims and tighten checks.

---

## 9) Testing and Verification Matrix

Run at minimum after every phase:

- `./gradlew test`
- `./gradlew lint`
- Architecture checks (Detekt/lint custom rules)

For high-risk phases (3, 4, 6), also run:

- Focused screen regression tests (instrumented where available)
- Manual smoke test flows:
  - Add/edit/delete expense
  - Add/edit income
  - Dashboard summary updates
  - Analytics period changes
  - Budget notifications setup
  - Export/import happy path

---

## 10) Acceptance Metrics (Definition of Done)

Migration is considered complete when all are true:

1. **Boundary health**
   - 0 `domain -> ui` imports.
   - 0 direct cross-feature internal imports.
2. **Ownership clarity**
   - Business value types are in explicit model packages/modules.
   - `utils` does not contain domain policy or feature state models.
3. **Complexity reduction**
   - Majority of top offenders reduced to manageable size.
   - ViewModels mostly orchestrate, use cases contain calculations.
4. **Process enforcement**
   - CI fails on architecture violations.
   - Contributor docs reflect new structure.

Recommended quantitative targets:

- Reduce cross-feature UI imports by at least 80%.
- Reduce count of files > 400 lines by at least 60%.
- Keep new files under ~250-300 lines unless justified.

---

## 11) Risks and Mitigations

### Risk: Regressions during mass moves
- **Mitigation:** smaller PRs, frequent test runs, behavior-preserving changes first.

### Risk: Merge conflicts on high-churn files
- **Mitigation:** prioritize high-churn files early; keep branches short-lived.

### Risk: DI breakages during modularization
- **Mitigation:** complete package-first cleanup before module splits; migrate one module at a time.

### Risk: Team inconsistency with new boundaries
- **Mitigation:** codify rules in CI and PR templates; publish ownership map.

---

## 12) Ownership and Governance

Define and maintain explicit owners:

- `app/*`: app shell owners
- `core/model`: domain model owners
- `core/ui`: design system/shared UI owners
- `feature/*`: feature owners

Required process updates:

- Add architecture review checklist to PR template.
- Require at least one owner review for boundary-touching PRs.
- Require architecture check CI status before merge.

---

## 13) Implementation Notes for This Repository

Given current repository characteristics:

- Start with package-first migration in `:app` to reduce risk.
- Keep release cadence by limiting migration PR size.
- Avoid combining feature changes and migration changes in the same PR.
- Keep existing user-facing behavior unchanged until architecture baseline is stable.

---

## 14) Immediate Next Actions (Week 1)

1. Create `docs/architecture/package-boundaries.md` and define allowed dependencies.
2. Add architecture checks to CI (warning mode first, then fail mode).
3. Split and migrate `utils` value types into explicit packages.
4. Extract first set of shared cross-feature contracts.

---

## 15) Appendix - Example Dependency Policy (Human-readable)

- `app` may depend on `core` and `feature` APIs.
- `feature:X` may depend on `core:*` and its own internal packages.
- `feature:X` may depend on `feature:Y` **only through public API package/module**, not internals.
- `core:model` must remain platform-agnostic as much as possible.
- `core:ui` may depend on `core:model`, not vice versa.
- `core:database` and `core:datastore` should not depend on feature modules.

---

## 16) Change Management Notes

- This plan should be reviewed after each completed phase.
- If timelines slip, keep phase order unchanged; only reduce scope per phase.
- Architecture debt discovered mid-migration should be added as explicit backlog items, not hidden in unrelated PRs.

---

## 17) Final Outcome Statement

At completion, Spendly will have:

- predictable package/module ownership,
- enforced architectural boundaries,
- reduced cross-feature coupling,
- smaller and more maintainable source files,
- and a structure that supports faster, safer feature development.
