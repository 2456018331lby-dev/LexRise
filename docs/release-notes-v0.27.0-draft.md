# LexRise v0.27.0

## Highlights

- Adds offline mnemonic injection for built-in wordlists through the 11th CSV column, `mnemonic`.
- Reads committed `tools/mnemonics_seed.csv` first, then optional ignored `tools/raw/mnemonics.csv` so future offline batches can override or expand the seed.
- Rebuilds all bundled books with mnemonic support: CET4 94 hints, CET6 89 hints, KY 90 hints.
- Upgrades flip-card and tough-word mnemonic display into a dedicated "巧记线索" card.
- Keeps Room schema, SRS scoring, review logs, and user-edited mnemonics unchanged.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"` passed: 3 tests.
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Android unit tests passed: 113 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.46 MB.

## Notes

- No online API is used by the wordlist builder.
- Existing installed databases are not force-updated with new built-in mnemonics, because the app cannot distinguish an intentionally cleared user mnemonic from a previously blank imported value.
