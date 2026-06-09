# LexRise v0.33.0

## Highlights

- Vocabulary search now shows a "结果分诊" card when a query has usable results.
- The card tells learners whether to inspect the active phase filter, follow a root lane, fold derivative or typo hits back to the lemma, sweep direct term hits, confirm meaning matches, or browse mixed results in batches.
- The result page now has two complementary guides: "结果分诊" for successful searches and "空结果救援" for failed searches.

## Implementation Notes

- Added `VocabularyResultTriage`, `VocabularyResultLane`, and `VocabularyResultTriageKind`.
- Added pure function `buildVocabularyResultTriage(query, results, phaseFilter)`.
- Added `VocabularyResultTriageCard` and `VocabularyResultLaneRow` to the Vocab tab using the existing LexRise gradient-card language.
- Bumped app metadata to `versionCode=33` / `versionName="0.33.0"`.
- Android unit tests increase from 141 to 148.

## Verification

- `python -m unittest discover -s tools -p "test_*.py"`
- `./gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`
