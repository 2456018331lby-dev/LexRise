# LexRise v0.22.0

## Highlights

- Adds a Review tab queue brief before the in-session stats card.
- Classifies due review words as empty, warmup, root-trace, context, active-recall, or mixed.
- Shows queue metrics, an intensity bar, action label, and focus terms to inspect first.
- Prioritizes root clusters first, then spelling/dictation active recall before context hints, so the card follows the selected practice mode.
- Keeps existing review order, practice mode selection, SRS scoring, and Room schema unchanged.

## Verification

- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Unit tests passed: 94 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.26 MB.

## Notes

- No Room schema change in v0.22.0.
- `buildReviewQueueBrief` is a JVM-testable pure function used by the Review UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
