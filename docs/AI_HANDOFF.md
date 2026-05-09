# LexRise AI 接手手册

这份文档专门给下一个 AI（或人）接手这个项目时读。
README 面向用户，这份面向开发者/AI——信息密度优先，直接给"当前到哪了、地雷在哪、下一步候选是什么"。

**最近一次更新**：2026-05-09（v0.2 完工后）

---

## 1. 项目一句话描述

`c:/Users/24560/Desktop/study/Englishdemo` 是一个本地离线的 Android 背单词 App（Kotlin + Compose + Room v2 + WorkManager），面向 CET4/CET6/考研。当前能跑、测试全绿、APK 64 MB 可装。

---

## 2. 当前版本：v0.2（已完成）

**词库规模**（全部来自合法开源源头，首启自动导入）：

| 词书 | 词量 | 来源 |
|------|------|------|
| 四级高频 · 词根序 | 3846 | ECDICT (MIT) tag=cet4 |
| 六级进阶 · 词根序 | 5406 | ECDICT tag=cet6 |
| 考研英语 · 大纲词 | 4801 | ECDICT tag=ky |

**学习核心**：

- 词按 **rootKey 聚簇 + 簇内 frq 升序** 出现（不是字母序）。约 21–25% 的词能聚簇，其余按 frq 排
- 翻卡：默认盖住释义，点击才翻；翻出后显示词根 + 含义 + 同根兄弟词 + 派生词 + 用户编辑的巧记
- Dashboard：30 日热力图 + 连续打卡徽章（3/7/30/100 天阶梯）
- SRS：AGAIN/HARD/GOOD/EASY 四档，任何评分都严格推进 interval（v0.1 bug 已修）

**已做的测试 / 构建验证**：

- `./gradlew.bat :app:testDebugUnitTest` — **12 个 test 全绿**
- `./gradlew.bat :app:assembleDebug` — 成功，APK 在 `app/build/outputs/apk/debug/app-debug.apk` ≈ 64 MB

---

## 3. 代码索引（读它在哪）

### 3.1 数据层 `app/src/main/java/com/study/englishdemo/data/`

| 文件 | 职责 |
|------|------|
| `Models.kt` | Room 实体 + 领域模型（`WordEntryEntity` / `WordEntry` / `RootReference`）|
| `Dao.kt` | `WordBookDao / WordEntryDao / WordProgressDao / ReviewLogDao` |
| `AppDatabase.kt` | Room DB，version=2，`fallbackToDestructiveMigration()` |
| `SpacedRepetitionScheduler.kt` | SRS 算法（纯函数，已测） |
| `StudyRepository.kt` | 业务层，调 DAO；`ensureBundledBookImported` 导三本；`getSemanticNeighbors` 拉同根词；`recentReviewCounts(days)` 做热力图数据 |
| `WordBookParser.kt` / `WordBookImporter.kt` | CSV/TXT 解析；新 10 列 + 旧 6 列兼容 |
| `UserPreferencesRepository.kt` | DataStore 设置，实现 `UserPreferencesProvider` 接口（为可测试解耦） |
| `RootReferenceLoader.kt` | 懒加载 `assets/reference/roots.json`，带 `Mutex` |

### 3.2 UI 层 `app/src/main/java/com/study/englishdemo/`

| 文件 | 职责 |
|------|------|
| `MainActivity.kt` | 所有 Compose 页面：Dashboard/Learn/Review/Vocab/Settings + `FlipWordCard` / `ReviewHeatmap` / `StreakBadge` |
| `ui/AppViewModel.kt` | `AppUiState` + 所有 action；`hydrateRootsAndSiblings` 懒加载 |
| `ui/PreviewData.kt` | Compose Preview 样板数据 |
| `ui/Theme.kt` | Material3 配色（未做动态取色） |
| `reminder/ReviewReminderWorker.kt` + `ReviewReminderScheduler.kt` | WorkManager 每日复习提醒 |
| `speech/WordSpeaker.kt` | Android TextToSpeech |
| `AppContainer.kt` | 手写 DI 容器 |
| `EnglishDemoApplication.kt` | Application 入口，首启同步 reminder |

