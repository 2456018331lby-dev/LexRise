# Englishdemo / LexRise v0.4

LexRise 是面向四级、六级、考研英语的原生 Android 背单词应用，走本地离线优先路线：词书、学习进度、复习记录和提醒都保存在本机，不依赖账号和云同步。

## v0.4 亮点

- **四种复习模式**（复习 Tab 顶部切换，SRS 评分统一回流）
  - 翻卡（原 v0.3 行为）
  - 选择题：干扰项优先从同根词中抽，训练词根判别；正确 → GOOD，错误 → AGAIN
  - 拼写：看中文写英文，按 Levenshtein 距离自动映射四档评分（完美 → EASY，一两个字母 → GOOD，较差 → HARD，差得远 → AGAIN）
  - 听写：同拼写，但前置 TTS 播词、隐藏英文
- **难词专攻 Tab**：聚合历史『重来』次数 Top N 的词做错题本，按重来次数倒序；原地『会了/不会』评分
- **翻卡交互修复**
  - 词根意思现在紧跟拆词条显示（不用翻卡再点一次）
  - 翻面面板去掉重复的词根意思段，只留同根例词，整体节奏紧凑

- **词根驱动学习**（这是 LexRise 区别于墨墨/不背单词的主路径）
  - 翻卡顶部展示**前缀 · 词根 · 后缀**三色可视化拆分，每次出卡都在做一次构词法训练
  - 簇首词标『**地基词**』徽章：学会它，其他同根词自动减负
  - 同根词 chip 带学习阶段色点，一眼看到哪些已会、哪些还没碰
  - 新增 **词根 Tab**：按词根聚合浏览整本词书，每根一个进度环 + 成员词列表
  - Dashboard 显示**词根覆盖进度**（已接触 N / 全书 M 个根）
- **考试日期 + 自动配速**
  - 设置页可选考试日期，开启自动配速后，App 按剩余词量 + 剩余天数自动算每日新词量（+20% 缓冲，5~40 词/天内）
  - Dashboard 顶部显示考试倒计时卡
- **Material 3 动态取色**：Android 12+ 跟随系统主题色，自然融入壁纸配色；低版本自动回退到 LexRise 品牌色
- **深色模式**：完整的 Material 3 深色 palette，容器/描边色都补齐

- **三本真实规模内置词书**（首启自动导入，约 14000 词，来自 MIT 协议的 ECDICT）
  - 四级高频 · 词根序（CET4 约 3800 词）
  - 六级进阶 · 词根序（CET6 约 5400 词）
  - 考研英语 · 大纲词（考研英语一约 4800 词）
- **词根聚簇排序**：同词根的词连着出现（例：state → statement → status；include → conclude → exclude）。比按字母序或死记高频表更接近墨墨的学习节奏，但是用的是完全开源合法的词根数据。
- **翻卡释义面升级**：除了中文翻译和例句，展示词根及含义、同根例词、ECDICT 派生词（复数、过去式等），并允许本地编辑巧记/助记口诀。
- **30 日热力图 + 连续徽章**：Dashboard 顶部展示 5×6 热力格，连续打卡 3 / 7 / 30 / 100 天有不同徽章文案和进度条。
- **翻卡交互**：默认盖住释义，点击卡片才翻出，评分按钮在翻出后激活，强制"先自测再对答案"的主动回忆流程。
- **词汇浏览 Tab**：全本词书按关键词搜索（term / translation / definition 都命中）+ 按学习阶段筛选。
- **词书管理**：非 builtin 可改名、可删除；重复导入同名词书自动合并去重，不产生副本。
- **SRS**：修过 `interval*1.2` 不推进、streak 在"今天没打卡"情形归零等 v0.1 的边界 bug。
- **发音**：系统 `TextToSpeech`。

## 内容合法性

