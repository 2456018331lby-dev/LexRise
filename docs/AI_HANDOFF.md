# LexRise AI 接手手册

这份文档专门给下一个 AI（或人）接手这个项目时读。
README 面向用户，这份面向开发者/AI——信息密度优先，直接给"当前到哪了、地雷在哪、下一步候选是什么"。

**最近一次更新**：2026-06-09（v0.37 完工后）

---

## 1. 项目一句话描述

`c:/Users/24560/Desktop/study/Englishdemo` 是一个本地离线的 Android 背单词 App（Kotlin + Compose + Material3 dynamicColor + Room v2 + WorkManager），面向 CET4/CET6/考研。当前能跑、167 个 Android 单测 + 3 个 Python 脚本单测全绿，debug APK 可装。

**独立 git 仓库**（v0.3 起）：`Englishdemo/.git/`，分支 `main`，家目录级别仓库被完全隔离。

---

## 2. 当前版本：v0.37（已完成）

**词库规模**（全部来自合法开源源头，首启自动导入）：

| 词书 | 词量 | 来源 |
|------|------|------|
| 四级高频 · 词根序 | 3846 | ECDICT (MIT) tag=cet4 |
| 六级进阶 · 词根序 | 5406 | ECDICT tag=cet6 |
| 考研英语 · 大纲词 | 4801 | ECDICT tag=ky |

**学习核心**：

- 词按 **rootKey 聚簇 + 簇内 frq 升序** 出现。约 21–25% 的词能聚簇；v0.21 起新词页顶部会按当前批次派生“本批新词策略”，先提示词根、词形、语境或混合打法；v0.28 起新词页还会按当前批次派生“巧记覆盖简报”，提示先读现成巧记、边学边补、借词根桥接或速记起步；v0.35 起新增“新词闭环计划”，把批次拆成预读、自测、评分入轨，并按词根、巧记、语境、大批次或小批快进选择闭环打法
- **复习模式（v0.5）**：翻卡 / 选择题 / 完形 / 拼写 / 听写 五种统一走 SRS
  - 翻卡：默认盖住释义，点击才翻；**拆词条和词根意思常驻显示**（v0.4 bug fix，不再要翻卡二次揭示）；v0.18 起正面新增“记忆锚”，自动提示先抓词根、词形、语境或基础释义；v0.25 起点击同根词 chip 打开的底部详情卡会先显示词根详情导读；v0.37 起底部详情卡新增词条 hero 和“复盘计划”三步轨道；v0.27 起内置或用户填写的巧记显示为“巧记线索”卡片
  - 选择题：`buildQuizQuestion` 从同根词优先抽干扰项；答对 → GOOD，答错 → AGAIN
  - 完形：`buildClozeQuestion` 从例句中挖掉目标词或 derivatives（v0.10），v0.11 起候选排序优先原词、再按例句最早出现的派生词；v0.26 起题干和选项之间新增“语境导读”，提示先抓词根、词形、语义或快速扫句；v0.36 起 `buildClozeBlank` 会先拆多句例句并按命中词形、句长、空格位置和标点排序，优先生成短而清晰的完形题干；复用同根优先干扰项；答对 → GOOD，答错 → AGAIN
  - 拼写/听写：Levenshtein 距离 `normalizedEditDistance` → `ratingFromEditDistance` 四档映射（0 → EASY，≤0.15 → GOOD，≤0.35 → HARD，余 → AGAIN）
- **难词专攻 Tab**（v0.4/v0.16/v0.19）：`getToughWords` 聚合 `review_logs` 中 `rating=AGAIN` 计数 top N；v0.16 起每个难词卡派生“重建 / 词根 / 语境 / 巩固”错题处方；v0.19 起顶部汇总“错题战情台”，判断当前错题池主导打法；支持原地"会了/不会"快速评分
- **簇首词**（同根中 frq 最低的那个）顶上『地基词』徽章
- **词根 Tab**：按根聚合整本词书；v0.24 起顶部新增“词根图谱简报”，先判断整本词根覆盖是待生成、起步、扩展、收束还是基本铺开；v0.29 起新增“根族巧记补给”，按根族 mnemonic seed 覆盖提示补地基词、补根族缺口、回看有种子根或抽查强根族；v0.14 起每个根族卡自动生成“起步 / 推进 / 收尾 / 稳固”路线，展示阶段徽章、推荐关注词、触达比例和剩余词数；成员 chip 点击打开底部详情卡（v0.5），不再只播音；v0.25 起底部详情卡新增“根族导读 / 词形导读 / 语境导读 / 速览导读”，展示行动标签、指标、强度条和焦点词 chips；v0.37 起同一底部卡新增“复盘计划”，按词根、词形、巧记、语境或快扫生成三步复盘轨道
- **内置巧记（v0.27）**：三本内置 CSV 已扩展到 11 列 `mnemonic`；构建脚本先读入仓 seed，再读不入仓 `tools/raw/mnemonics.csv` 覆盖。当前 seed 命中 CET4 94 条、CET6 89 条、考研 90 条。App 不调用在线 API，不在启动时自动覆盖用户已经编辑过的 mnemonic
- Dashboard：考试倒计时卡 + 词根覆盖卡 + 30 日热力图 + 连续打卡徽章；v0.30 起在顶部指标后新增“今日负载简报”，把复习债、新词目标、难词压力和词根缺口拆成 4 条负载轨道；v0.17 起新增“今日训练路线”，把复习债、难词、词根覆盖和新词推进排成 1–3 个可点击步骤；v0.23 起在热力图前新增“七日节奏简报”，判断最近一周是安静期、恢复、平衡、稳态还是冲刺
- **今日学习焦点（v0.8/v0.9）**：Dashboard 根据复习债 / 自动配速 / 词根覆盖 / 新词剩余 / 完成态，派生“今天先做什么”的建议卡；v0.9 新增行动按钮，可直达复习 / 新词 / 词根 Tab
- **考试日期 + 自动配速**：按剩余词量 / 剩余天数 + 20% 缓冲算每日目标
- **词汇搜索（v0.6/v0.12/v0.13/v0.20/v0.32/v0.33）**：直接命中不足时按英文词形相似度补充候选；`clarfy` 这类小 typo 也能找回 `clarify`；v0.12 起直接搜索和 fuzzy fallback 都支持 derivatives，`clarified` / `clarifed` 也能找回 `clarify`；v0.13 起结果卡展示“词形雷达”，说明命中的原词/派生词形；v0.20 起搜索框下方新增“检索洞察”，判断当前是原词命中、词形/typo、同根聚簇、释义命中还是无结果；v0.32 起空结果时新增“空结果救援”，按阶段筛选、英文词干、原形、拼写和中文释义给出改查路线；v0.33 起有结果时新增“结果分诊”，按阶段、同根、词形、原词、释义或混合路线提示先怎么看
- **练习模式阶梯 + 复习队列预案 + 本轮收口建议 + 本轮统计 + 本轮教练（v0.34/v0.22/v0.31/v0.7/v0.15）**：Review Tab 在模式条后先显示“练习模式阶梯”，把翻卡、选择、完形、拼写、听写解释成 5 级训练路线，并按本轮表现提示先试当前模式、降速一档、升下一档或收口听写；随后显示“复习队列预案”，按到期词和当前模式提示轻量回温、词根修复、语境回忆、主动提取或混合清债；v0.31 起新增“本轮收口建议”，按本轮完成数、稳答、再练、当前模式和剩余到期词判断先开始、继续、修错、加难、收尾或清空；随后显示完成 / 稳答 / 再练 / 稳定率；v0.15 起根据当前模式和稳定率派生“先校准 / 降速修复 / 稳住节奏 / 提高难度”的下一步策略；五种 Review 练习模式统一计入，Learn/Tough 不混入
- SRS：AGAIN/HARD/GOOD/EASY 四档

**工程加固（v0.5 新）**：

- 内置词书导入和用户词书合并统一走 `importMutex`，防止首启/重复点击导入时插入重复词书
- 词条进度初始化从逐条 `insert` 改为 `insertAll`
- `app/build.gradle.kts` 已同步 `versionCode=37` / `versionName="0.37.0"`

