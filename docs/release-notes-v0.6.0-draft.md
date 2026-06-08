# LexRise v0.6.0

## Highlights

- Adds typo-tolerant vocabulary search for English terms.
- Keeps direct term/translation/definition matches first, then fills remaining results with term-similarity candidates.
- Improves the Vocabulary tab with search-status guidance, horizontally scrollable phase filters, and root/part-of-speech/tag chips in each result row.

## Verification

- `./gradlew.bat :app:lintDebug`
- `./gradlew.bat :app:testDebugUnitTest`
- `./gradlew.bat :app:assembleDebug`

## Notes

- No Room schema change in v0.6.0.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
