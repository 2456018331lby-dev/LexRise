# LexRise v0.13.0

## Highlights

- Adds a vocabulary-result "word-form radar" that shows which lemma or derivative form matched the current search query.
- Makes derivative searches more explainable: searching `clarified` or typo `clarifed` can return `clarify` and show the matched form on the result card.
- Refines vocabulary result cards with clearer hierarchy and horizontally scrollable chips for small screens.
- Keeps search ranking and Room schema unchanged.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 53 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.92 MB.

## Notes

- No Room schema change in v0.13.0.
- `matchingWordForms` is a JVM-testable pure function used only for UI explanation.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
