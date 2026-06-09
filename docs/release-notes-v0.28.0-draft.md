# LexRise v0.28.0

## Highlights

- Adds a Learn-tab mnemonic coverage brief after the existing new-word batch strategy.
- Classifies the current new-word batch as ready-to-use mnemonics, partial seed gap, root bridge, quick start, or empty.
- Shows coverage progress, two metrics, action label, and focus-term chips so the v0.27 offline mnemonic seed becomes visible before individual cards.
- Keeps Room schema, new-word order, SRS scoring, and user-edited mnemonics unchanged.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"` passed: 3 tests.
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Android unit tests passed: 118 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.49 MB.

## Notes

- `buildMnemonicBatchBrief` is a JVM-testable pure function.
- The UI only reads `WordEntry.mnemonic`; it does not write missing mnemonics automatically.
- GitHub MCP credentials still need separate repair before MCP-based remote publishing can work.
- GitHub main was refreshed through GitHub API snapshot commit `1d458eca4d27e47876ee978d90d5dc95664ffde2` and wordlist blob-alignment commit `b7ec540c2683631d75501c6fab72c7772b199f8d`; GitHub MCP still returned `Bad credentials` in this environment.
