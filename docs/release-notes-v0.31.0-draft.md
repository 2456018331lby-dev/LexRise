# LexRise v0.31.0

## Highlights

- Review Tab adds a "本轮收口建议" card between the queue brief and the practice stats card.
- The card uses current session stats, practice mode, and remaining due count to decide whether to start, continue, repair mistakes, level up, wrap up, or close the round.
- The logic is derived by `buildReviewExitBrief`, stays pure/JVM-testable, and does not change SRS scoring, review order, Room schema, or user settings.

## Engineering Notes

- Added `ReviewExitBrief` / `ReviewExitBriefKind`.
- Added `ReviewExitBriefCard` with the existing Review visual language: gradient surface, action pill, progress bar, and two metrics.
- Bumped app metadata to `versionCode=31` / `versionName="0.31.0"`.
- Android unit tests increase from 129 to 135.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"`
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`

## Compatibility

- No Room schema change.
- No dependency change.
- No bundled wordlist regeneration.
