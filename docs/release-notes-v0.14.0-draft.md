# LexRise v0.14.0

## Highlights

- Adds a root-family learning route to the Roots tab.
- Classifies each root family as seed, building, consolidating, or mastered based on touched-word progress.
- Shows the next terms to inspect, touched ratio, remaining count, and stage-specific action copy on each root card.
- Refines `RootGroupCard` into a gradient route card with a stage badge and clearer hierarchy.
- Keeps Room schema unchanged: the route is derived from existing `RootGroup` members and progress state.

## Verification

- `./gradlew.bat :app:lintDebug` passed.
- `./gradlew.bat :app:testDebugUnitTest` passed: 57 tests, 0 failures.
- `./gradlew.bat :app:assembleDebug` passed: debug APK 64.68 MB.

## Notes

- No Room schema change in v0.14.0.
- `buildRootGroupInsight` is a JVM-testable pure function used by the Roots UI.
- No remote push should be performed from this workspace until GitHub auth is configured safely.