**已做的测试 / 构建验证**：

- `./gradlew.bat :app:lintDebug` — **通过**（1 个既有 `GradleDependency` 版本提示）
- `python -m unittest discover -s tools -p "test_*.py"` — **3 个 test 全绿**
- `./gradlew.bat :app:testDebugUnitTest` — **167 个 test 全绿**
- `./gradlew.bat :app:assembleDebug` — 成功，debug APK 64.87 MB

---

## 3. 代码索引（读它在哪）

### 3.1 数据层 `app/src/main/java/com/study/englishdemo/data/`

| 文件 | 职责 |
|------|------|
| `Models.kt` | Room 实体 + 领域模型。v0.37 增：`RootWordPracticePlan`、`RootWordPracticePlanKind`；v0.35 增：`LearningLoopBrief`、`LearningLoopBriefKind`、`LearningLoopStep`；v0.34 增：`PracticeModeBrief`、`PracticeModeBriefKind`、`PracticeModeLadderStep`；v0.33 增：`VocabularyResultTriage`、`VocabularyResultLane`、`VocabularyResultTriageKind`；v0.32 增：`VocabularySearchRescuePlan`、`VocabularySearchRescueStep`；v0.31 增：`ReviewExitBrief`、`ReviewExitBriefKind`；v0.30 增：`DailyLoadBrief`、`DailyLoadBriefKind`、`DailyLoadLane`；v0.29 增：`RootMnemonicBrief`、`RootMnemonicBriefKind`；v0.28 增：`MnemonicBatchBrief`、`MnemonicBatchBriefKind`；v0.26 增：`ClozeContextGuide`、`ClozeContextGuideKind`；v0.25 增：`RootWordGuide`、`RootWordGuideKind`；v0.24 增：`RootAtlasBrief`、`RootAtlasBriefKind`；v0.23 增：`StudyRhythmBrief`、`StudyRhythmBriefKind`；v0.22 增：`ReviewQueueBrief`、`ReviewQueueBriefKind`；v0.21 增：`WordBatchBrief`、`WordBatchBriefKind`；v0.20 增：`VocabularySearchInsight`、`VocabularySearchInsightKind`；v0.19 增：`ToughWordsBrief`；v0.18 增：`WordMemoryAnchor`、`WordMemoryAnchorKind`；v0.17 增：`DailyStudyRoute`、`DailyStudyRouteStep`、`DailyStudyRouteTarget`；v0.16 增：`ToughWordPrescription`、`ToughWordPrescriptionKind`；v0.15 增：`PracticeSessionCoach`、`PracticeSessionCoachKind`；v0.14 增：`RootGroupStage`、`RootGroupInsight`；v0.10 增：`ClozeBlank`；v0.9 给 `StudyFocusCue` 增 `actionLabel`；v0.8 增：`StudyFocusKind`、`StudyFocusCue`；v0.7 增：`PracticeSessionStats`；v0.5 增：`PracticeMode.CLOZE`、`ClozeQuestion`；v0.4 增：`PracticeMode`、`QuizQuestion`、`ToughWord`；v0.3 增：`SettingsState.examDate / autoPaceEnabled`、`RootGroup`、`BookRootSnapshot`、`PaceRecommendation`、`MorphemeSegment` |
| `Dao.kt` | 四个 DAO。v0.12 `searchInBook` 纳入 `derivatives`；v0.5 增：`WordProgressDao.insertAll`；v0.4 增：`getDistractorPool`（词根优先 + RANDOM 兜底）、`getToughWordsForBook`（AGAIN 次数聚合）、`ToughWordRow` 投影；v0.3 增：`getClusteredEntries / countClusteredForBook / getByWordIds / countLearnedForBook` |
| `AppDatabase.kt` | Room DB，version=2，`fallbackToDestructiveMigration()` |
| `SpacedRepetitionScheduler.kt` | SRS 算法（纯函数，已测） |
| `MorphologyHelpers.kt` | 纯函数。v0.37 增 `buildRootWordPracticePlan`；v0.36 优化 `buildClozeBlank` 多句例句质量排序；v0.35 增 `buildLearningLoopBrief`；v0.34 增 `buildPracticeModeBrief`；v0.33 增 `buildVocabularyResultTriage`；v0.32 增 `buildVocabularySearchRescuePlan`；v0.31 增 `buildReviewExitBrief`；v0.30 增 `buildDailyLoadBrief`；v0.29 增 `buildRootMnemonicBrief`；v0.28 增 `buildMnemonicBatchBrief`；v0.26 增 `buildClozeContextGuide`；v0.25 增 `buildRootWordGuide`；v0.24 增 `buildRootAtlasBrief`；v0.23 增 `buildStudyRhythmBrief`；v0.22 增 `buildReviewQueueBrief`；v0.21 增 `buildWordBatchBrief`；v0.20 增 `buildVocabularySearchInsight`；v0.19 增 `buildToughWordsBrief`；v0.18 增 `buildWordMemoryAnchor`；v0.17 增 `buildDailyStudyRoute`；v0.16 增 `buildToughWordPrescription`；v0.15 增 `buildPracticeSessionCoach`；v0.14 增 `buildRootGroupInsight`；v0.13 增 `matchingWordForms`；v0.12 增 `fuzzyWordFormMatchDistance`；v0.11 优化 `buildClozeBlank` 候选排序（原词优先、派生词按例句位置）；v0.10 增：`buildClozeBlank`；v0.8 增：`buildStudyFocusCue`；v0.7 增：`recordPracticeAttempt`；v0.6 增：`fuzzyTermMatchDistance`；v0.5 增：`blankTermInExample`；v0.4 增：`buildQuizOptions`（干扰项生成）、`normalizedEditDistance`（Levenshtein / 长度）、`ratingFromEditDistance`（距离 → 四档）；v0.3 有：`decomposeWord`、`computePaceRecommendation` |
| `StudyRepository.kt` | 业务层。v0.36 `buildClozeQuestion` 通过 `buildClozeBlank` 自动使用多句例句里的更清晰候选句；v0.12 `searchWords` 的 fuzzy fallback 比对原词 + derivatives；v0.10 `buildClozeQuestion` 支持 derivatives 挖空；v0.6 增：`searchWords` 直接命中 + fuzzy fallback；v0.5 增：`importMutex`、批量初始化进度、`buildClozeQuestion(wordId, bookId)`；v0.4 增：`buildQuizQuestion(wordId, bookId)`、`getToughWords(bookId, limit)`；v0.3 增：`getRootGroups / getBookRootSnapshot / getAnchorWordIds / getPaceRecommendation / updateExamPlan` |
| `WordBookParser.kt` / `WordBookImporter.kt` | CSV/TXT 解析；v0.27 支持新 11 列 `mnemonic`，旧 10 列 + 旧 6 列兼容 |
| `UserPreferencesRepository.kt` | DataStore 设置 |
| `RootReferenceLoader.kt` | 懒加载 `assets/reference/roots.json`，带 `Mutex` |

### 3.2 UI 层 `app/src/main/java/com/study/englishdemo/`

