# LexRise v0.8.0

## Highlights

- Adds a Dashboard "today's focus" card that turns raw progress data into a concrete next-step recommendation.
- Chooses between review debt, exam pacing, root coverage, new-word progress, and momentum states using a tested pure function.
- Improves the Dashboard visual hierarchy with a gradient focus card, strategy icon, progress bar, and two compact metric pills.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 42 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.89 MB.

## Notes

- No Room schema change in v0.8.0.
- The focus card is derived from existing session, root coverage, and pace data; it does not write back the user's manual daily target.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
