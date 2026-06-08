# LexRise v0.15.0

## Highlights

- Adds an in-session coach to the Review tab practice dashboard.
- Derives warmup, recovery, stabilize, and advance advice from the current practice mode and session stability.
- Helps the learner decide whether to slow down, continue the current set, or raise difficulty into cloze, spelling, or dictation.
- Refreshes the practice stats card with a gradient coach panel, live strategy badge, and two strategy metrics.
- Keeps Room schema unchanged: coach state is derived from existing `PracticeSessionStats` and `PracticeMode`.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 61 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.98 MB.

## Notes

- No Room schema change in v0.15.0.
- `buildPracticeSessionCoach` is a JVM-testable pure function used by the Review UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