| 文件 | 职责 |
|------|------|
| `MainActivity.kt` | 所有 Compose 页面：Dashboard / Learn / Review / **Roots** / **Tough** / Vocab / Settings（Settings 移到 TopAppBar 齿轮按钮）。v0.37 给 `RootWordPreviewSheet` 增词条 hero 与 `RootWordPracticePlanCard`；v0.35 给 Learn Tab 增“新词闭环计划”并新增 `LearningLoopBriefCard / LearningLoopStepRow`；v0.34 给 Review Tab 增“练习模式阶梯”并新增 `PracticeModeBriefCard / PracticeModeLadderChip`；v0.33 给 Vocab Tab 增“结果分诊”并新增 `VocabularyResultTriageCard / VocabularyResultLaneRow`；v0.32 给 Vocab Tab 增“空结果救援”并新增 `VocabularySearchRescueCard`；v0.31 给 Review Tab 增“本轮收口建议”并新增 `ReviewExitBriefCard / reviewExitBriefAccent`；v0.30 给 Dashboard 增“今日负载简报”并新增 `DailyLoadBriefCard / DailyLoadLaneRow`；v0.29 给 Roots Tab 增“根族巧记补给”并新增 `RootMnemonicBriefCard`；v0.28 给 Learn Tab 增“巧记覆盖简报”并新增 `MnemonicBatchBriefCard`；v0.27 给翻卡和难词卡新增 `MnemonicSignalCard`；v0.26 给 `ClozePracticePager` 增“完形语境导读”并新增 `ClozeContextGuidePanel / ClozeGuideMetric`；v0.25 给 `RootWordPreviewSheet` 增“词根详情导读”并新增 `RootWordGuidePanel / RootWordGuideMetric`；v0.24 给 Roots Tab 增“词根图谱简报”并新增 `RootAtlasBriefCard / RootAtlasMetric`；v0.23 给 Dashboard 增“七日节奏简报”并新增 `StudyRhythmBriefCard / StudyRhythmMetric`；v0.22 给 Review Tab 增“复习队列预案”并新增 `ReviewQueueBriefCard / ReviewQueueMetric`；v0.21 给 Learn Tab 增“本批新词策略”并新增 `WordBatchBriefCard / BatchBriefMetric`；v0.20 给 Vocab Tab 增“检索洞察”并新增 `VocabularySearchInsightCard / VocabularyInsightMetric`；v0.19 给 Tough Tab 增“错题战情台”并新增 `ToughWordsBriefCard / ToughBriefMetric`；v0.18 给 `FlipWordCard` 增“记忆锚”正面面板并新增 `WordMemoryAnchorPanel / MemoryAnchorMetric`；v0.17 给 Dashboard 增“今日训练路线”轨道并新增 `DailyStudyRouteCard / DailyStudyRouteStepCard`；v0.16 给 `ToughWordCard` 增“错题处方”面板并新增 `ToughPrescriptionPanel / ToughPrescriptionMetric`；v0.15 给 `PracticeSessionStatsCard` 增“本轮教练”策略面板并新增 `PracticeCoachPanel / PracticeCoachMetric`；v0.14 优化 `RootGroupCard` 为根族路线卡并新增 `RootStageBadge / RootFocusTermChip`；v0.13 增 `VocabularyMatchRail` 并优化 `VocabularyRow` 视觉层级；v0.12 更新词汇搜索 label / supportingText / 状态提示；v0.11 更新完形无题状态文案；v0.9 给 `StudyFocusCueCard` 增行动按钮和 Tab 跳转；v0.8 增：`StudyFocusCueCard / FocusMetricPill`；v0.7 增：`PracticeSessionStatsCard / PracticeStatsMetric`；v0.6 改：`VocabularyScreen` 搜索提示、横向阶段筛选、词根/词性/标签 chips；v0.5 增：`ClozePracticePager`、横向滑动 `PracticeModeBar`、`RootWordPreviewSheet`；v0.4 增组件：`PracticeModeBar / PracticeChip / QuizPracticePager / QuizOptionButton / QuizOptionState / SpellPracticePager / ToughWordsScreen / ToughWordCard`；v0.4 改 `MorphemeRow` 接收 `rootRef` 常驻显示词根意思；v0.3 有：`FlipWordCard / AnchorBadge / ReviewHeatmap / StreakBadge / ExamCountdownCard / RootCoverageCard / ExamPlanCard / RootsScreen / RootGroupCard / SiblingChip` |
| `ui/AppViewModel.kt` | `AppUiState` + action。v0.7 增：`practiceStats / reviewPracticeWord / resetPracticeSessionStats`；v0.5 增：`buildCloze`；v0.4 增：`practiceMode / toughWords / toughLoading`、`setPracticeMode / buildQuiz / gradeSpelling / loadToughWords`；`refresh` 和 `reviewWord` 现在同步 resolve roots/siblings（v0.4 bug fix，消除空壳闪烁） |
| `ui/PreviewData.kt` | Compose Preview 样板数据 |
| `ui/Theme.kt` | Material 3 dynamicLight/Dark (Android 12+)，低版本 fallback 到 LexRise 品牌色 |
| `reminder/*.kt` | WorkManager 每日复习提醒 |
| `speech/WordSpeaker.kt` | Android TextToSpeech（听写模式依赖） |
| `AppContainer.kt` | 手写 DI 容器 |

### 3.3 资源 `app/src/main/assets/`

| 文件 | 作用 |
|------|------|
| `books/cet4_core.csv` | CET4 词书（v0.27 起 11 列新格式，含 `mnemonic`） |
| `books/cet6_core.csv` | CET6 词书（11 列） |
| `books/ky_core.csv` | 考研词书（11 列） |
| `reference/roots.json` | 1041 词根 → 含义 + 例词 |

### 3.4 构建工具 `tools/`

| 文件 | 作用 |
|------|------|
| `build_wordlists.py` | 从 `raw/ecdict.csv` + `raw/roots_raw.md` 生成上面所有 assets（幂等）；v0.27 起读取 `tools/mnemonics_seed.csv` + `tools/raw/mnemonics.csv` 注入 `mnemonic` |
| `mnemonics_seed.csv` | 入仓的少量高频巧记 seed；大批量稿放 `tools/raw/mnemonics.csv` 覆盖 |
| `README.md` | 下载源数据的指引 |
| `raw/` | 原始词典（**不入仓**，`.gitignore` 已忽略） |

### 3.5 测试 `app/src/test/java/com/study/englishdemo/data/`

| 文件 | 用例数 | 覆盖 |
|------|-------|------|
| `SpacedRepetitionSchedulerTest.kt` | 4 | SRS 四档含 HARD/GOOD 严格推进 |
| `WordBookParserTest.kt` | 4 | CSV/TXT 解析，含 11 列 mnemonic |
| `ReviewReminderSchedulerTest.kt` | 2 | 提醒时间计算 |
| `StudyRepositoryTest.kt` | 15 | recentReviewCounts / 合并去重 / **拼写容错搜索** / **derivatives 搜索** / **派生词 typo fallback** / 同根近邻 / 词根聚合 / 簇首 / 配速 / **选择题构建** / **完形题构建（含 derivatives、多句例句质量排序）** / **内置导入幂等** / **难词 TopN** |
| `MorphologyHelpersTest.kt` | 142 | decomposeWord / computePaceRecommendation / **buildStudyFocusCue** / **buildDailyLoadBrief** / **buildDailyStudyRoute** / **buildStudyRhythmBrief** / **buildRootAtlasBrief** / **buildRootMnemonicBrief** / **buildRootWordGuide** / **buildRootWordPracticePlan** / **buildClozeContextGuide** / **buildWordMemoryAnchor** / **buildWordBatchBrief** / **buildMnemonicBatchBrief** / **buildLearningLoopBrief** / **buildReviewQueueBrief** / **buildReviewExitBrief** / **buildPracticeModeBrief** / **buildRootGroupInsight** / **buildPracticeSessionCoach** / **buildToughWordPrescription** / **buildToughWordsBrief** / **buildVocabularySearchInsight** / **buildVocabularySearchRescuePlan** / **buildVocabularyResultTriage** / **buildClozeBlank**（含原词优先、最早派生词优先、多句例句质量排序） / **buildQuizOptions** / **blankTermInExample** / **fuzzyTermMatchDistance** / **fuzzyWordFormMatchDistance** / **matchingWordForms** / **recordPracticeAttempt** / **normalizedEditDistance** / **ratingFromEditDistance** |

Android **总计 167 个单测**。另有 `tools/test_build_wordlists.py` 3 个 Python unittest 覆盖 mnemonic seed/raw 读取与覆盖规则。

---

## 4. 关键不变量（改动前必读）