### 3.3 资源 `app/src/main/assets/`

| 文件 | 作用 |
|------|------|
| `books/cet4_core.csv` | CET4 词书（10 列新格式） |
| `books/cet6_core.csv` | CET6 词书 |
| `books/ky_core.csv` | 考研词书 |
| `reference/roots.json` | 1041 词根 → 含义 + 例词 |

### 3.4 构建工具 `tools/`

| 文件 | 作用 |
|------|------|
| `build_wordlists.py` | 从 `raw/ecdict.csv` + `raw/roots_raw.md` 生成上面所有 assets（幂等） |
| `README.md` | 下载源数据的指引 |
| `raw/` | 原始词典（**不入仓**，`.gitignore` 已忽略） |

### 3.5 测试 `app/src/test/java/com/study/englishdemo/data/`

| 文件 | 覆盖 |
|------|------|
| `SpacedRepetitionSchedulerTest.kt` | SRS 4 用例（含 HARD/GOOD 严格推进） |
| `WordBookParserTest.kt` | CSV/TXT 解析 3 用例 |
| `ReviewReminderSchedulerTest.kt` | 提醒时间计算 2 用例 |
| `StudyRepositoryTest.kt` | `recentReviewCounts / merge 去重 / getSemanticNeighbors` 3 用例 |

---

## 4. 关键不变量（改动前必读）

1. **Room schema 现在是 v2**。再次改 `WordEntryEntity` 必须升到 v3 并决定迁移策略；`fallbackToDestructiveMigration` 对用户数据来说是清库，要慎重切换
2. **内置词书不可删/不可改名**：判断 `source == "builtin"`，UI 和 Repository 两层都有守卫。别绕过
3. **positionInBook 决定背词顺序**：CSV 里行的顺序就是学习顺序（已经由 `build_wordlists.py` 排好词根聚簇）。任何"打乱/随机"特性要另起字段，不要直接改 `positionInBook`
4. **mnemonic 是用户本地内容**：不要自动填充或从外部同步覆盖
5. **reviewWord 空安全**：`progressDao.getByWordId` 可能为 null（数据库异常/竞态），保持静默返回而不是 `requireNotNull`
6. **StudyRepository 解耦了接口**：`UserPreferencesProvider` / `BookImporter`。测试都走 Fake，**不要**在测试里直接 new `UserPreferencesRepository`（需要 Context，会让单测变 instrumented test）

---

## 5. 地雷和常见坑

### 5.1 Gradle / 构建

- `local.properties` 的 `sdk.dir` 在 Windows 下**必须**是 `C\:/Users/...` 这种（正斜杠 + `\:` 转义冒号）。`C\\:\\Users\\...` 双反斜杠会被 Gradle 解析成不存在路径，报 `Directory does not exist`
- 内置 Android SDK 在项目 `.android-sdk/`，`.gitignore` 已忽略。换机器要重新下载
- `com.google.android.material:material:1.12.0` 是必须依赖（`Theme.Material3.*` 需要），缺它 `processDebugResources` 会失败
- `./gradlew.bat :app:processDebugResources` 偶发 "Could not move temporary workspace"，重跑即可（Gradle transform workspace 在 Windows 被 AV 临时锁住）
- 不要加 `--no-daemon` 之外的特殊配置；v0.1 时曾看到 daemon 内存不足崩溃留下 `hs_err_pid*.log`，已被清理且 `.gitignore` 覆盖

### 5.2 网络 / 数据源

