# LexRise v0.32.0

## Highlights

- Vocabulary search now shows an "空结果救援" card when a query has no results.
- The rescue route explains whether to remove phase filters, expand a short English query, return a derivative to base form, shorten to a stem, fix one spelling point, or switch Chinese meaning wording.
- The card appears below the existing search insight card and does not mutate the query, phase filter, search order, or user settings.

## Engineering Notes

- Added `VocabularySearchRescuePlan` and `VocabularySearchRescueStep`.
- Added pure function `buildVocabularySearchRescuePlan(query, resultCount, phaseFilter)`.
- Added `VocabularySearchRescueCard` to the Vocab tab using the existing gradient card language.
- Bumped app metadata to `versionCode=32` / `versionName="0.32.0"`.
- Android unit tests increase from 135 to 141.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"`
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`

## Compatibility

- No Room schema change.
- No dependency change.
- No bundled wordlist regeneration.