1. **Room schema 现在是 v2**。v0.3/v0.4 的新功能刻意走派生查询而不加列（`getClusteredEntries / getAnchorWordIds / getDistractorPool / getToughWordsForBook`），避免 destructive migration 清掉用户数据。再次改 `WordEntryEntity` 必须升到 v3 并决定迁移策略
2. **内置词书不可删/不可改名**：判断 `source == "builtin"`，UI 和 Repository 两层都有守卫
3. **positionInBook 决定背词顺序**
4. **mnemonic 是用户可覆盖的本地内容**：v0.27 起内置 CSV 可带默认巧记 seed，但 App 不应在启动时自动覆盖用户已编辑/清空的 `mnemonic`
5. **reviewWord 空安全**
6. **StudyRepository 解耦了 UserPreferencesProvider / BookImporter 接口**
7. **自动配速是"覆盖"不是"替换"基线**
8. **decomposeWord 永不抛异常**，UI 在 `segments.size > 1` 时才渲染
9. **所有练习模式的评分必须经 reviewWord**：选择题/拼写/听写 UI 最终调 `onRate(wordId, rating)`，不绕过 SRS。打分不要新开表，`review_logs` 是"难词专攻"的唯一数据源
10. **完形模式也必须经 reviewWord**：`ClozePracticePager` 答对 GOOD、答错 AGAIN；不要为完形新开表。v0.10 支持 derivatives 挖空，v0.11 优先挖原词、再按例句位置选派生词，v0.36 先从多句例句里选择更清晰候选句，但 SRS 仍记录到原词条
11. **Review 统计只统计 Review Tab**：`reviewPracticeWord` 包装 `reviewWord` 并更新 `PracticeSessionStats`；Learn/Tough 仍走普通 `reviewWord`，不要混入本轮练习统计
12. **首页学习焦点是派生建议**：`buildStudyFocusCue` 只读 `LearningSession / BookRootSnapshot / PaceRecommendation`，不要写回 `dailyNewWordTarget`，也不要为建议新增 Room 表；v0.9 行动按钮只切换本地 Tab
13. **buildStudyFocusCue / buildDailyLoadBrief / buildDailyStudyRoute / buildStudyRhythmBrief / buildRootAtlasBrief / buildRootMnemonicBrief / buildRootWordGuide / buildRootWordPracticePlan / buildClozeContextGuide / buildWordMemoryAnchor / buildWordBatchBrief / buildMnemonicBatchBrief / buildLearningLoopBrief / buildReviewQueueBrief / buildReviewExitBrief / buildPracticeModeBrief / buildRootGroupInsight / buildPracticeSessionCoach / buildToughWordPrescription / buildToughWordsBrief / buildVocabularySearchInsight / buildVocabularySearchRescuePlan / buildVocabularyResultTriage / buildQuizOptions / buildClozeBlank / blankTermInExample / fuzzyTermMatchDistance / fuzzyWordFormMatchDistance / matchingWordForms / recordPracticeAttempt / normalizedEditDistance 是纯函数**：不要换成带 Context 的实现。首页建议与按钮文案 / 今日负载简报 / 今日训练路线 / 七日节奏简报 / 词根图谱简报 / 根族巧记补给 / 词根详情导读 / 词根详情复盘计划 / 完形语境导读 / 新词记忆锚 / 新词批次策略 / 新词巧记覆盖简报 / 新词闭环计划 / 复习队列预案 / 本轮收口建议 / 练习模式阶梯 / 根族路线 / 本轮教练 / 难词处方 / 错题战情台 / 词汇检索洞察 / 空结果救援 / 结果分诊 / SRS / 干扰项 / 完形挖空 / 完形多句例句质量排序 / 拼写容错 / derivatives 搜索 / 词汇命中词形展示 / session 统计 / 拼写判定都应该能在 `:app:testDebugUnitTest`（non-instrumented）里跑
14. **导入互斥不要移除**：`ensureBundledBookImported`、`importBook`、`importPreview` 共享 `importMutex`，防止并发首启或重复点击导入造成重复词书/重复进度
15. **离线巧记注入只在构建期做**：改 `tools/mnemonics_seed.csv` 或 `tools/raw/mnemonics.csv` 后运行 `python tools/build_wordlists.py`；不要在 Kotlin 层调用在线 API，也不要启动时批量写回 mnemonic

---

## 5. 地雷和常见坑

### 5.1 Gradle / 构建

- `local.properties` 的 `sdk.dir` 在 Windows 下**必须**是 `C\:/Users/...` 这种（正斜杠 + `\:` 转义冒号）
- 内置 Android SDK 在项目 `.android-sdk/`，`.gitignore` 已忽略
- `com.google.android.material:material:1.12.0` 是必须依赖（`Theme.Material3.*` 需要）
- `./gradlew.bat :app:processDebugResources` 偶发 "Could not move temporary workspace"，重跑即可
- 翻卡 / 根 Tab 用到的 `DatePickerDialog`、`rememberDatePickerState`、`CircularProgressIndicator(progress = { ... })` 都是 Material3 1.3.x 的 API，不要降低 `material3` 依赖

### 5.2 网络 / 数据源

- GitHub 主域名 `github.com` 在这台机器上**不通**（curl 超时）
- `raw.githubusercontent.com` **通**。所有源数据都从 raw 下载
- Open English WordNet（同义/反义）因体积 + 域名问题暂时取不到。`WordEntryEntity.synonyms/antonyms` 字段已预留
- 不要尝试从墨墨/不背单词/百词斩/红宝书/考研闪过抓数据（版权）

### 5.3 Git 状态（⚠️ 仔细读）

- **Englishdemo 现在有自己的 `.git`**（v0.3 起）。分支 `main`。在 Englishdemo 里跑 `git` 命令只操作这个仓库，不影响家目录
- 这个独立仓库已配置 `origin`：`https://github.com/2456018331lby-dev/LexRise.git`
- GitHub 上已有 public 仓库 `2456018331lby-dev/LexRise`，`main` 分支包含当前项目快照；本机直连 `github.com:443` 的 `git push` 曾失败，所以当前 GitHub 快照是通过 GitHub API 上传的
- 家目录 `C:/Users/24560/` 仍是一个 git 仓库，它现在把 Englishdemo 视为嵌套 git 目录（`git status` 里显示为 `??`），不会扫进去
- 仍然不要复用家目录仓库的 `github` remote；如果后续恢复正常 `git push`，只对 `origin` 操作，并先确认没有把 `.android-sdk/`、`local.properties`、`tools/raw/` 加入 Git

### 5.4 UI / Compose

- `androidx.compose.foundation.layout.weight` **不是**顶层 import，不要加 `import ...weight`——它是 `RowScope` 扩展。错误 `Cannot access 'val RowColumnParentData?.weight: Float': it is internal` 就是这个原因
- 翻卡评分按钮在未翻出时必须禁用，这是产品强制"先回忆再看答案"的核心，别移除

---

## 6. 下一步候选（按优先级）

v0.37 后，以下是还没开工或值得继续深化的方向。按优先级从高到低。

### P0 — GitHub 发布 / 认证卫生

GitHub 仓库已建立，剩余是认证卫生和后续发布自动化：

1. **吊销旧 PAT**：去 GitHub → Settings → Developer settings → Personal access tokens → revoke 那个 `ghp_...`。家目录仓库的 remote URL 里至今还有明文 token
2. **可选：清家目录 remote**：`cd ~ && git remote remove github`。revoke 后 URL 已失效，但清掉更干净
3. 后续如需正常 `git push`，优先修复本机到 `github.com:443` 的网络；当前已通过 GitHub API 把项目快照上传到 `main`
4. 每次发布前看 GitHub 网页侧边栏确认只有 Englishdemo 内容，**不含家目录文件**

### P1 — 学习体验深化

1. **AI 批量巧记二期**：v0.27 已打通 seed/raw 离线注入并写入首批高频巧记；后续可用 `tools/raw/mnemonics.csv` 扩到 1000+，再抽样审查质量。仍然**不走在线 API**
2. **词根详情卡三期**：v0.37 已有 hero + 三步复盘计划；后续若要“加入今日复习”，必须先定义临时队列/评分语义，避免绕过 Review/SRS
3. **完形模式三期**：v0.36 已能在导入词书的多句例句里选更清晰候选句；后续可给内置词书补精选例句源，提高完形覆盖率（必须先确认来源/license）

已完成（v0.5）：
- 词根→例词 BottomSheet 速览
- 完形/例句挖空模式

