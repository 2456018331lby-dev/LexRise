# LexRise v0.11.0

## Highlights

- Improves cloze practice candidate selection when an example contains multiple possible blanks.
- Prefers the lemma when both the target word and a derivative appear in the same example, keeping the question focused on the current word entry.
- When the lemma is absent, uses the earliest matching derivative in the sentence instead of relying on variant list order or word length.
- Updates the cloze empty-state copy to reflect that both lemma and derivative forms are checked.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 47 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.90 MB.

## Notes

- No Room schema change in v0.11.0.
- SRS behavior is unchanged: answering any cloze still reviews the original word entry.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
