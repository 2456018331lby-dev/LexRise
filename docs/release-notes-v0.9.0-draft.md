# LexRise v0.9.0

## Highlights

- Makes the Dashboard focus card actionable with a full-width call-to-action button.
- Routes each focus state to the matching bottom tab: review debt and momentum to Review, pace/new-word guidance to Learn, and root coverage to Roots.
- Moves focus-action copy into `buildStudyFocusCue` so the recommendation and button label stay covered by unit tests instead of drifting in UI code.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 43 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.89 MB.

## Notes

- No Room schema change in v0.9.0.
- Focus actions only switch local Compose tabs; they do not mutate settings, progress, or review logs.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