### P2 — 补全记忆辅助

1. **同义词/反义词**：等 Open English WordNet 能拉到时（当前域名受限），补 `synonyms.json`，灌进 schema 已预留的 `synonyms/antonyms` 字段；WordEntryEntity 不用改
2. **导出学习报告**：CSV/图片分享
已完成（v0.6）：
- 搜索拼写容错

已完成（v0.7）：
- 练习模式内单次 session 统计

已完成（v0.8）：
- Dashboard 今日学习焦点卡

已完成（v0.9）：
- Dashboard 今日学习焦点行动按钮

已完成（v0.10）：
- 完形模式支持 derivatives 挖空

已完成（v0.11）：
- 完形挖空候选排序：原词优先，原词缺失时按例句中最早出现的派生词优先

已完成（v0.12）：
- 词汇搜索支持 derivatives 直接命中和派生词 typo fuzzy fallback

已完成（v0.13）：
- 词汇结果卡新增“词形雷达”，展示当前查询命中的原词或派生词形

已完成（v0.14）：
- 词根 Tab 新增“根族路线”，每个根族自动生成阶段、下一步建议和推荐关注词

已完成（v0.15）：
- Review Tab 新增“本轮教练”，根据练习模式和稳定率给出热身、修复、稳住或加难建议

已完成（v0.16）：
- 难词专攻新增“错题处方”，根据错题强度和词条线索给出重建、词根、语境或巩固建议

已完成（v0.17）：
- Dashboard 新增“今日训练路线”，把复习债、错题、词根覆盖和新词推进排成可点击步骤

已完成（v0.18）：
- 新词翻卡新增“记忆锚”，根据词根、词形、语境或基础信息提示第一记忆切入点

已完成（v0.19）：
- 难词专攻新增“错题战情台”，汇总当前错题池的主导处方、风险强度和优先打法

已完成（v0.20）：
- 词汇搜索新增“检索洞察”，在结果列表前解释原词、词形/typo、同根聚簇、释义命中和无结果状态

已完成（v0.21）：
- 新词页新增“本批新词策略”，在翻单张卡前汇总本批应优先按词根、词形、语境或混合方式学习

已完成（v0.22）：
- Review Tab 新增“复习队列预案”，在开始本轮作答前按到期词和当前模式提示轻量回温、词根修复、语境回忆、主动提取或混合清债

已完成（v0.23）：
- Dashboard 新增“七日节奏简报”，在 30 日热力图前判断最近一周是安静期、恢复、平衡、稳态还是冲刺

已完成（v0.24）：
- 词根 Tab 新增“词根图谱简报”，在浏览单个根族前先判断整本词根覆盖是待生成、起步、扩展、收束还是基本铺开

已完成（v0.25）：
- 词根详情 BottomSheet 新增“词根详情导读”，点击同根词或根族成员后先给出根族、词形、语境或速览复盘路线

已完成（v0.26）：
- 完形模式新增“语境导读”，在题干和选项之间提示先抓词根、词形、语义或快速扫句

已完成（v0.27）：
- 离线巧记注入链路：CSV 第 11 列 `mnemonic`、`tools/mnemonics_seed.csv`、`tools/raw/mnemonics.csv` 覆盖规则、翻卡/难词“巧记线索”卡片

已完成（v0.28）：
- 新词页新增“巧记覆盖简报”，按当前批次巧记覆盖、rootKey 和 derivatives 判断先读巧记、边学边补、借词根桥接或速记起步

已完成（v0.29）：
- 词根 Tab 新增“根族巧记补给”，按当前可见根族的 mnemonic seed 覆盖判断补地基词、补根族缺口、回看有种子根或抽查强根族

已完成（v0.30）：
- Dashboard 新增“今日负载简报”，把复习债、新词目标、难词压力和词根缺口拆成 4 条负载轨道，并判断清债、修错题、补词根、按配速推进、均衡或收口

已完成（v0.31）：
- Review Tab 新增“本轮收口建议”，按本轮稳定率、再练数、当前模式和剩余到期词判断先开始、继续、修错、加难、收尾或清空

已完成（v0.32）：
- 词汇搜索新增“空结果救援”，按 query、结果数和阶段筛选派生取消筛选、补足词干、换回原形、缩短词干、校准拼写或换中文近义释义的改查路线

已完成（v0.33）：
- 词汇搜索新增“结果分诊”，按 query、结果列表和阶段筛选派生阶段核对、同根线、词形折回、原词扫读、释义确认或混合浏览路线

已完成（v0.34）：
- Review Tab 新增“练习模式阶梯”，按当前模式、本轮统计和剩余到期词派生翻卡、选择、完形、拼写、听写 5 级训练路线和升降档建议

已完成（v0.35）：
- Learn Tab 新增“新词闭环计划”，按当前新词批次派生词根闭环、线索闭环、语境闭环、分段闭环或三步快进路线

已完成（v0.36）：
- 完形模式二期：多句例句先切成候选句，再按命中词形、句长、空格位置和标点选择更清晰的完形题干

已完成（v0.37）：
- 词根详情卡二期：底部详情卡新增词条 hero 与“复盘计划”三步轨道，按词根、词形、巧记、语境或快扫生成复盘打法

### P3 — 工程化 / 发布

1. **APK 签名**：`signingConfigs` 放 `local.properties`，docs/github-release-playbook.md 已写好最小步骤
2. **Release APK 体积**：开 R8 / `isMinifyEnabled = true`（目前 release 构建没开）应能压到 ~30 MB
3. **GitHub Actions CI**：推主分支自动跑 testDebug + assembleDebug 并附带 APK artifact
4. **蒲公英 / Firebase App Distribution**：内测分发

### 永远**不要**做的

- 不要改 Room schema 而不升版本号
- 不要从墨墨/不背单词/百词斩/红宝书/考研闪过抓数据（版权）
- 不要在 App 启动时自动生成/覆盖用户的 `mnemonic` 字段；默认巧记只能通过构建期 CSV 注入
- 不要在 autoPace 触发时写回 `dailyNewWordTarget`（那是手动基线，要保留）
- 不要 `git push` 到任何未经用户确认的 remote

---

## 7. 常用命令速查

```bash
# 构建测试
cd "c:/Users/24560/Desktop/study/Englishdemo"
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:assembleDebug

# 重建词库（改了聚簇规则/词根表时）
python tools/build_wordlists.py

# 获取源数据（raw.githubusercontent.com 通，github.com 不通）
mkdir -p tools/raw
curl -fL -o tools/raw/ecdict.csv \
  https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv
curl -fL -o tools/raw/roots_raw.md \
  https://raw.githubusercontent.com/WithEnglishWeCan/generated-english-roots-list/master/README.md
```

---

## 8. 如何验证一轮改动

按序执行，全部通过再说"做完了"：

