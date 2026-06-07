# LexRise v0.12.0

## Highlights

- Expands vocabulary search to match derivative forms stored in the existing wordlist data.
- Lets searches such as `clarified` return the base entry `clarify`.
- Extends fuzzy fallback across lemma and derivative forms, so a small typo such as `clarifed` can still recover `clarify`.
- Updates vocabulary search copy to explain lemma, derivative, translation, and definition search paths.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 50 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 63.90 MB.

## Notes

- No Room schema change in v0.12.0.
- Search uses the existing `derivatives` column generated from wordlist data.
- No remote push was performed from this workspace. Follow `docs/github-release-playbook.md` after GitHub auth is configured safely.
