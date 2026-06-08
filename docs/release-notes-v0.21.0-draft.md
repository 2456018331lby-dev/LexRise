# LexRise v0.21.0

## Highlights

- Adds a Learn tab batch strategy card before individual new-word cards.
- Classifies the current new-word batch as root-focused, word-form-focused, context-focused, mixed, or empty.
- Shows batch metrics, an intensity bar, action label, and focus terms to inspect first.
- Keeps existing new-word ordering and flip-card scoring unchanged; this is explanatory UI derived from the current batch.
- Keeps Room schema unchanged.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 87 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.22 MB.

## Notes

- No Room schema change in v0.21.0.
- `buildWordBatchBrief` is a JVM-testable pure function used by the Learn UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
