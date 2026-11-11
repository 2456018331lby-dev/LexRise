# CLAUDE.md — LexRise / Englishdemo

本地离线 Android 背单词 App（CET4/CET6/考研）。Kotlin + Jetpack Compose + Material3 dynamicColor + Room v2 + WorkManager + DataStore。
当前 v0.39，180 个 Android 单测 + 3 个 Python 脚本单测全绿，debug APK 可装。

这份文件是每次会话都加载的「怎么在本仓库干活」速查。详情不在这里展开：
- 架构 / 不变量 / 下一步候选 → `docs/AI_HANDOFF.md`（开工前读 §4 不变量、§5 地雷）
- 动手步骤（改词库 / 升依赖 / Room 迁移 / 排错 / Git）→ `docs/MAINTENANCE.md`
- 发布 → `docs/github-release-playbook.md`

---

## 1. 干活循环（每次小改完必跑）

```powershell
# 单测（<60s）
.\gradlew.bat :app:testDebugUnitTest
# 改了 tools/build_wordlists.py 或 mnemonic seed/raw 才跑
python -m unittest discover -s tools -p "test_*.py"
# 可装机 APK（首次 ~4min，增量 ~30s）
.\gradlew.bat :app:assembleDebug
```

「测试绿 + assembleDebug 过」≈ 能装能跑，这是宣布完成的最低门槛。构建/测试用后台跑，别干等。

---

## 2. 不可破的不变量（改前必读，违反=线上崩溃/丢用户数据）

1. **Room schema 现在 v2，`fallbackToDestructiveMigration()` 会清空用户全部进度**。任何改 `*Entity`（加/删/改列、改索引）都必须升 version + 写 `Migration` + 补迁移测试。见 `MAINTENANCE.md §4`。
2. **本项目的主模式：不加列**。v0.3–v0.39 的所有新功能都走「派生查询 / 纯函数派生 / 构建期资源 / UI session 状态」，绝不动 Entity。想加功能先问：能不能用 `@Query` + Repository 拼 + 纯函数 + ViewModel 临时状态搞定？基本都能。
3. **所有 `build*` 逻辑都是纯函数**（`buildStudyFocusCue` / `buildClozeBlank` / `buildReviewSessionRadar` / `buildQuizOptions` / `computePaceRecommendation` …）：不要换成带 `Context` 的实现，必须能在 `:app:testDebugUnitTest`（non-instrumented）里跑。新逻辑先补 `MorphologyHelpersTest.kt` 再写实现。
4. **所有练习模式评分必经 `reviewWord`**（选择/完形/拼写/听写最终都调 `onRate(wordId, rating)`）。不为任何题型新开表，`review_logs` 是「难词专攻」的唯一数据源。完形 SRS 仍回原词条。
5. **Review 统计只算 Review Tab**：`reviewPracticeWord` 包装 `reviewWord` 并更新 `PracticeSessionStats`；Learn/Tough 走普通 `reviewWord`，不混入本轮统计。
6. **mnemonic 是用户可覆盖的本地内容**：默认巧记只能构建期通过 CSV 注入（`tools/mnemonics_seed.csv` 或不入仓的 `tools/raw/mnemonics.csv`）；**不在 Kotlin 层调在线 API，不在启动时覆盖用户已编辑的 mnemonic**。
7. **内置词书不可删/不可改名**（`source == "builtin"`，UI + Repository 双层守卫）；`positionInBook` 由脚本按聚簇顺序生成，别手改。
8. **导入互斥不能移除**：`ensureBundledBookImported` / `importBook` / `importPreview` 共享 `importMutex`。
9. **autoPace 是「覆盖」不是「替换」基线**：触发时不要写回 `dailyNewWordTarget`。

---

## 3. 加一个典型 feature 的落地套路

绝大多数新功能长这样（照抄 v0.14–v0.39 任意一个）：

1. `data/Models.kt`：加领域模型（`XxxBrief` + `XxxKind` 等，纯 data class / enum）。
2. `data/MorphologyHelpers.kt`：加 `buildXxx(...)` 纯函数派生逻辑。
3. `data/MorphologyHelpersTest.kt`：给每个分支补断言（这是回归保护，先写）。
4. `MainActivity.kt`：加 `XxxCard` Compose 组件挂到对应 Tab（所有页面都在这一个文件）。
5. 版本元数据 `app/build.gradle.kts` 同步 `versionCode` / `versionName`，更新 README + AI_HANDOFF 变更日志。
6. 跑第 1 节的循环，全绿再收工。

关键文件速查表在 `MAINTENANCE.md §9`；代码索引在 `AI_HANDOFF.md §3`。

---

## 4. 编排与协作（复用 OMC 全局规则，落到本项目）

- **派给 subagent**：跨多文件的改动、重构、全仓搜索、深调研 → 委派，保住主上下文写实现。定向单点查找（某函数/某行）直接用搜索工具。
- **模型分层**：快查用轻模型，常规改动 `sonnet`，架构/Room 迁移/涉安全的深活儿用 `opus`。
- **写审分离**：作者 pass 和审查 pass 分两趟，不在同一活动上下文里自审自批；宣布完成前收集验证证据（测试输出）。
- **并行**：独立任务并行发起；构建/测试后台跑。
- **失败即诊断**：同一路子失败两次就停下找根因，别打补丁式硬试；换方向若偏离原意先说明再动手。

---

## 5. Git 安全（本仓库特有，务必遵守）

- **只在 `Englishdemo/` 里跑 git**，绝不在家目录 `C:/Users/24560/` `git add`（那是另一个仓库，会扫进 `.ssh/` `.claude/` 等隐私目录）。
- `git remote -v` 必须指向 `2456018331lby-dev/LexRise`，**不要复用家目录那个含明文旧 PAT 的 `github` remote**。
- push 前查：无 `local.properties`、无 `*.jks/*.keystore`、无 `tools/raw/`、无 `.android-sdk/` `app/build/`。
- 只在用户明确要求时 commit；破坏性 git（force push / reset --hard / clean -f）需用户确认；不 push 到未确认的 remote。
- 用户中文里说的「重新生成 token / bypass / dangerous-skip-permissions」不是我能自主开启的，要用户自己在终端做，我只把流程写清楚。

---

## 6. 环境地雷（Windows）

- `local.properties` 的 `sdk.dir` 必须正斜杠 + `\:` 转义冒号：`sdk.dir=C\:/Users/24560/Desktop/study/Englishdemo/.android-sdk`。
- `processDebugResources` 偶发 `Could not move temporary workspace`（杀软锁目录）→ 重跑即可，别乱清缓存。
- 依赖别乱降：`material3:1.3.x`（DatePicker/CircularProgressIndicator API）、`com.google.android.material:1.12.0`（Theme.Material3.*）、`junit:4.13.2` 都是硬依赖。
- 源数据：`github.com` 在本机不通，`raw.githubusercontent.com` 通。词库原始文件不入仓，按 `tools/README.md` 下到 `tools/raw/` 再 `python tools/build_wordlists.py`。
- Compose：`weight` 是 `RowScope`/`ColumnScope` 扩展，不要顶层 import。
