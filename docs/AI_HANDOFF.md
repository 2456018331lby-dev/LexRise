# LexRise AI 接手手册

这份文档专门给下一个 AI（或人）接手这个项目时读。
README 面向用户，这份面向开发者/AI——信息密度优先，直接给"当前到哪了、地雷在哪、下一步候选是什么"。

**最近一次更新**：2026-05-09（v0.3 完工后）

---

## 1. 项目一句话描述

`c:/Users/24560/Desktop/study/Englishdemo` 是一个本地离线的 Android 背单词 App（Kotlin + Compose + Material3 dynamicColor + Room v2 + WorkManager），面向 CET4/CET6/考研。当前能跑、23 个单测全绿、APK 67 MB 可装。

**本项目现在有自己的 git 仓库**（v0.3 起）：在 `Englishdemo/.git/`，分支 `main`。家目录 `C:/Users/24560/` 仍然是一个单独的 git 仓库，但两者独立；在 Englishdemo 里跑 `git` 命令只影响项目仓库。

---

## 2. 当前版本：v0.3（已完成）

**词库规模**（全部来自合法开源源头，首启自动导入）：

| 词书 | 词量 | 来源 |
|------|------|------|
| 四级高频 · 词根序 | 3846 | ECDICT (MIT) tag=cet4 |
| 六级进阶 · 词根序 | 5406 | ECDICT tag=cet6 |
| 考研英语 · 大纲词 | 4801 | ECDICT tag=ky |

**学习核心**：

- 词按 **rootKey 聚簇 + 簇内 frq 升序** 出现（不是字母序）。约 21–25% 的词能聚簇，其余按 frq 排
- 翻卡：默认盖住释义，点击才翻；**始终显示拆词分段**（前缀/词根/后缀三色），翻出后显示词根定义 + 同根兄弟词（带阶段色点）+ 派生词 + 用户编辑的巧记
- **簇首词**（同根中 frq 最低的那个）顶上有『地基词』徽章——鼓励用户先背地基再展开
- **词根 Tab**：按根聚合整本词书，每根一个进度环、成员词 chip 列表（带阶段色点）
- Dashboard：考试倒计时卡 + 词根覆盖卡 + 30 日热力图 + 连续打卡徽章（3/7/30/100 天阶梯）
- **考试日期 + 自动配速**：设置考试日期后开启自动配速，按剩余词量 / 剩余天数 + 20% 缓冲算每日目标（5~40 词/天内 clamp）
- SRS：AGAIN/HARD/GOOD/EASY 四档，任何评分都严格推进 interval

**已做的测试 / 构建验证**：

- `./gradlew.bat :app:testDebugUnitTest` — **23 个 test 全绿**（v0.2 为 12 个）
- `./gradlew.bat :app:assembleDebug` — 成功，APK 在 `app/build/outputs/apk/debug/app-debug.apk` ≈ 67 MB

---

## 3. 代码索引（读它在哪）

### 3.1 数据层 `app/src/main/java/com/study/englishdemo/data/`

| 文件 | 职责 |
|------|------|
| `Models.kt` | Room 实体 + 领域模型（`WordEntryEntity` / `WordEntry` / `RootReference`）；v0.3 增：`SettingsState.examDate / autoPaceEnabled`、`RootGroup`、`BookRootSnapshot`、`PaceRecommendation`、`MorphemeSegment` |
| `Dao.kt` | `WordBookDao / WordEntryDao / WordProgressDao / ReviewLogDao`；v0.3 增：`getClusteredEntries / countClusteredForBook / getByWordIds / countLearnedForBook` |
| `AppDatabase.kt` | Room DB，version=2，`fallbackToDestructiveMigration()` |
| `SpacedRepetitionScheduler.kt` | SRS 算法（纯函数，已测） |
| `MorphologyHelpers.kt` | v0.3 新增：`decomposeWord`（拆前缀/词根/后缀）+ `computePaceRecommendation`（配速计算），纯函数，8 个单测 |
| `StudyRepository.kt` | 业务层；v0.3 增：`getRootGroups / getBookRootSnapshot / getAnchorWordIds / getPaceRecommendation / updateExamPlan` |
| `WordBookParser.kt` / `WordBookImporter.kt` | CSV/TXT 解析；新 10 列 + 旧 6 列兼容 |
| `UserPreferencesRepository.kt` | DataStore 设置；v0.3 增：`examDate`（ISO string）、`autoPaceEnabled` |
| `RootReferenceLoader.kt` | 懒加载 `assets/reference/roots.json`，带 `Mutex` |

