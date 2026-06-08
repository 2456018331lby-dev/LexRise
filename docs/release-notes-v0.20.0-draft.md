# LexRise v0.20.0

## Highlights

- Adds a Vocabulary tab search insight card before the result list.
- Classifies the current query as ready, empty result, direct term match, word-form/typo match, root cluster, or meaning match.
- Shows confidence, primary/secondary metrics, an action label, and focus terms/forms to inspect first.
- Keeps existing vocabulary search ordering unchanged; the card is explanatory UI derived from the current query and results.
- Keeps Room schema unchanged.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 82 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.18 MB.

## Notes

- No Room schema change in v0.20.0.
- `buildVocabularySearchInsight` is a JVM-testable pure function used by the Vocabulary UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