LexRise 不打包以下任何商业词书的原文：墨墨背单词、不背单词、百词斩、新东方红宝书、考研闪过、有道词典精选等。词本体来自 MIT 协议的 [skywind3000/ECDICT](https://github.com/skywind3000/ECDICT)，词根来自 [WithEnglishWeCan/generated-english-roots-list](https://github.com/WithEnglishWeCan/generated-english-roots-list)。巧记/口诀字段默认留空，由用户本地编辑。

## 技术栈

- Kotlin
- Jetpack Compose + Material3
- Room v2（新 schema，含 rootKey / derivatives / mnemonic / frq / pos / positionInBook 等字段）
- DataStore Preferences
- WorkManager
- Android TextToSpeech

## 目录结构

- `app/` Android 应用模块
- `app/src/main/assets/books/` 内置 CSV 词书（由 `tools/build_wordlists.py` 生成）
- `app/src/main/assets/reference/roots.json` 1000+ 词根词缀及含义
- `tools/` 词库构建脚本和它的使用说明
- `docs/` 发布手册和架构笔记

## 自定义词书导入

### CSV（新 10 列格式）

```csv
term,phonetic,definition,translation,example,tags,rootKey,derivatives,frq,pos
clarify,/ˈklærəfaɪ/,make easy to understand,澄清,Please clarify the goal.,cet4|core,clar,clarified|clarifies,1200,vt.
```

后四列可留空以兼容旧 6 列格式，不会报错。

### TXT

```text
abandon :: 放弃 :: Never abandon your plan.
benefit :: 益处 :: Daily practice brings benefit.
```

TAB 或 ` - ` 也作分隔符。空词条和重复单词自动过滤。

### 去重规则

导入时若已有同名词书（非 builtin），新文件中已存在的 term 会跳过，只追加新词并刷新总词数。要生成新词书，改导入文件标题或先把现有同名词书重命名。

## 本地开发

```bash
./gradlew test
./gradlew assembleDebug
```

Windows PowerShell：

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

`local.properties` 的 `sdk.dir` 在 Windows 下建议正斜杠 + `\:` 冒号转义：

```properties
sdk.dir=C\:/Users/你的用户名/Desktop/study/Englishdemo/.android-sdk
```

## 词库再生成

原始词典不入仓（体积 + license），按 [tools/README.md](tools/README.md) 说明下载 ECDICT 和 roots 源数据到 `tools/raw/`，然后：

```bash
python tools/build_wordlists.py
```

脚本会重建 `app/src/main/assets/books/*.csv` 和 `app/src/main/assets/reference/roots.json`。

## 发布

见 [docs/github-release-playbook.md](docs/github-release-playbook.md)，包含签名 release APK 的完整最小步骤。

## 进度 / 变更日志

### v0.4（2026-05-09）
- 复习 Tab 支持四模式（翻卡/选择/拼写/听写），共用 SRS
- 新 Tab「难词专攻」聚合错题本
- 修翻卡交互 bug：词根意思不再要二次点击；翻面去掉重复段落
- 底栏 6 项，Settings 移到 TopAppBar 齿轮
- 测试 23 → 30

### v0.3（2026-05-09）
- 项目独立为自己的 git 仓库（之前共用家目录仓库）
- 新增词根 Tab：聚合整本词书按根浏览，带进度环和成员词
- 翻卡新增拆词可视化（前缀/词根/后缀三色分段）和『地基词』徽章
- 同根词 chip 显示对方学习阶段色点
- 新增考试日期 + 自动配速（按剩余词量 / 剩余天数 + 20% 缓冲）
- Dashboard 新增考试倒计时卡、词根覆盖卡
- Theme 接入 Material 3 dynamicColor；深色 palette 补全
- 测试从 12 增到 23（+6 configuration/拆词/簇首/图谱 用例）

### v0.2（2026-05-09）
- 内置词库由 390 词扩到 14000 词
- 新增词根聚簇排序、词根面板、同根词和派生词
- Dashboard 从 7 日柱状图升级为 30 日热力图 + 连续徽章
- 翻卡可编辑本地巧记
- Room schema v1 → v2（首次升级清库重建）
- 新增 `tools/build_wordlists.py` 词库构建脚本
- 清理 JVM 崩溃残留、修 `local.properties` 转义、`.gitignore` 覆盖 `tools/raw/`

### v0.1（2026-05-09）
- 首版翻卡交互、词汇 Tab、7 日柱状图、词书删除与重命名、SRS 区间推进 bug 修复
- 11 个单元测试，`assembleDebug` 成功
