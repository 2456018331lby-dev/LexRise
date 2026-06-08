# LexRise v0.18.0

## Highlights

- Adds a front-side memory anchor to new-word flip cards.
- Classifies each word as root-family, word-form, context, or solo-anchor based on root references, derivatives, examples, and phase.
- Renders the anchor as a compact gradient panel with a badge, strategy copy, two metrics, and optional clue chips.
- Keeps user mnemonics untouched: the anchor is derived guidance, not generated or persisted mnemonic text.
- Keeps Room schema unchanged and does not alter SRS scoring.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 73 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.11 MB.

## Notes

- No Room schema change in v0.18.0.
- `buildWordMemoryAnchor` is a JVM-testable pure function used by the Learn card UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