- GitHub 主域名 `github.com` 在这台机器上**不通**（curl 超时）
- `raw.githubusercontent.com` **通**。所有源数据都从 raw 下载
- Open English WordNet（同义/反义）因体积 + 域名问题暂时取不到。`WordEntryEntity.synonyms/antonyms` 字段已预留，留给 v0.3
- 不要尝试从墨墨/不背单词/百词斩/红宝书/考研闪过抓数据（版权）

### 5.3 Git 状态（⚠️ 非常重要）

- **整个 `C:/Users/24560/` 是一个 git 仓库**（不是 Englishdemo 本身）。在 Englishdemo 里跑 `git ...` 实际操作的是家目录级别的仓库
- 远程 `github` URL **明文包含 GitHub PAT**（`ghp_...`）。任何 `git remote -v` / `git push` / git 相关输出都会泄露这个 token
- **未吊销 token 前，不要 push 任何东西**
- 给 LexRise 独立 git 仓库的计划被用户认可过，但还没做
- 不要帮用户 `git config` 修改 home 仓库设置

### 5.4 UI / Compose

- `androidx.compose.foundation.layout.weight` **不是**顶层 import，不要加 `import ...weight`——它是 `RowScope` 扩展。错误 `Cannot access 'val RowColumnParentData?.weight: Float': it is internal` 就是这个原因
- 翻卡评分按钮在未翻出时必须禁用，这是产品强制"先回忆再看答案"的核心，别移除

---

## 6. 下一步候选（按优先级）

按优先级从高到低，都未开工：

### P0 — 安全和 git 整理

1. 吊销泄露的 `ghp_...` token，清掉 home-level remote 的明文
2. 给 Englishdemo 做独立 git 仓库（`cd Englishdemo && git init` → 加 remote）
3. 确认这份独立仓库发到 GitHub 不会同时上传家目录

### P1 — 补全记忆辅助

1. **同义词/反义词**：等 Open English WordNet 能拉到时，补 `synonyms.json`，灌进 schema 已预留的字段
2. **AI 批量巧记**：用 LLM 离线生成首轮，用户再编辑。不走在线 API，跑本地批处理脚本输出 CSV 再合并
3. **词根学习流**：目前只有"同根词在队列里紧挨着"，可以加一个独立的"词根浏览"入口，点词根看所有例词

### P2 — 学习体验增强

1. **听写模式**：给出释义，让用户在键盘打出英文，与墨墨的听写对位
2. **选择题模式**：混淆项从 `rootRef.examples` 或 `derivatives` 里抽，自然生成难度适中的干扰项
3. **学习计划**：按考试日倒推每日目标量
4. **导出学习报告**：CSV/图片分享

### P3 — 其他

1. Material You 动态取色
2. 支持系统深色模式
3. APK 签名配置（`signingConfigs` 放 `local.properties`，playbook 已写）
4. 真正的 Release 分发（GitHub Release / 蒲公英）
5. 搜索支持拼写容错

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

1. `./gradlew.bat :app:testDebugUnitTest` 全绿
2. `./gradlew.bat :app:assembleDebug` 成功
3. APK 体积 < 80 MB（v0.2 基线 64 MB）
4. 手装 APK 到手机/模拟器，至少跑：
   - 首启 Dashboard 看到三本内置词书
   - 新词页前几张卡是同词根簇（例如 state / statement / status 连着出）
   - 翻卡能看到词根面板和派生词
   - 能编辑巧记并保存
5. `git status`（家目录级别）检查——如果出现 `.claude/` `.ssh/` 等非项目文件 staged，说明做错了

---

## 9. 进度时间线

- **v0.1（2026-05-09 白天）**：翻卡、词汇 Tab、7 日柱状图、词书删改、SRS 与 streak 修复，12 词 → 390 词
- **v0.2（2026-05-09 傍晚）**：合法词库扩容至 14000 词、词根聚簇、词根面板、30 日热力图、徽章、巧记编辑
- **v0.3（待做）**：同义反义词、AI 巧记、学习计划、多学习模式
