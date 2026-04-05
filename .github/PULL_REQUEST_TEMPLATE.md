## Summary

- Describe the core change in 1-3 bullets.

## Validation

- [ ] `./gradlew :app:compileDebugKotlin`
- [ ] `./gradlew test`
- [ ] `./gradlew lint`
- [ ] `./gradlew checkArchitectureBoundaries`
- [ ] `./gradlew connectedAndroidTest`

## Architecture Checklist

- [ ] Boundary impact reviewed (`domain -> ui`, cross-feature internal imports)
- [ ] Ownership impact reviewed (`core/*`, `feature/*`, `utils`)
- [ ] Test impact reviewed (unit/instrumented updates and coverage)
- [ ] For migration phases: PR scope is limited to a single phase