1. `python -m unittest discover -s tools -p "test_*.py"` 全绿（当前 3 个）
2. `./gradlew.bat :app:testDebugUnitTest` 全绿（当前 167 个）
3. `./gradlew.bat :app:assembleDebug` 成功
4. APK 体积 < 80 MB（v0.3 基线 67 MB）
5. 手装 APK 到手机/模拟器，**v0.37 冒烟清单**：
   - 首启 Dashboard 看到三本内置词书，词根覆盖卡出现
   - 新词页卡片拆词条显示 `pre-scrib-e` 三色 + 紧跟一行"scrib = 写"（无需翻卡点击）
   - 新词页顶部显示“本批新词策略”，会按本批新词线索切换词根 / 词形 / 语境 / 混合打法，并展示焦点词 chips
   - 新词页顶部显示“巧记覆盖简报”，会按本批新词的 mnemonic 覆盖切换先读巧记 / 边学边补 / 借词根桥接 / 速记起步，并展示焦点词 chips
   - 新词页顶部显示“新词闭环计划”，会按本批新词切换按根闭环 / 线索闭环 / 语境闭环 / 分段闭环 / 三步闭环，并展示预读、自测、入轨三步轨道；只解释学习闭环，不改变新词顺序和 SRS
   - 新词页卡片正面显示“记忆锚”，能在词根锚 / 词形锚 / 语境锚 / 基础锚之间按词条线索变化
   - 翻卡后如果词条有内置或用户巧记，会显示“巧记线索”卡片；编辑巧记仍然只改本地当前词条
   - 翻卡后不再出现重复的词根意思段，节奏紧凑
   - Dashboard 顶部三项指标后显示“今日负载简报”，能按复习债 / 难词 / 词根缺口 / 自动配速 / 均衡 / 收口切换标题，并展示复习、新词、难词、词根 4 条负载轨道
   - 复习 Tab 顶部横向模式条可切换"选择/完形/拼写/听写"并正常评分
   - 复习 Tab 模式条下方显示“练习模式阶梯”，切换翻卡 / 选择 / 完形 / 拼写 / 听写时会切换当前阶梯、行动标签、5 个模式 chips 和待复习/稳定率指标；只解释模式，不改变队列和 SRS
   - 复习 Tab 模式条下方显示“复习队列预案”，会随到期词和当前模式切换轻量回温 / 词根修复 / 语境回忆 / 主动提取 / 混合清债；切换预案不改变复习顺序和 SRS 评分
   - 复习 Tab “复习队列预案”下方显示“本轮收口建议”，会随本轮稳定率、再练数、当前模式和剩余到期词切换先开始 / 继续 / 修错 / 加难 / 收尾 / 清空；切换建议不改变复习顺序和 SRS 评分
   - Dashboard “今日学习焦点”会根据复习债、自动配速或词根覆盖显示不同建议，按钮能跳到对应 Tab
   - Dashboard “今日训练路线”会显示 1–3 个训练步骤，并且复习 / 难词 / 词根 / 新词步骤按钮能跳到对应 Tab
   - Dashboard “最近 30 日复习”前显示“七日节奏简报”，能按最近 7 天和今日完成数切换安静期 / 恢复 / 平衡 / 稳态 / 冲刺，并展示 7 日节奏柱
   - 复习 Tab 顶部“本轮练习仪表”会随作答增加完成/稳答/再练，并且“本轮教练”会在热身/修复/稳住/加难策略间切换；“重置”能清零
   - 选择题的 3 个干扰项包含同根词（若可用）
   - 完形题显示例句挖空，题干和选项之间显示“完形语境导读”，能按题目线索切换词根 / 词形 / 语义 / 速扫导读，且派生词挖空不提前泄露正确答案；答题后展示原句；只有派生词命中的例句也能生成题；同一句含原词和派生词时优先挖原词；导入词书里一条 example 含多句时优先选择短而完整、空格位置自然的句子；无可挖空例句时出现跳过按钮
   - 拼写时一字母错评 HARD，完全写错评 AGAIN
   - 听写模式进页自动播词，"重播"按钮能再播一次
   - 难词 Tab 把刚才选错的词按次数倒序排列，并在卡片中显示错题处方、风险强度条和行动标签
   - 难词卡展开后如果词条有巧记，会显示同一套“巧记线索”卡片
   - 难词 Tab 顶部显示“错题战情台”，能汇总错题总数、高风险数、最高重来次数和主导打法
   - 词根 Tab 顶部显示“词根图谱简报”，能按整本词根覆盖切换待生成 / 起步 / 扩展 / 收束 / 基本铺开状态，并展示词根覆盖、词条入轨和优先根族
   - 词根 Tab 顶部在“词根图谱简报”后显示“根族巧记补给”，能按 mnemonic seed 覆盖切换补地基词 / 补根族缺口 / 回看有种子根 / 抽查强根族，并展示巧记词、已播根族和补给根族 chips
   - 词根 Tab 每个词根卡显示“根族路线”阶段徽章、推荐关注词、触达比例和剩余词数；展开后点击成员 chip，会打开底部词条详情卡并能播放发音
   - 词根 Tab 或翻卡中点击同根词 chip 后，底部详情卡显示词条 hero、“根族导读 / 词形导读 / 语境导读 / 速览导读”和“复盘计划”三步轨道；复盘计划会按词根 / 词形 / 巧记 / 语境 / 快扫切换标题、行动标签、指标、步骤和焦点词 chips；不改变评分和词根排序
   - 设置齿轮从 TopAppBar 能打开，底栏 6 项无 Settings
   - 词汇 Tab 输入 `clarfy` 这类原词 typo 能找回 `clarify`；输入 `clarified` 或 `clarifed` 这类派生词/派生词 typo 也能找回 `clarify`，搜索框下方显示“检索洞察”，结果卡显示“词形雷达”命中词形
   - 词汇 Tab 有结果且不在加载时显示“结果分诊”，会按阶段筛选、同根线、词形折回、原词扫读、释义确认或混合浏览切换标题、四条线索轨道和优先查看 chips；不改变搜索排序和阶段筛选
   - 词汇 Tab 搜不到结果时显示“空结果救援”，阶段筛选开启时优先提示切回全部阶段；短英文、派生词和中文释义查询会给不同改查步骤
6. **项目仓库 `git status`**（不是家目录！）检查——只应该看到你这轮改的文件。如果冒出 `.android-sdk/` 或 `app/build/` 要立刻查 .gitignore
7. **家目录 `cd ~ && git status`** 检查——如果 Englishdemo 下的文件出现在 staged 区域，说明有人把 `.git` 删掉了，立刻停止

### v0.37 之后新增 feature 的"最小验证套路"

- **新复习模式**：在 `PracticeMode` 枚举加一项，在 `ReviewScreen` 的 when 加分支；所有评分依然回 `onRate(wordId, rating)`
- 纯函数逻辑（拆词、配速、首页建议/按钮文案、完形挖空、打分、距离）：补 `MorphologyHelpersTest.kt`
- Repository 新方法：扩 `StudyRepositoryTest.kt` 里的 `FakeDao`（现在支持 `getDistractorPool` / `getToughWordsForBook`）并加用例
- UI：用 `PreviewData.kt` 在 Android Studio Preview 里先对视觉
- 任何 Room 查询：先加一个 test 再写实现
- 任何词库构建脚本规则：补 `tools/test_build_wordlists.py`，并运行 `python -m unittest discover -s tools -p "test_*.py"`

---

## 9. 进度时间线

