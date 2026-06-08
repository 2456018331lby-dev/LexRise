# LexRise v0.25.0

## Highlights

- Adds a root-word guide inside the root/member preview bottom sheet.
- Classifies the selected word as root-trace, word-forms, context, or quick-review guidance.
- Shows action label, intensity bar, two key metrics, and focus-term chips before the existing meanings/example/details.
- Reuses the same bottom sheet from Learn same-root chips and the Roots tab member chips.
- Keeps Room schema, root ordering, learning order, and SRS scoring unchanged.

## Verification

- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Unit tests passed: 108 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.40 MB.

## Notes

- No Room schema change in v0.25.0.
- `buildRootWordGuide` is a JVM-testable pure function used by the root preview UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
