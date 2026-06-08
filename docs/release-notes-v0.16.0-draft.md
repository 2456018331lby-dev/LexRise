# LexRise v0.16.0

## Highlights

- Adds a tough-word prescription panel to the Tough tab.
- Classifies each mistake as rebuild, root trace, context repair, or stabilize based on `againCount`, lapses, root clues, examples, and current phase.
- Turns tough-word cards into gradient prescription cards with a badge, action label, intensity bar, and two key metrics.
- Keeps the existing tough-word ordering and SRS scoring path unchanged.
- Keeps Room schema unchanged: prescription state is derived from existing `ToughWord` and `WordEntry` data.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 65 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.01 MB.

## Notes

- No Room schema change in v0.16.0.
- `buildToughWordPrescription` is a JVM-testable pure function used by the Tough UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