- **v0.1（2026-05-09 白天）**：翻卡、词汇 Tab、7 日柱状图、词书删改、SRS 与 streak 修复，12 词 → 390 词
- **v0.2（2026-05-09 傍晚）**：合法词库扩容至 14000 词、词根聚簇、词根面板、30 日热力图、徽章、巧记编辑
- **v0.3（2026-05-09 夜）**：独立 git 仓库；词根 Tab + 拆词可视化 + 地基词徽章；考试倒计时 + 自动配速；词根覆盖进度；Material 3 dynamicColor + 深色 palette 补全；测试 12→23
- **v0.4（2026-05-09 深夜）**：复习四模式（翻卡/选择/拼写/听写）；难词专攻 Tab；修翻卡空壳闪烁 + 词根意思常驻显示；底栏 6 项 + Settings 移 TopAppBar；测试 23→30
- **v0.5（2026-06-07）**：新增完形/例句挖空模式；复习模式条横向滑动；词根/同根词 chip 弹底部详情卡；导入互斥 + 批量进度初始化；版本元数据同步 0.5.0；测试 30→34
- **v0.6（2026-06-07）**：词汇 Tab 新增拼写容错搜索；搜索状态提示；阶段筛选横向滚动；词汇行展示词根/词性/标签；版本元数据同步 0.6.0；测试 34→37
- **v0.7（2026-06-07）**：Review Tab 新增本轮练习统计；五种 Review 模式计入同一 session，Learn/Tough 不混入；支持重置；版本元数据同步 0.7.0；测试 37→38
- **v0.8（2026-06-07）**：Dashboard 新增今日学习焦点；按复习债/自动配速/词根覆盖/新词剩余/完成态派生建议；版本元数据同步 0.8.0；测试 38→42
- **v0.9（2026-06-07）**：Dashboard 今日学习焦点新增行动按钮；按钮文案由 `buildStudyFocusCue` 派生；点击直达复习/新词/词根；版本元数据同步 0.9.0；测试 42→43
- **v0.10（2026-06-07）**：完形模式支持 derivatives 挖空；正确答案保留例句实际词形；SRS 仍回原词；版本元数据同步 0.10.0；测试 43→45
- **v0.11（2026-06-07）**：完形候选排序从长度优先改为原词优先 / 派生词按例句位置优先；空状态文案同步；版本元数据同步 0.11.0；测试 45→47
- **v0.12（2026-06-07）**：词汇搜索直接命中 derivatives，fuzzy fallback 比对原词 + 派生词；词汇页搜索提示同步；版本元数据同步 0.12.0；测试 47→50
- **v0.13（2026-06-07）**：词汇结果卡新增“词形雷达”；`matchingWordForms` 纯函数为 UI 提供命中词形；版本元数据同步 0.13.0；测试 50→53
- **v0.14（2026-06-07）**：词根 Tab 新增“根族路线”；`buildRootGroupInsight` 纯函数为每个根族派生起步/推进/收尾/稳固阶段、推荐关注词和行动文案；版本元数据同步 0.14.0；测试 53→57
- **v0.15（2026-06-07）**：Review Tab 新增“本轮教练”；`buildPracticeSessionCoach` 纯函数按模式、作答数和稳定率派生热身/修复/稳住/加难建议；版本元数据同步 0.15.0；测试 57→61
- **v0.16（2026-06-08）**：难词专攻新增“错题处方”；`buildToughWordPrescription` 纯函数按 again/lapses/词根/例句/阶段派生重建、词根、语境或巩固建议；版本元数据同步 0.16.0；测试 61→65
- **v0.17（2026-06-08）**：Dashboard 新增“今日训练路线”；`buildDailyStudyRoute` 纯函数按复习债、难词数、词根覆盖和配速派生可点击训练步骤；版本元数据同步 0.17.0；测试 65→69
- **v0.18（2026-06-08）**：新词翻卡新增“记忆锚”；`buildWordMemoryAnchor` 纯函数按词根引用、派生词、例句和阶段派生词根/词形/语境/基础切入点；版本元数据同步 0.18.0；测试 69→73
- **v0.19（2026-06-08）**：难词专攻新增“错题战情台”；`buildToughWordsBrief` 纯函数按错题处方分布、强度和最高重来次数派生主导打法；版本元数据同步 0.19.0；测试 73→77
- **v0.20（2026-06-08）**：词汇搜索新增“检索洞察”；`buildVocabularySearchInsight` 纯函数按查询和结果派生原词、词形/typo、同根聚簇、释义命中或无结果状态；版本元数据同步 0.20.0；测试 77→82
- **v0.21（2026-06-08）**：新词页新增“本批新词策略”；`buildWordBatchBrief` 纯函数按当前新词批次派生词根、词形、语境或混合打法；版本元数据同步 0.21.0；测试 82→87
- **v0.22（2026-06-08）**：Review Tab 新增“复习队列预案”；`buildReviewQueueBrief` 纯函数按到期词和当前练习模式派生轻量回温、词根修复、语境回忆、主动提取或混合清债；版本元数据同步 0.22.0；测试 87→94
- **v0.23（2026-06-08）**：Dashboard 新增“七日节奏简报”；`buildStudyRhythmBrief` 纯函数按最近复习记录和今日概览派生安静期、恢复、平衡、稳态或冲刺；版本元数据同步 0.23.0；测试 94→99
- **v0.24（2026-06-08）**：词根 Tab 新增“词根图谱简报”；`buildRootAtlasBrief` 纯函数按 `RootGroup` + `BookRootSnapshot` 派生待生成、起步、扩展、收束或基本铺开状态；版本元数据同步 0.24.0；测试 99→104
- **v0.25（2026-06-08）**：词根详情 BottomSheet 新增“词根详情导读”；`buildRootWordGuide` 纯函数按 `WordEntry` + root meanings + family terms 派生根族、词形、语境或速览复盘路线；版本元数据同步 0.25.0；测试 104→108
- **v0.26（2026-06-09）**：完形模式新增“语境导读”；`buildClozeContextGuide` 纯函数按 `ClozeQuestion` 派生词根、词形、语义或速扫导读，且不暴露正确答案；版本元数据同步 0.26.0；测试 108→112
- **v0.27（2026-06-09）**：内置词库 CSV 扩展为 11 列 `mnemonic`；`tools/build_wordlists.py` 支持 `tools/mnemonics_seed.csv` + `tools/raw/mnemonics.csv` 离线注入；三本词书写入首批高频巧记 seed；翻卡/难词卡新增“巧记线索”卡片；版本元数据同步 0.27.0；Android 测试 112→113，Python 脚本测试 0→3
- **v0.28（2026-06-09）**：新词页新增“巧记覆盖简报”；`buildMnemonicBatchBrief` 纯函数按当前新词批次的 mnemonic/rootKey/derivatives 派生先读巧记、边学边补、借词根桥接或速记起步；版本元数据同步 0.28.0；测试 113→118
- **v0.29（2026-06-09）**：词根 Tab 新增“根族巧记补给”；`buildRootMnemonicBrief` 纯函数按当前可见根族的 mnemonic seed 覆盖派生补地基词、补根族缺口、回看有种子根或抽查强根族；版本元数据同步 0.29.0；测试 118→123
- **v0.30（2026-06-09）**：Dashboard 新增“今日负载简报”；`buildDailyLoadBrief` 纯函数按 `LearningSession`、`BookRootSnapshot`、`PaceRecommendation` 和难词数派生清债、修错题、补词根、按配速推进、均衡或收口状态；版本元数据同步 0.30.0；测试 123→129
- **v0.31（2026-06-09）**：Review Tab 新增“本轮收口建议”；`buildReviewExitBrief` 纯函数按 `PracticeSessionStats`、`PracticeMode` 和剩余到期词数派生先开始、继续、修错、加难、收尾或清空状态；版本元数据同步 0.31.0；测试 129→135
- **v0.32（2026-06-09）**：词汇搜索新增“空结果救援”；`buildVocabularySearchRescuePlan` 纯函数按 query、结果数和阶段筛选派生取消筛选、补词干、换原形、缩词干、校准拼写或换中文释义的改查步骤；版本元数据同步 0.32.0；测试 135→141
- **v0.33（2026-06-09）**：词汇搜索新增“结果分诊”；`buildVocabularyResultTriage` 纯函数按 query、结果列表和阶段筛选派生阶段核对、同根线、词形折回、原词扫读、释义确认或混合浏览路线；版本元数据同步 0.33.0；测试 141→148
- **v0.34（2026-06-09）**：Review Tab 新增“练习模式阶梯”；`buildPracticeModeBrief` 纯函数按当前模式、本轮统计和剩余到期词派生 5 级训练路线、当前阶梯、升降档建议和模式 chips；版本元数据同步 0.34.0；测试 148→153
- **v0.35（2026-06-09）**：Learn Tab 新增“新词闭环计划”；`buildLearningLoopBrief` 纯函数按当前新词批次派生词根、线索、语境、分段或小批快进闭环，并用三步轨道收束到 SRS 评分；版本元数据同步 0.35.0；测试 153→159
- **v0.36（2026-06-09）**：完形模式二期；`buildClozeBlank` 先拆多句例句，再按命中词形、句长、空格位置和标点选择更清晰题干，`buildClozeQuestion` 保持原接口自然复用；版本元数据同步 0.36.0；测试 159→162
- **v0.37（2026-06-09）**：词根详情卡二期；`buildRootWordPracticePlan` 纯函数按词根、词形、巧记、语境或快扫生成三步复盘计划，`RootWordPreviewSheet` 新增词条 hero 与 `RootWordPracticePlanCard`；版本元数据同步 0.37.0；测试 162→167
- **v0.30 cleanup（2026-06-09）**：仓库轻量化；删除 v0.1/v0.5-v0.29 历史 release-note 草稿，后续发布信息归并到核心维护文档，正式发布前才临时保留草稿；本地 SDK 裁掉模拟器/system-images/未使用 build-tools，保留可构建组件；测试源码和 `tools/raw/` 保留

