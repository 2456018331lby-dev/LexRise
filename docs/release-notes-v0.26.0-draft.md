# LexRise v0.26.0

## Highlights

- Adds a cloze context guide between the cloze prompt and answer options.
- Classifies each cloze question as root-trace, word-form, meaning, or quick-scan guidance.
- Shows action label, confidence bar, two key metrics, and focus-term chips without revealing the correct answer.
- Keeps cloze question construction, option ordering, Room schema, and SRS scoring unchanged.

## Verification

- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Unit tests passed: 112 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.43 MB.

## Notes

- No Room schema change in v0.26.0.
- `buildClozeContextGuide` is a JVM-testable pure function used by the cloze UI.
- GitHub public repository is initialized at `https://github.com/2456018331lby-dev/LexRise`; this workspace may need GitHub API sync if direct `git push` still fails.
