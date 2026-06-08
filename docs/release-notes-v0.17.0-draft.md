# LexRise v0.17.0

## Highlights

- Adds a Dashboard daily study route that turns review debt, tough words, root coverage, and new-word pacing into 1-3 actionable steps.
- Adds route targets for Review, Tough, Roots, and Learn so each step can jump directly to the right tab.
- Renders the route as a horizontal training rail with task copy, a key metric, intensity bar, and action button.
- Keeps Room schema unchanged: route state is derived from existing session, root snapshot, pace, and tough-word count.
- Keeps the existing study focus card and all SRS scoring paths unchanged.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 69 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.06 MB.

## Notes

- No Room schema change in v0.17.0.
- `buildDailyStudyRoute` is a JVM-testable pure function used by the Dashboard UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