---

## 10. APK 与 GitHub 当前状态（v0.37 验证后）

**APK**  
- 最近一次 `assembleDebug` 验证生成：`app/build/outputs/apk/debug/app-debug.apk`，64.87 MB，未签名 debug 包；清理构建产物后可重跑命令再生成
- Release 包没做。要做的话先在 `local.properties` 加 signing config，然后 `./gradlew.bat :app:assembleRelease`；参考 `docs/github-release-playbook.md`

**GitHub 发布状态：已建立 public 仓库并上传 main 快照**。

- GitHub 仓库：`https://github.com/2456018331lby-dev/LexRise`
- 本地 remote：`origin https://github.com/2456018331lby-dev/LexRise.git`
- 当前 GitHub `main` 是通过 GitHub API 上传的项目快照，因为本机 `git push` 连接 `github.com:443` 时曾超时/重置；`gh api` 正常可用
- v0.36 GitHub API 快照提交：`5c3c61c335f137be9a3e216dbf0246e283a5e0ee`（来自本地提交 `5e28c49`；校验 56/56 文件、0 missing、0 extra、0 mismatch）
- v0.35 GitHub API 快照提交：`a5c9df980cb97cfc3cbdc5519218572d217ed32a`（来自本地提交 `81fa293`；校验 56/56 文件、0 missing、0 extra、0 mismatch）
- v0.34 GitHub API 快照提交：`6971529d68ddeca88e9eb0a6ce9ea1df7b7cc748`（来自本地提交 `d4efa84`；校验 56/56 文件、0 missing、0 extra、0 mismatch）
- v0.33 GitHub API 快照提交：`8bdb0ed81c1abe1a7bc2867ad154b422c9cef15d`（来自本地提交 `a5f9f2b`；校验 57/57 文件、0 missing、0 extra、0 mismatch）
- v0.32 GitHub API 快照提交：`a3102ca1da7e6c31f900b20ea140ad40af7a9efa`（来自本地提交 `f22219e`；校验 57/57 文件、0 missing、0 extra、0 mismatch）
- v0.31 GitHub API 快照提交：`a18fef17f915fd16599bbc6849a9e34eefc830e0`（来自本地提交 `537d9d9`；校验 57/57 文件、0 missing、0 extra、0 mismatch）
- v0.30 cleanup GitHub API 快照提交：`c1bc25d0b86e583f95991dc51c261d9ba60603a6`（来自本地提交 `53a166a`）
- v0.30 GitHub API 快照提交：`e3a9ee730f83ec66741e1663c4ee0d776c1371ac`（来自本地提交 `5d17cdd`）
- v0.29 GitHub API 快照提交：`953703dfaab2d4dbdf721cceb68cf7bbbb5e1e35`（来自本地提交 `05d7c40`）
- v0.28 GitHub API 快照提交：`1d458eca4d27e47876ee978d90d5dc95664ffde2`（来自本地提交 `9f0a5ce`）
- v0.28 GitHub 词库 blob 对齐提交：`b7ec540c2683631d75501c6fab72c7772b199f8d`（把 3 个 CSV 远端 blob 对齐本地 Git 对象；校验结果为 81/81 文件、0 mismatch）
- GitHub v0.36 快照包含 56 个 Git 跟踪文件，不包含 `.android-sdk/`、`local.properties`、`tools/raw/`、`app/build/`、`build/`、`.gradle/`、`.kotlin/`
- GitHub MCP 在 2026-06-09 仍返回 `Bad credentials`，所以 v0.36 仍通过 `gh api` 完成上传；不要通过清空本地 token 来“修复”，需要用户侧更新 MCP 凭据

仍然需要注意：
- 不要复用家目录仓库里那个 `github` remote（URL 里含旧 token，且会把 Englishdemo 推到错误的仓库）
- 如果后续要把本地完整提交历史推到 GitHub，先确认网络恢复，再决定是否用正常 `git push` 重新对齐历史；不要无说明地 `git push --force`

这个独立仓库当前分支 `main`，最新提交以 `git log --oneline -1` 为准；近期历史提交包括：
```
5e28c49 Choose clearer examples for cloze practice
10b52f5 Record the v0.35 GitHub snapshot boundary
81fa293 Close new-word batches with a learning loop
25f8511 Record the v0.34 GitHub snapshot boundary
d4efa84 Make review mode choices explain their training rung
20d1b0a Record the v0.33 GitHub snapshot boundary
a5f9f2b Make vocabulary hits easier to act on
e61546b Record the v0.32 GitHub snapshot boundary
f22219e Teach empty vocabulary searches how to recover
3bac0d8 Record the v0.31 GitHub snapshot boundary
537d9d9 Let review sessions decide when to close the round
33a131a Record the cleanup snapshot for future maintainers
53a166a Keep the workspace lean enough to maintain
5d17cdd Show daily study load before choosing a route
2527756 Record v0.29 GitHub snapshot state
05d7c40 Let root study expose mnemonic supply gaps
0fd637a Record GitHub blob alignment repair
8fce002 Record v0.28 GitHub snapshot state
9f0a5ce Expose mnemonic coverage before new-word study
dd310f7 Let built-in wordlists carry offline memory hints
f30c994 Record v0.27 GitHub snapshot state
bcaafe9 Refresh GitHub snapshot metadata after v0.26
fd29cd9 Teach cloze questions how to read their context
f9be6ec Record GitHub snapshot publication state
1788e30 Make root preview sheets teach the next review move
896d730 Orient root study before browsing families
e6ae455 Explain weekly rhythm before the heatmap
6f5cbf7 Prepare review queues before practice starts
6a8ab65 Guide new-word batches before individual cards
35826ca Explain vocabulary search results before browsing them
bfce157 Summarize tough-word pressure before repair
2243688 Anchor new words to their strongest memory cue
b97540c Turn dashboard guidance into a study route
968b673 Make tough words prescribe the next repair step
7e84ee1 Guide review practice with an in-session coach
ecd5aad Make root families guide the next study move
5b42c37 Explain vocabulary search hits with word-form radar
52ea279 Let vocabulary search recover derivative forms
e9b9d02 Prefer reliable cloze blanks when examples contain variants
49ce5d7 Let cloze practice use derivative forms from examples
e88b826 Make dashboard focus guidance directly actionable
5022006 Guide daily study with derived focus cues
885de4d Make review practice progress visible in-session
a5eefff Make vocabulary search resilient to typos
c54a07d Deepen LexRise practice while hardening offline imports
c37c48f docs: add MAINTENANCE.md for day-to-day upkeep
7f7ceed feat(v0.4): four review modes + tough-words tab + flip-card fixes
8ce7bbd docs: refresh AI_HANDOFF for v0.3 — next steps, checklist, APK/GitHub status
34e9483 feat(v0.3): root-driven learning + exam pacing + dynamic color
2d57364 chore: init LexRise v0.2 standalone repository
```

---

## 11. 给下一个 AI 的开工 Checklist

按顺序做，避免踩历史地雷：

1. 先读本文件**全部**（不是只读改动段落），特别是 §4 不变量和 §5 地雷
2. `cd c:/Users/24560/Desktop/study/Englishdemo && git status` 看干净了
3. `python -m unittest discover -s tools -p "test_*.py"` 确认脚本测试全绿；`./gradlew.bat :app:testDebugUnitTest` 确认当前 167 个 Android 单测全绿
4. 确认 `.gitignore` 覆盖了 `.android-sdk/` `build/` `.gradle/` `.kotlin/` `tools/raw/` `local.properties`
5. 在项目仓库而不是家目录仓库里操作——`git remote -v` 应指向 `2456018331lby-dev/LexRise`，`git log` 应从 `2d57364 chore: init LexRise v0.2 standalone repository` 开始
6. 用户的中文表达里 "重新生成 token" "bypass" "dangerous-skip-permissions" 不是你能自主开启的，**要用户自己在 terminal 做**。你的角色是把流程写清楚
7. 开始实现前先看 §6 有没有这个需求的条目——如果有，参考评估过的风险；如果没有，先在这里加一条再写代码
