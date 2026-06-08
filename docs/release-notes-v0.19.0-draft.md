# LexRise v0.19.0

## Highlights

- Adds a Tough tab brief that summarizes the current tough-word pool before individual cards.
- Classifies the pool by dominant prescription type: rebuild, root trace, context, or stabilize.
- Shows total tough words, high-risk words, peak AGAIN count, action label, and intensity bar.
- Keeps the existing tough-word ordering and quick GOOD/AGAIN actions unchanged.
- Keeps Room schema unchanged: the brief is derived from existing tough words and prescriptions.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 77 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.14 MB.

## Notes

- No Room schema change in v0.19.0.
- `buildToughWordsBrief` is a JVM-testable pure function used by the Tough UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