### 3.2 UI 层 `app/src/main/java/com/study/englishdemo/`

| 文件 | 职责 |
|------|------|
| `MainActivity.kt` | 所有 Compose 页面：Dashboard/Learn/Review/**Roots**/Vocab/Settings + `FlipWordCard`（含 `MorphemeRow` 拆词、`AnchorBadge` 地基词）/ `ReviewHeatmap` / `StreakBadge` / `ExamCountdownCard` / `RootCoverageCard` / `ExamPlanCard`（带 DatePicker）/ `RootsScreen` + `RootGroupCard` / `SiblingChip`（带阶段色点） |
| `ui/AppViewModel.kt` | `AppUiState` + 所有 action；v0.3 增：`anchorWordIds / rootSnapshot / pace / rootGroups / rootsQuery`、`updateExamPlan / updateRootsQuery / loadRootGroups` |
| `ui/PreviewData.kt` | Compose Preview 样板数据 |
| `ui/Theme.kt` | Material 3 配色；v0.3 升级：dynamicLight/Dark (Android 12+)，低版本 fallback 到 LexRise 品牌色 |
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
| `StudyRepositoryTest.kt` | `recentReviewCounts / merge 去重 / getSemanticNeighbors / getRootGroups / getAnchorWordIds / getPaceRecommendation` 6 用例 |
| `MorphologyHelpersTest.kt` | `decomposeWord` 4 用例 + `computePaceRecommendation` 4 用例 |

---

## 4. 关键不变量（改动前必读）

1. **Room schema 现在是 v2**。v0.3 新增功能刻意走派生查询（`getClusteredEntries / getAnchorWordIds`）而不加列，避免 destructive migration 清掉 v0.2 用户数据。再次改 `WordEntryEntity` 必须升到 v3 并决定迁移策略
2. **内置词书不可删/不可改名**：判断 `source == "builtin"`，UI 和 Repository 两层都有守卫。别绕过
3. **positionInBook 决定背词顺序**：CSV 里行的顺序就是学习顺序（已经由 `build_wordlists.py` 排好词根聚簇）。任何"打乱/随机"特性要另起字段，不要直接改 `positionInBook`
4. **mnemonic 是用户本地内容**：不要自动填充或从外部同步覆盖
5. **reviewWord 空安全**：`progressDao.getByWordId` 可能为 null（数据库异常/竞态），保持静默返回而不是 `requireNotNull`
6. **StudyRepository 解耦了接口**：`UserPreferencesProvider` / `BookImporter`。测试都走 Fake，**不要**在测试里直接 new `UserPreferencesRepository`（需要 Context，会让单测变 instrumented test）
7. **自动配速是"覆盖"不是"替换"基线**：用户的手动基线（`dailyNewWordTarget`）始终保留在 DataStore 里；autoPace 开启时 `DailyOverview.newWordTarget` 用计算值，关闭即回到基线。不要在自动配速触发时写回基线
8. **decomposeWord 永不抛异常**：找不到 rootKey 返回单个 PLAIN 段。UI 只要在 `segments.size > 1` 时渲染拆词行，零维护

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
- 这个独立仓库**没有配置 remote**——刻意的，因为家目录仓库的 `github` remote URL 中的 PAT 还没被用户吊销
- **未吊销泄露的 `ghp_...` 前不要帮用户加 remote、也不要 `git push`**
- 家目录 `C:/Users/24560/` 仍是一个 git 仓库，它现在把 Englishdemo 视为嵌套 git 目录（`git status` 里显示为 `??`），不会扫进去
- 给 LexRise 发到 GitHub 的合法路径：`gh auth login` 浏览器流 or SSH keypair，任一即可，都避开 PAT

