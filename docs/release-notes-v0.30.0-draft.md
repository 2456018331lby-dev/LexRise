# LexRise v0.30.0

## Highlights

- Adds a Dashboard daily load brief after the top metric row.
- Classifies the day as review debt, tough-word repair, root gap, pace push, balanced, or clear wrap-up.
- Shows total intensity, two key metrics, action label, and four load lanes for review, new words, tough words, and root coverage.
- Keeps Room schema, learning routes, auto-pace settings, SRS scoring, and user content unchanged.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"` passed: 3 tests.
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` passed.
- Android unit tests passed: 129 tests, 0 failures, 0 errors.
- Lint passed with 1 existing `GradleDependency` warning.
- Debug APK built: 64.58 MB.

## Notes

- `buildDailyLoadBrief` is a JVM-testable pure function.
- The UI only reads `LearningSession`, `BookRootSnapshot`, `PaceRecommendation`, and current tough-word count.
- GitHub MCP credentials still need separate repair before MCP-based remote publishing can work.
- GitHub main was refreshed through GitHub API snapshot commit `e3a9ee730f83ec66741e1663c4ee0d776c1371ac`; GitHub MCP still returned `Bad credentials` in this environment.
