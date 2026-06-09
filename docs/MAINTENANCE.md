# LexRise 维护手册

面向"过几周回来改点东西"的自己或下一个维护者。
不讲架构（那是 `AI_HANDOFF.md` 的职责），只讲**动手做事的步骤**。

---

## 1. 日常开发循环

```bash
cd c:/Users/24560/Desktop/study/Englishdemo

# 跑单测（每次小改完就跑，<60s）
./gradlew.bat :app:testDebugUnitTest

# 如果改了 tools/build_wordlists.py 或 mnemonic seed/raw 规则
python -m unittest discover -s tools -p "test_*.py"

# 构建可装手机的 APK（~4 min 首次，~30s 增量）
./gradlew.bat :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

"改完跑一下这两条" 是雷打不动的。`assembleDebug` 过 + 测试绿 ≈ 能装能跑。

### 如果 Gradle 卡住或缓存坏了

```bash
# 清构建产物（不动 .android-sdk 和 tools/raw）
./gradlew.bat clean

# 某个 task 报 "Could not move temporary workspace"（Windows 杀软偶发）
# 直接重跑原命令即可

# Daemon 疑似状态异常
./gradlew.bat --stop
```

一般不要整目录删 `.android-sdk/`——它是当前项目的本地 Android SDK。空间紧张时只能按需裁剪明确不用的组件，例如只构建 APK 时可以移除 `.android-sdk/system-images/`、`.android-sdk/emulator/` 和未被 Gradle 实际使用的旧/新 build-tools；但要保留 `platforms/android-35`、`build-tools/34.0.0`、`cmdline-tools/`、`platform-tools/` 和 `licenses/`。`.gradle/` 可删但会触发依赖重新下载，通常只在缓存异常或用户明确要求清理空间时做。

### 仓库轻量化规则

- 测试源码不要删：`app/src/test/` 和 `tools/test_build_wordlists.py` 是后续优化的回归保护。
- `docs/release-notes-v0.*-draft.md` 不长期保留；发布信息归入 `README.md` / `AI_HANDOFF.md` / `MAINTENANCE.md`，只有正在准备正式发布时才临时保留草稿。
- 构建产物和缓存可删：`app/build/`、根目录 `build/`、`.gradle/`、`.kotlin/`、`tools/__pycache__/`。
- 原始词库 `tools/raw/` 不入仓，但用于重建内置词书；除非确认短期不再改词库，否则不要为了几十 MB 删除它。

---

## 2. 改词库内容

**场景 A：发现某个词的翻译/例句错了**

最快路径：直接改 `app/src/main/assets/books/*.csv` 的对应行。

- CSV 11 列格式：`term, phonetic, definition, translation, example, tags, rootKey, derivatives, frq, pos, mnemonic`
- 保持 UTF-8 无 BOM
- 改完跑 `./gradlew.bat :app:assembleDebug`，重装 APK 后**首启会清库重建**（因为 Room `fallbackToDestructiveMigration`，内置词书的 builtin source 会重新导入）

**场景 B：扩词 / 换词源 / 调聚簇规则**

改脚本：`tools/build_wordlists.py`，然后：

```bash
# 准备原始词典（raw.githubusercontent.com 在国内可以通，github.com 不通）
mkdir -p tools/raw
curl -fL -o tools/raw/ecdict.csv \
  https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv
curl -fL -o tools/raw/roots_raw.md \
  https://raw.githubusercontent.com/WithEnglishWeCan/generated-english-roots-list/master/README.md

# 重建词库资源（幂等）
python tools/build_wordlists.py
python -m unittest discover -s tools -p "test_*.py"

# 再构建 APK
./gradlew.bat :app:assembleDebug
```

**场景 C：离线批量补巧记**

最小格式：

```csv
term,mnemonic
describe,de + scrib：把看到的写下来就是 describe 描述
```

- 少量可复现 seed：改 `tools/mnemonics_seed.csv`（入仓）
- 大批量离线生成稿：放 `tools/raw/mnemonics.csv`（不入仓，且会覆盖 seed 同名 term）
- 然后运行 `python tools/build_wordlists.py` 重建三本内置词书
- 不要在脚本里调用在线 API，也不要在 App 启动时自动覆盖用户已经编辑过的 `mnemonic`

**警告**：不要手动改 `positionInBook`，它由脚本按聚簇顺序生成，人为打乱会破坏"同根词连着出现"的学习节奏。

**警告**：不要从墨墨、百词斩、红宝书、考研闪过抓数据。版权坑。本仓库只用 MIT 的 ECDICT 和开源词根表。

---

## 3. 改词根映射

词根数据在 `app/src/main/assets/reference/roots.json`，由 `tools/build_wordlists.py` 的 `parse_roots_md` 从 `tools/raw/roots_raw.md` 生成。

如果你要**增一个词根或改一个例词映射**：

1. 改 `tools/build_wordlists.py` 里的 `example_to_root` 精确映射（优先），不要加太短的前缀匹配（3 字母以下会造成误聚）
2. `python tools/build_wordlists.py` 重建
3. 验证 `MorphologyHelpersTest.decomposeWord_*` 还绿

聚簇率指标：当前约 21–25%。提到 30% 以上通常意味着误聚增多，要审查噪声前缀。

---

## 4. 改 Room 数据库（高危）

现在 schema version = 2。**任何改 `WordEntryEntity` / `WordBookEntity` / `WordProgressEntity` / `ReviewLogEntity` 的行为都需要升版本号**——包括：加列、删列、改列名、改列类型、加/删索引。

### 能不能避开？能避开就避开

很多"新功能"其实不需要加列。本项目 v0.3-v0.36 的所有新能力（簇首、词根覆盖、词根图谱简报、根族巧记补给、词根详情导读、根族路线、难词专攻、难词处方、错题战情台、选择题干扰池、完形题、完形语境导读、派生词挖空、完形候选排序、完形多句例句质量排序、搜索拼写容错、derivatives 搜索、词形命中展示、词汇检索洞察、空结果救援、结果分诊、练习模式阶梯、本轮练习统计、本轮教练、本轮收口建议、复习队列预案、七日节奏简报、首页学习焦点、今日负载简报、今日训练路线、新词记忆锚、新词批次策略、新词巧记覆盖简报、新词闭环计划、离线巧记 seed 注入）都走**派生查询、纯函数派生、构建期资源生成或 UI session 状态**——DAO 里新加 `@Query`、在 Repository 里拼题、在构建脚本里生成资源，或在 ViewModel 层维护临时状态，而不改 Entity。看一眼这些例子再决定是不是真要动 schema：

- 想标记"这个词是簇首"？→ 已经有 `getAnchorWordIds(bookId)` 动态算，不加 `isAnchor` 列
- 想统计"哪个词翻车最多"？→ `getToughWordsForBook` 从 `review_logs` GROUP BY 出来
- 想让错题本告诉用户“怎么修这个词”？→ `buildToughWordPrescription` 从 `ToughWord` + 词条线索派生处方，不写 `review_logs`
- 想汇总这批错题先怎么打？→ `buildToughWordsBrief` 从 `ToughWord` 列表和处方派生战情台，不写 `review_logs`
- 想知道"这本书词根覆盖率"？→ `getBookRootSnapshot` 实时聚合
- 想先解释整本词根图谱该怎么推进？→ `buildRootAtlasBrief` 从 `RootGroup` + `BookRootSnapshot` 派生词根图谱简报，不写设置、不改 root 列表排序
- 想知道该先给哪些根族补巧记？→ `buildRootMnemonicBrief` 从 `RootGroup.members.mnemonic` 派生根族巧记补给，不写进度、不自动生成 mnemonic、不改 root 列表排序
- 想让词根详情卡提示怎么复盘当前词？→ `buildRootWordGuide` 从 `WordEntry` + root meanings + family terms 派生词根详情导读，不写进度、不改评分
- 想让词根 Tab 告诉用户“下一步看哪组词”？→ `buildRootGroupInsight` 从 `RootGroup` 触达比例和成员 phase 派生根族路线，不加列
- 想加新练习题型？→ 优先用 `PracticeMode` + Repository 构题 + 纯函数，评分仍回 `reviewWord`
- 想解释当前练习模式该怎么用、何时升降档？→ `buildPracticeModeBrief` 从 `PracticeMode` + `PracticeSessionStats` + 剩余到期词数派生练习模式阶梯，不改模式切换、不写评分
- 想让完形题支持派生词、优化挖空优先级或筛掉多句例句里的噪声句？→ `buildClozeBlank` 从已有 `example/derivatives` 字段派生候选句和候选词形并排序，不加表
- 想让完形题先提示用户该抓什么线索？→ `buildClozeContextGuide` 从 `ClozeQuestion` 派生完形语境导读，不改构题、不泄露答案、不写评分
- 想让词汇搜索支持复数/时态/派生形，或解释“为什么命中这个词”？→ `searchInBook` 查已有 `derivatives` 列，`searchWords` 用 `fuzzyWordFormMatchDistance` 做 fallback，UI 用 `matchingWordForms` 展示命中词形，不加表
- 想在结果列表前解释“这次搜索质量如何”？→ `buildVocabularySearchInsight` 从当前 query 和结果列表派生检索洞察，不改 DAO、不改排序、不写搜索日志
- 想让空结果告诉用户怎么改查？→ `buildVocabularySearchRescuePlan` 从 query、结果数和阶段筛选派生救援路线，不改 DAO、不自动改 query、不改阶段筛选
- 想让有结果的词汇搜索告诉用户先怎么看？→ `buildVocabularyResultTriage` 从 query、结果列表和阶段筛选派生分诊路线，不改 DAO、不改排序、不自动切筛选
- 想统计"这轮 Review 做得怎么样"？→ `PracticeSessionStats` 是 UI session 状态，`recordPracticeAttempt` 纯函数计数，不写 Room
- 想根据本轮表现提示“该降速还是加难”？→ `buildPracticeSessionCoach` 从 `PracticeSessionStats` + `PracticeMode` 派生本轮教练，不写设置、不写 Room
- 想判断这轮该继续、修错、加难还是收口？→ `buildReviewExitBrief` 从 `PracticeSessionStats` + `PracticeMode` + 剩余到期词数派生本轮收口建议，不写设置、不改队列、不改评分
- 想在本轮复习开始前提示“先怎么清这批到期词”？→ `buildReviewQueueBrief` 从 `dueReviewWords` + `PracticeMode` 派生复习队列预案，不改复习顺序、不写评分、不写 Room
- 想解释最近一周复习节奏？→ `buildStudyRhythmBrief` 从 `recentReviewCounts` + `DailyOverview` 派生七日节奏简报，不写复习日志、不改热力图数据
- 想提示"今天先做什么"并跳到对应页面？→ `buildStudyFocusCue` 从 session / rootSnapshot / pace 派生建议和按钮文案；`MainActivity` 只做本地 Tab 切换，不写设置、不写 Room
- 想先判断今天压力主要来自哪里？→ `buildDailyLoadBrief` 从 session / rootSnapshot / pace / toughWords 数量派生今日负载简报，不写设置、不改自动配速、不改路线排序
- 想把今天拆成多步路线？→ `buildDailyStudyRoute` 从 session / rootSnapshot / pace / toughWords 数量派生 1–3 个行动步骤；`MainActivity` 只负责跳 Tab，不写设置、不写 Room
- 想让新词卡先提示“抓什么线索”？→ `buildWordMemoryAnchor` 从词条、词根引用和同根词数量派生记忆锚；UI 只展示，不覆盖用户 `mnemonic`
- 想让一批新词先给整体打法？→ `buildWordBatchBrief` 从当前 `recommendedNewWords` 派生词根/词形/语境/混合策略，不改学习顺序、不写队列表
- 想让新词批次解释巧记覆盖？→ `buildMnemonicBatchBrief` 从当前 `recommendedNewWords.mnemonic/rootKey/derivatives` 派生覆盖简报；UI 只展示，不自动补写 mnemonic
- 想让新词批次告诉用户怎么完成一轮闭环？→ `buildLearningLoopBrief` 从当前 `recommendedNewWords` 派生预读、自测、评分入轨三步，不改新词顺序、不写队列、不改 SRS
- 想批量补默认巧记？→ 改 `tools/mnemonics_seed.csv` 或 `tools/raw/mnemonics.csv`，用 `tools/build_wordlists.py` 写入内置 CSV 的 `mnemonic` 列；不要改 Room，不要启动时覆盖用户编辑

### 如果确实要升级 schema

当前 `AppDatabase.create` 用的是 `fallbackToDestructiveMigration()`——**这会清空用户所有学习进度**。生产环境下不能这么干。操作手顺：

1. 改 Entity
2. `AppDatabase.kt` 里 `@Database(version = 3, ...)`
3. 写一个 `Migration(2, 3)` 对象：

```kotlin
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE word_entries ADD COLUMN newField TEXT NOT NULL DEFAULT ''")
    }
}
```

4. 挂到 builder：

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "lexrise.db")
    .addMigrations(MIGRATION_2_3)
    // 注意：保留 fallbackToDestructiveMigration 作为最终兜底，或彻底移除
    .build()
```

5. 把 `exportSchema = true` 打开，把 `app/schemas/` 目录提交，这样 Room 会在每次迁移生成 JSON 对比文件
6. 补测试：用**降级的** `MigrationTestHelper`（instrumented test）验证旧数据在迁移后还读得到

跳过步骤 3-6 的"简单加一列"都是未来崩溃的时间炸弹。

---

## 5. 升级依赖

依赖清单在 `app/build.gradle.kts`。升级前先记录一下现状：

```bash
./gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath > /tmp/deps-before.txt
```

### 安全升级（次要版本号变化）

`androidx.compose.ui:1.7.8` → `1.7.x` 基本无痛。升完跑 `:app:testDebugUnitTest` + `:app:assembleDebug`。

### 小心升级（主版本号变化）

- `material3 1.3.x → 2.x`：API 有破坏，`DatePicker` / `DatePickerDialog` / `CircularProgressIndicator(progress = ...)` 几个的签名都可能动。先翻 release notes
- `room 2.6.x → 2.7+`：可能要改 KSP 版本、Schema 导出机制
- `compose 1.7 → 1.8 / 2.0`：关注 Modifier API 和 rememberSaveable 变化
- Kotlin 升级：要同步升 compose-compiler 和 KSP 的对应版本矩阵

每次主版本跨越，单独一个分支（哪怕项目还没 push 到 remote，用本地 git branch 隔离也行）。

### 不要动的

- `com.google.android.material:material:1.12.0`：`Theme.Material3.*` 需要，删掉构建立即炸
- `junit:junit:4.13.2`：单测已经全部写在 JUnit4 风格上，切 JUnit5 需要全部改 `@Test` import

---

## 6. 发版流程（打 tag + APK）

**前置**：确认项目仓库 remote 指向 `https://github.com/2456018331lby-dev/LexRise.git`，且不要复用家目录仓库里那个泄露过 `ghp_...` PAT 的 `github` remote。

```bash
cd c:/Users/24560/Desktop/study/Englishdemo

# 1. 确认所有测试绿、构建干净
./gradlew.bat :app:testDebugUnitTest :app:assembleDebug

# 2. 改 app/build.gradle.kts 里的 versionCode 和 versionName
# versionCode 是整数（单调递增），versionName 是字符串（如 "0.4.0"）

# 3. 改完提交
git add app/build.gradle.kts
git commit -m "chore: bump version to 0.4.0"

# 4. 打 tag
git tag -a v0.4.0 -m "v0.4.0"
git push origin main --tags

# 5. 构建 release APK（需要先按 docs/github-release-playbook.md 配签名）
./gradlew.bat :app:assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk

# 6. gh CLI 创建 release（或网页操作）
gh release create v0.4.0 \
  app/build/outputs/apk/release/app-release.apk \
  --title "LexRise v0.4.0" \
  --notes-file RELEASE_NOTES.md
```

签名步骤详见 `docs/github-release-playbook.md` 第 2.5 节。

---

## 7. 常见问题排查

### 症状 1：APK 装上手机后空白或立刻 crash

1. `adb logcat | grep -i "englishdemo\|androidruntime"` 看堆栈
2. 最常见的是 Room migration 相关的 `IllegalStateException`——检查是不是改了 Entity 没升 version
3. 或者 `@Composable` 里访问 null uiState——检查 PreviewData 是否覆盖了新字段

### 症状 2：构建通过但某屏空白

- 通常是 ViewModel 里 `viewModelScope.launch { ... }` 抛异常被吞。在 `AppViewModel` 的 action 开头临时加 `println` 或把 `runCatching {...}` 改成裸调查看
- Compose 侧 `AnimatedVisibility` 的 visible 永远 false？检查上游 state 是不是在协程完成前就 emit 了空数据

### 症状 3：`local.properties` 里 `sdk.dir` 写对了但 Gradle 报 `Directory does not exist`

Windows 下**必须**是正斜杠 + `\:` 转义冒号：

```properties
sdk.dir=C\:/Users/24560/Desktop/study/Englishdemo/.android-sdk
```

**错误写法**：`C:\Users\...` / `C:/Users/...`（不转义冒号）/ `C\\:\\Users\\...`（双反斜杠会被 Gradle 解成不存在路径）。

### 症状 4：`processDebugResources` 失败 `Could not move temporary workspace`

Windows 杀软或文件索引锁住了 Gradle 的 transform 目录。**重跑一次**一般就好。持续出现则临时关杀软的实时扫描。

### 症状 5：词库 CSV 导入失败或乱码

- CSV 必须 UTF-8 无 BOM。Excel 保存出来的默认带 BOM，要用 VS Code / Sublime / PowerShell `Out-File -Encoding utf8NoBOM` 重存
- 不要含 ASCII 以外的引号（全角 `“”`）——只认 ASCII 双引号
- 测试解析：单独跑 `WordBookParserTest`（里面覆盖 CSV 11 列 mnemonic + 旧 6/10 列兼容 + TXT 三种分隔符）

### 症状 6：单测 `org.junit.runners.model.InvalidTestClassError: Method X() should be void`

Kotlin `runBlocking { ... assertThat(...).containsExactly(...) }` 的最后一行表达式如果返回非 Unit（`containsExactly` 返回 `Ordered`），JUnit4 就会报这个。解决：改用 `.isEqualTo(...)` 或在块末尾加 `Unit`。

---

## 8. Git 维护

### 日常提交

- **不要**在家目录 (`C:/Users/24560/`) 里 `git add`——那是另一个仓库
- 只在 `Englishdemo/` 目录下跑 git 命令
- 想确认自己在对的仓库：`git remote -v` 应指向 LexRise 的 GitHub repo，而不是家目录的 `github` remote

### 安全清单（push 前）

```bash
# 1. 不能有 local.properties（含 SDK 路径 + 未来的签名密码）
git ls-files | grep local.properties   # 应为空

# 2. 不能有 *.jks / *.keystore
git ls-files | grep -E '\.(jks|keystore)$'   # 应为空

# 3. 不能有 tools/raw/ecdict.csv（63MB，且跟 license 有关要另论）
git ls-files tools/raw/   # 应为空

# 4. 不能有 .android-sdk/ 或 build/ 产物
git ls-files .android-sdk/ app/build/   # 应为空
```

以上任一不空 = `.gitignore` 有漏网，立即补齐。

### 永远不该做

- 家目录里 `git add .`（会扫 `.ssh/` `.claude/` 等一堆隐私目录）
- 无说明地 `git push --force` 到 main（当前 GitHub main 是 API 快照；如需对齐本地完整历史，先写清楚原因和回滚路径）
- 用 GitHub Git Data API 发布快照时直接读取工作区文本文件；应从 `git cat-file blob <sha>` 读取 Git 对象内容，避免 Windows 换行让远端 blob SHA 偏离本地提交
- 在未 revoke 泄露 PAT 前复用家目录仓库的 `github` remote

---

## 9. 关键文件速查

| 要做什么 | 改哪里 |
|---------|-------|
| 改 UI 文案/颜色 | `MainActivity.kt`（Compose 全部在这里）+ `ui/Theme.kt`（色盘） |
| 改学习算法（SRS） | `data/SpacedRepetitionScheduler.kt` + 对应 Test |
| 改导入格式 | `data/WordBookParser.kt` + `WordBookParserTest` |
| 改导入幂等/合并逻辑 | `data/StudyRepository.kt#importMutex/importBook/ensureBundledBookImported` + `StudyRepositoryTest` |
| 改每日复习提醒 | `reminder/ReviewReminderScheduler.kt` + `ReviewReminderSchedulerTest` |
| 改发音 | `speech/WordSpeaker.kt`（当前是 Android TTS，改成音频文件要加 raw 资源） |
| 改考试计划/自动配速 | `data/MorphologyHelpers.kt#computePaceRecommendation` + `MorphologyHelpersTest` |
| 改首页学习焦点/行动按钮 | `data/MorphologyHelpers.kt#buildStudyFocusCue` + `data/Models.kt#StudyFocusCue` + `MainActivity.kt#StudyFocusCueCard` + `MorphologyHelpersTest` |
| 改首页今日负载简报 | `data/MorphologyHelpers.kt#buildDailyLoadBrief` + `data/Models.kt#DailyLoadBrief/DailyLoadBriefKind/DailyLoadLane` + `MainActivity.kt#DailyLoadBriefCard` + `MorphologyHelpersTest` |
| 改首页今日训练路线 | `data/MorphologyHelpers.kt#buildDailyStudyRoute` + `data/Models.kt#DailyStudyRoute/DailyStudyRouteStep/DailyStudyRouteTarget` + `MainActivity.kt#DailyStudyRouteCard/DailyStudyRouteStepCard` + `MorphologyHelpersTest` |
| 改首页七日节奏简报 | `data/MorphologyHelpers.kt#buildStudyRhythmBrief` + `data/Models.kt#StudyRhythmBrief/StudyRhythmBriefKind` + `MainActivity.kt#StudyRhythmBriefCard/StudyRhythmMetric` + `MorphologyHelpersTest` |
| 改新词卡记忆锚 | `data/MorphologyHelpers.kt#buildWordMemoryAnchor` + `data/Models.kt#WordMemoryAnchor/WordMemoryAnchorKind` + `MainActivity.kt#WordMemoryAnchorPanel/MemoryAnchorMetric` + `MorphologyHelpersTest` |
| 改新词批次策略 | `data/MorphologyHelpers.kt#buildWordBatchBrief` + `data/Models.kt#WordBatchBrief/WordBatchBriefKind` + `MainActivity.kt#WordBatchBriefCard/BatchBriefMetric` + `MorphologyHelpersTest` |
| 改新词巧记覆盖简报 | `data/MorphologyHelpers.kt#buildMnemonicBatchBrief` + `data/Models.kt#MnemonicBatchBrief/MnemonicBatchBriefKind` + `MainActivity.kt#MnemonicBatchBriefCard` + `MorphologyHelpersTest` |
| 改词根图谱简报 | `data/MorphologyHelpers.kt#buildRootAtlasBrief` + `data/Models.kt#RootAtlasBrief/RootAtlasBriefKind` + `MainActivity.kt#RootAtlasBriefCard/RootAtlasMetric` + `MorphologyHelpersTest` |
| 改根族巧记补给 | `data/MorphologyHelpers.kt#buildRootMnemonicBrief` + `data/Models.kt#RootMnemonicBrief/RootMnemonicBriefKind` + `MainActivity.kt#RootMnemonicBriefCard` + `MorphologyHelpersTest` |
| 改词根详情导读 | `data/MorphologyHelpers.kt#buildRootWordGuide` + `data/Models.kt#RootWordGuide/RootWordGuideKind` + `MainActivity.kt#RootWordGuidePanel/RootWordPreviewSheet` + `MorphologyHelpersTest` |
| 改新词闭环计划 | `data/Models.kt#LearningLoopBrief/LearningLoopStep` + `data/MorphologyHelpers.kt#buildLearningLoopBrief` + `MainActivity.kt#LearningLoopBriefCard/WordQueueScreen` + `MorphologyHelpersTest` |
| 改词根 Tab 根族路线 | `data/MorphologyHelpers.kt#buildRootGroupInsight` + `data/Models.kt#RootGroupInsight/RootGroupStage` + `MainActivity.kt#RootGroupCard/RootStageBadge/RootFocusTermChip` + `MorphologyHelpersTest` |
| 改选择题干扰项逻辑 | `data/MorphologyHelpers.kt#buildQuizOptions` + `MorphologyHelpersTest` |
| 改完形/例句挖空 | `data/MorphologyHelpers.kt#buildClozeBlank/blankTermInExample` + `StudyRepository.kt#buildClozeQuestion` + 对应 Test；多句例句先按质量排序，单句内保持原词优先、派生词按例句位置优先 |
| 改完形语境导读 | `data/MorphologyHelpers.kt#buildClozeContextGuide` + `data/Models.kt#ClozeContextGuide/ClozeContextGuideKind` + `MainActivity.kt#ClozeContextGuidePanel/ClozePracticePager` + `MorphologyHelpersTest` |
| 改词汇搜索/拼写容错 | `data/Dao.kt#searchInBook` + `data/MorphologyHelpers.kt#fuzzyTermMatchDistance/fuzzyWordFormMatchDistance/matchingWordForms` + `StudyRepository.kt#searchWords` + `MainActivity.kt#VocabularyRow/VocabularyMatchRail` + 对应 Test |
| 改本轮练习统计 | `data/Models.kt#PracticeSessionStats` + `data/MorphologyHelpers.kt#recordPracticeAttempt` + `ui/AppViewModel.kt#reviewPracticeWord` + `MainActivity.kt#PracticeSessionStatsCard` |
| 改练习模式阶梯 | `data/Models.kt#PracticeModeBrief/PracticeModeLadderStep` + `data/MorphologyHelpers.kt#buildPracticeModeBrief` + `MainActivity.kt#PracticeModeBriefCard/ReviewScreen` + `MorphologyHelpersTest` |
| 改本轮练习教练 | `data/Models.kt#PracticeSessionCoach/PracticeSessionCoachKind` + `data/MorphologyHelpers.kt#buildPracticeSessionCoach` + `MainActivity.kt#PracticeCoachPanel/PracticeSessionStatsCard` + `MorphologyHelpersTest` |
| 改本轮收口建议 | `data/Models.kt#ReviewExitBrief/ReviewExitBriefKind` + `data/MorphologyHelpers.kt#buildReviewExitBrief` + `MainActivity.kt#ReviewExitBriefCard/ReviewScreen` + `MorphologyHelpersTest` |
| 改复习队列预案 | `data/Models.kt#ReviewQueueBrief/ReviewQueueBriefKind` + `data/MorphologyHelpers.kt#buildReviewQueueBrief` + `MainActivity.kt#ReviewQueueBriefCard/ReviewQueueMetric` + `MorphologyHelpersTest` |
| 改难词错题处方 | `data/Models.kt#ToughWordPrescription/ToughWordPrescriptionKind` + `data/MorphologyHelpers.kt#buildToughWordPrescription` + `MainActivity.kt#ToughWordCard/ToughPrescriptionPanel` + `MorphologyHelpersTest` |
| 改难词错题战情台 | `data/Models.kt#ToughWordsBrief` + `data/MorphologyHelpers.kt#buildToughWordsBrief` + `MainActivity.kt#ToughWordsBriefCard/ToughBriefMetric` + `MorphologyHelpersTest` |
| 改词汇检索洞察 | `data/Models.kt#VocabularySearchInsight` + `data/MorphologyHelpers.kt#buildVocabularySearchInsight` + `MainActivity.kt#VocabularySearchInsightCard/VocabularyInsightMetric` + `MorphologyHelpersTest` |
| 改词汇空结果救援 | `data/Models.kt#VocabularySearchRescuePlan/VocabularySearchRescueStep` + `data/MorphologyHelpers.kt#buildVocabularySearchRescuePlan` + `MainActivity.kt#VocabularySearchRescueCard/VocabularyScreen` + `MorphologyHelpersTest` |
| 改词汇结果分诊 | `data/Models.kt#VocabularyResultTriage/VocabularyResultLane` + `data/MorphologyHelpers.kt#buildVocabularyResultTriage` + `MainActivity.kt#VocabularyResultTriageCard/VocabularyScreen` + `MorphologyHelpersTest` |
| 改拼写判分 | `data/MorphologyHelpers.kt#normalizedEditDistance` + `ratingFromEditDistance` |
| 加新复习模式 | `data/Models.kt#PracticeMode` 枚举 + `MainActivity.kt#ReviewScreen` when 分支 |
| 加新底栏 Tab | `MainActivity.kt#HomeTab` 枚举 + NavigationBar item + when 分支 |

---

## 10. 术语对照（方便搜代码）

| 中文 | 代码里 | 说明 |
|------|-------|------|
| 词书 | WordBook / word_books | 一本完整词汇集合，如"CET4 核心" |
| 词条 | WordEntry / word_entries | 单词本体 |
| 进度 | WordProgress / word_progress | 某词的 SRS 状态 |
| 复习记录 | ReviewLog / review_logs | 每次评分的事件日志（难词专攻的数据源） |
| 词根 | rootKey / RootReference | 如 "scrib"、"stat"、"spec" |
| 簇首 / 地基词 | anchor | 同词根中 frq 最低的那个 |
| 聚簇 | clustered | `rootKey != ''` 的词 |
| 配速 | pace | 按考试日期算出的每日新词目标 |
| 难词 | tough / ToughWord | AGAIN 次数 > 0 的词 |
| 拆词 | decompose / MorphemeSegment | 前缀/词根/后缀三色分段 |
