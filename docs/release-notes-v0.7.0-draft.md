# LexRise v0.7.0

## Highlights

- Adds an in-session practice dashboard to the Review tab.
- Tracks answered, stable, needs-practice, and stability-rate metrics across flip, choice, cloze, spelling, and dictation modes.
- Keeps Learn and Tough ratings out of the Review session metrics so the numbers stay scoped to deliberate practice.
- Adds a reset action for starting a fresh 10-question or 20-question self-check.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 38 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.52 MB.

## Notes

- No Room schema change in v0.7.0.
- Practice metrics are session-only UI state; durable review history still lives in `review_logs`.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
