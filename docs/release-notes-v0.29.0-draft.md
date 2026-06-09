# LexRise v0.29.0

## Highlights

- Adds a Roots-tab root mnemonic supply brief after the existing root atlas brief.
- Classifies the visible root graph as no mnemonic seed, partial root gaps, usable coverage, dense network, or empty.
- Shows mnemonic word coverage, seeded root coverage, progress, action label, and focus-root chips so offline mnemonic seeds can guide future content completion.
- Keeps Room schema, root ordering, learning progress, SRS scoring, and user-edited mnemonics unchanged.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"` passed: 3 tests.
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Android unit tests passed: 123 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.54 MB.

## Notes

- `buildRootMnemonicBrief` is a JVM-testable pure function.
- The UI only reads `RootGroup.members.mnemonic`; it does not write or auto-generate missing mnemonics.
- GitHub MCP credentials still need separate repair before MCP-based remote publishing can work.
- GitHub main was refreshed through GitHub API snapshot commit `953703dfaab2d4dbdf721cceb68cf7bbbb5e1e35`; GitHub MCP still returned `Bad credentials` in this environment.
