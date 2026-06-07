# LexRise v0.5.0

## Highlights

- Adds a fifth review mode: cloze practice from example sentences.
- Improves review-mode switching with a horizontally scrollable mode bar for small screens.
- Opens root/sibling word chips in a bottom-sheet word preview with pronunciation, morpheme split, translation, definition, example, and learning phase.
- Handles missing quiz/cloze material with explicit skip states instead of leaving the UI stuck in a loading message.
- Hardens built-in and user word-book imports with a repository-level mutex and batch progress initialization.

## Verification

- `./gradlew.bat :app:testDebugUnitTest`
- `./gradlew.bat :app:assembleDebug`

## Notes

- No Room schema change in v0.5.0.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
