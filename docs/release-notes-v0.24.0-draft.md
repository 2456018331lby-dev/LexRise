# LexRise v0.24.0

## Highlights

- Adds a Roots tab atlas brief before individual root-family cards.
- Classifies the overall root map as empty, seed, expand, consolidate, or mastered.
- Shows root coverage, clustered-word progress, a progress bar, action label, and priority root chips.
- Keeps whole-book coverage based on `BookRootSnapshot` while narrowing priority chips to the currently visible root results.
- Keeps Room schema, root list ordering, learning order, and SRS scoring unchanged.

## Verification

- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Unit tests passed: 104 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.36 MB.

## Notes

- No Room schema change in v0.24.0.
- `buildRootAtlasBrief` is a JVM-testable pure function used by the Roots UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