### 5.4 UI / Compose

- `androidx.compose.foundation.layout.weight` **不是**顶层 import，不要加 `import ...weight`——它是 `RowScope` 扩展。错误 `Cannot access 'val RowColumnParentData?.weight: Float': it is internal` 就是这个原因
- 翻卡评分按钮在未翻出时必须禁用，这是产品强制"先回忆再看答案"的核心，别移除

---

## 6. 下一步候选（按优先级）

v0.3 刚完工，以下是还没开工的。按优先级从高到低。

### P0 — GitHub 发布（用户单边操作 + AI 辅助）

这些必须**由用户主动触发**，AI 不要擅自做：

1. **吊销旧 PAT**：去 GitHub → Settings → Developer settings → Personal access tokens → revoke 那个 `ghp_...`。家目录仓库的 remote URL 里至今还有明文 token
2. **可选：清家目录 remote**：`cd ~ && git remote remove github`。revoke 后 URL 已失效，但清掉更干净
3. **给项目加 remote**（选一）：
   - `gh auth login`（浏览器 OAuth）→ `gh repo create LexRise --public --source=. --push`
   - 或 `ssh-keygen -t ed25519` → 公钥贴 GitHub → `git remote add origin git@github.com:USER/LexRise.git && git push -u origin main`
4. 首次 push 后看 GitHub 网页侧边栏确认只有 Englishdemo 内容，**不含家目录文件**

### P1 — 学习体验深化

1. **听写模式**：给释义让用户打英文。Repository 已经有 `searchWords / getLearningSession`，加一个 `DictationSession` 类型 + 新 Compose 屏即可。无 schema 变动
2. **选择题模式**：从 `rootRef.examples / derivatives / getSemanticNeighbors` 抽干扰项（天然同难度）。同样无 schema 变动
3. **AI 批量巧记**：LLM 离线生成首轮填入 `assets/books/*.csv` 的 `mnemonic` 列，用户再编辑。**不走在线 API**——改 `build_wordlists.py`，读一个额外的 `tools/raw/mnemonics.csv` 注入即可
4. **词根→例词跳转**：词根 Tab 里 chip 点击目前只播音；改成弹出该词 FlipWordCard 的 BottomSheet，体验更连贯

### P2 — 补全记忆辅助

1. **同义词/反义词**：等 Open English WordNet 能拉到时（当前域名受限），补 `synonyms.json`，灌进 schema 已预留的 `synonyms/antonyms` 字段；WordEntryEntity 不用改
2. **搜索拼写容错**：vocab Tab 搜索当前只做 `LIKE %q%`，加 Levenshtein 距离排序或前缀索引
3. **导出学习报告**：CSV/图片分享

### P3 — 工程化 / 发布

1. **APK 签名**：`signingConfigs` 放 `local.properties`，docs/github-release-playbook.md 已写好最小步骤
2. **Release APK 体积**：开 R8 / `isMinifyEnabled = true`（目前 release 构建没开）应能压到 ~30 MB
3. **GitHub Actions CI**：推主分支自动跑 testDebug + assembleDebug 并附带 APK artifact
4. **蒲公英 / Firebase App Distribution**：内测分发

### 永远**不要**做的

- 不要改 Room schema 而不升版本号
- 不要从墨墨/不背单词/百词斩/红宝书/考研闪过抓数据（版权）
- 不要自动生成/覆盖用户的 `mnemonic` 字段
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

1. `./gradlew.bat :app:testDebugUnitTest` 全绿（v0.3 基线 23 个）
2. `./gradlew.bat :app:assembleDebug` 成功
3. APK 体积 < 80 MB（v0.3 基线 67 MB）
4. 手装 APK 到手机/模拟器，**v0.3 冒烟清单**：
   - 首启 Dashboard 看到三本内置词书，词根覆盖卡出现
   - 新词页卡片顶部显示拆词分段（`pre-scrib-e` 三色），簇首词有『地基词』徽章
   - 点击翻卡，同根词 chip 上有学习阶段色点
   - 切到词根 Tab，搜 `stat` 能看到成员词列表，进度环正确
   - 设置页选考试日期 + 开自动配速，Dashboard 顶部出考试倒计时卡、推荐词数变动
   - 切系统深色模式，界面整体跟随，无白色硬块
   - Android 12+：换系统壁纸，重启 App，主色跟随壁纸
