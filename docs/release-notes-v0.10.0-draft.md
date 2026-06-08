# LexRise v0.10.0

## Highlights

- Improves cloze practice by allowing example blanks to match the target word's derivatives.
- Keeps the matched surface form as the correct option, so prompts remain grammatical when an example uses forms such as `clarified`.
- Preserves SRS behavior: answering a derivative-based cloze still reviews the original word entry.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 45 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.90 MB.

## Notes

- No Room schema change in v0.10.0.
- Derivative matching uses the existing `derivatives` field generated from wordlist data.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
