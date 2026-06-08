# LexRise v0.23.0

## Highlights

- Adds a Dashboard seven-day rhythm brief before the 30-day review heatmap.
- Classifies the recent rhythm as quiet, recovery, balanced, steady, or surge.
- Shows a seven-day mini rhythm chart, momentum bar, action label, and two key metrics.
- Uses today's overview to keep the current-day bar accurate even when the persisted heatmap has not refreshed yet.
- Keeps review logs, daily route behavior, SRS scoring, and Room schema unchanged.

## Verification

- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Unit tests passed: 99 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.31 MB.

## Notes

- No Room schema change in v0.23.0.
- `buildStudyRhythmBrief` is a JVM-testable pure function used by the Dashboard UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