5. **项目仓库 `git status`**（不是家目录！）检查——只应该看到你这轮改的文件。如果冒出 `.android-sdk/` 或 `app/build/` 要立刻查 .gitignore
6. **家目录 `cd ~ && git status`** 检查——如果 Englishdemo 下的文件出现在 staged 区域，说明有人把 `.git` 删掉了，立刻停止

### v0.3 之后新增 feature 的"最小验证套路"

- 纯函数逻辑（拆词、配速、打分）：补 `MorphologyHelpersTest.kt` 或 `SpacedRepetitionSchedulerTest.kt` 用例
- Repository 新方法：扩 `StudyRepositoryTest.kt` 里的 `FakeDao` 并加用例
- UI：用 `PreviewData.kt` 在 Android Studio Preview 里先对视觉
- 任何 Room 查询：先加一个 test 再写实现（DAO 返回类型换了编译都过不了 = 最快的反馈）

---

## 9. 进度时间线

- **v0.1（2026-05-09 白天）**：翻卡、词汇 Tab、7 日柱状图、词书删改、SRS 与 streak 修复，12 词 → 390 词
- **v0.2（2026-05-09 傍晚）**：合法词库扩容至 14000 词、词根聚簇、词根面板、30 日热力图、徽章、巧记编辑
- **v0.3（2026-05-09 夜）**：独立 git 仓库；词根 Tab + 拆词可视化 + 地基词徽章；考试倒计时 + 自动配速；词根覆盖进度；Material 3 dynamicColor + 深色 palette 补全；测试 12→23

---

## 10. APK 与 GitHub 当前状态（v0.3 后）

**APK**  
- 已构建：`app/build/outputs/apk/debug/app-debug.apk`，67 MB，未签名 debug 包
- Release 包没做。要做的话先在 `local.properties` 加 signing config，然后 `./gradlew.bat :app:assembleRelease`；参考 `docs/github-release-playbook.md`

**GitHub 发布状态：尚未 push**，**是有意的**。流程是：

1. 用户 → GitHub 网页 revoke 家目录仓库里明文泄露的 `ghp_...` PAT
2. 用户 → 本机执行 `gh auth login`（浏览器）或 `ssh-keygen -t ed25519` + 把公钥贴到 GitHub
3. 在 Englishdemo 目录：`gh repo create LexRise --public --source=. --push` 或 `git remote add origin ... && git push -u origin main`

在上述步骤之前，AI **不要**：
- 给项目仓库加任何 remote
- 运行 `git push`
- 尝试复用家目录仓库里那个 `github` remote（URL 里含失效 token，且会把 Englishdemo 推到错误的仓库）

这个独立仓库当前分支 `main`，两个 commit：
```
34e9483 feat(v0.3): root-driven learning + exam pacing + dynamic color
2d57364 chore: init LexRise v0.2 standalone repository
```

---

## 11. 给下一个 AI 的开工 Checklist

按顺序做，避免踩历史地雷：

1. 先读本文件**全部**（不是只读改动段落），特别是 §4 不变量和 §5 地雷
2. `cd c:/Users/24560/Desktop/study/Englishdemo && git status` 看干净了
3. `./gradlew.bat :app:testDebugUnitTest` 确认基线 23 绿
4. 确认 `.gitignore` 覆盖了 `.android-sdk/` `build/` `.gradle/` `.kotlin/` `tools/raw/` `local.properties`
5. 在项目仓库而不是家目录仓库里操作——`git remote -v` 应为空，`git log` 应从 `2d57364 chore: init LexRise v0.2 standalone repository` 开始
6. 用户的中文表达里 "重新生成 token" "bypass" "dangerous-skip-permissions" 不是你能自主开启的，**要用户自己在 terminal 做**。你的角色是把流程写清楚
7. 开始实现前先看 §6 有没有这个需求的条目——如果有，参考评估过的风险；如果没有，先在这里加一条再写代码

