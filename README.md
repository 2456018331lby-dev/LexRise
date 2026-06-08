# Englishdemo / LexRise v0.25

LexRise 是面向四级、六级、考研英语的原生 Android 背单词应用，走本地离线优先路线：词书、学习进度、复习记录和提醒都保存在本机，不依赖账号和云同步。

## v0.25 亮点

- **词根详情卡新增“词根详情导读”**
  - 在新词翻卡的同根词 chip 或词根 Tab 的成员 chip 打开底部详情卡时，先给出根族导读、词形导读、语境导读或速览导读
  - 导读卡展示行动标签、两项关键指标、强度条和焦点词 chips，让用户知道先比同根、先扫词形、先读例句还是快速回看
  - 底部详情卡支持纵向滚动，避免长释义、例句和导读内容在小屏上挤压
  - 逻辑由 `buildRootWordGuide` 纯函数派生，不新增数据库字段，不改变词根排序、学习顺序或 SRS 评分

## v0.24 亮点

- **词根 Tab 新增“词根图谱简报”**
  - 进入词根图谱后，会先判断整本词书的词根覆盖处于待生成、起步、扩展、收束还是基本铺开状态
  - 简报结合 `BookRootSnapshot` 和当前可见根族，展示词根覆盖、聚簇词入轨数量、进度条和优先根族 chips
  - 搜索词根时仍保留整本覆盖判断，只把焦点根族收窄到当前可见结果，避免把搜索结果误当作整本进度
  - 逻辑由 `buildRootAtlasBrief` 纯函数派生，不新增数据库字段，不改变词根列表排序、学习顺序或 SRS 评分

## v0.23 亮点

- **Dashboard 新增“七日节奏简报”**
  - 首页会在 30 日热力图前先判断最近 7 天复习节奏是安静期、恢复、平衡、稳态还是冲刺
  - 简报结合最近复习记录、今日完成数和当前负载，提示“先点亮今天 / 恢复节奏 / 稳步推进 / 保持节奏 / 收口复盘”
  - 卡片展示 7 日微型节奏柱、动量条、关键指标和行动标签，让用户先看懂节奏再看热力图
  - 逻辑由 `buildStudyRhythmBrief` 纯函数派生，不新增数据库字段，不改变复习记录、任务路线或 SRS 评分

## v0.22 亮点

- **Review Tab 新增“复习队列预案”**
  - 到期词进入练习前会先判断这轮更适合轻量回温、词根修复、语境回忆、主动提取还是混合清债
  - 同根词聚集时优先抓 `rootKey`；拼写/听写模式优先提示主动提取；完形或例句覆盖高时提示先读语境
  - 预案卡展示到期数量、当前模式/线索指标、强度条、行动标签和焦点词 chips，放在模式条和本轮统计之间
  - 逻辑由 `buildReviewQueueBrief` 纯函数派生，不新增数据库字段，不改变 SRS 评分、复习顺序或练习模式

## v0.21 亮点

- **新词页新增“本批新词策略”**
  - 新词列表顶部会先判断这一批更适合按词根、词形、语境还是混合方式学习
  - 同根词成组时先提示抓 rootKey；派生形较多时提示先扫词形变化；例句覆盖高时提示先读语境
  - 策略卡展示批次数量、关键线索、强度条和优先关注词，进入单张翻卡前先建立学习路线
  - 逻辑由 `buildWordBatchBrief` 纯函数派生，不新增数据库字段，不影响 Review 翻卡复用

## v0.20 基线亮点

- **词汇搜索新增“检索洞察”**
  - 词汇 Tab 会在结果列表前判断当前查询是原词命中、词形/typo 命中、同根聚簇、释义命中还是无结果
  - 洞察卡展示命中类型、结果强度、关键数量和优先查看的焦点词形/词条
  - 搜 `clarifed` 这类派生词 typo 时，会提示先看词形雷达，再回到原词建立记忆
  - 逻辑由 `buildVocabularySearchInsight` 纯函数派生，不新增数据库字段，不改变搜索排序

## v0.19 基线亮点

- **难词专攻新增“错题战情台”**
  - 难词 Tab 顶部会汇总当前错题池，判断主导问题是重建、词根、语境还是巩固
  - 展示错题总数、高风险数量、最高重来次数和当前打法标签
  - 战情台视觉采用渐变压力面板，与每张错题处方卡形成上下呼应
  - 逻辑由 `buildToughWordsBrief` 纯函数派生，不新增数据库字段，不改变错题排序和评分路径

## v0.18 基线亮点

- **新词卡新增“记忆锚”**
  - 翻卡正面会自动判断当前词优先抓词根、词形、语境还是基础释义
  - 词根锚会显示词根含义、同根邻居数和相关词；词形锚会展示常见 derivatives
  - 语境锚提示先读例句再遮词回想；无明显线索时回退到最小记忆策略
  - 逻辑由 `buildWordMemoryAnchor` 纯函数派生，不新增数据库字段，不覆盖用户巧记

## v0.17 基线亮点

- **Dashboard 新增“今日训练路线”**
  - 首页不再只给一个焦点，会把复习债、难词、词根覆盖和新词推进排成 1–3 步训练路线
  - 每一步都有入口按钮，可直接跳到复习、难词、词根或新词页
  - 路线卡使用横向任务轨道：任务标题、原因、关键指标和强度条一屏可见
  - 逻辑由 `buildDailyStudyRoute` 纯函数派生，不新增数据库字段，不改变原有学习/评分路径

## v0.16 基线亮点

- **难词专攻新增“错题处方”**
  - 每个难词卡会根据 `againCount`、`lapses`、学习阶段、词根和例句生成处理建议
  - 自动在“慢速重建 / 回到同根线索 / 放回例句里修复 / 短频快扫”之间切换
  - 难词卡视觉升级为处方卡：风险强度条、处方徽章、行动标签和两项关键指标常驻显示
  - 逻辑由 `buildToughWordPrescription` 纯函数派生，不新增数据库字段，不改变错题排序

## v0.15 基线亮点

- **Review Tab 新增“本轮教练”**
  - 本轮练习仪表不再只显示完成/稳答/再练，会根据当前模式和稳定率给出下一步策略
  - 自动在“先校准手感 / 降速修复 / 继续稳住 / 提高难度”之间切换
  - 稳定率高且样本足够时，提示从翻卡/选择升级到完形、拼写或听写；错题偏多时提示慢速复盘
  - 策略由 `buildPracticeSessionCoach` 纯函数派生，不新增数据库字段，不影响 SRS 评分路径

## v0.14 基线亮点

- **词根图谱新增“根族路线”**
  - 每个词根族自动判断“起步 / 推进 / 收尾 / 稳固”阶段，告诉用户下一步该先看哪些词
  - 词根卡片从简单进度条升级为渐变路线卡：阶段徽章、路线文案、触达比例、剩余词数和推荐词条一屏可见
  - 展开按钮从“展开全部”改为按阶段生成的动作文案，例如“展开首攻词”“展开收尾词”
  - 逻辑由 `buildRootGroupInsight` 纯函数派生，不新增数据库字段，不改变词书顺序

## v0.13 基线亮点

- **词汇结果新增“词形雷达”**
  - 搜索 `clarified` 或 `clarifed` 后，结果卡会直接显示命中的词形，解释为什么返回 `clarify`
  - 词汇卡片改成更清晰的层级：词条头部、释义、命中词形、词根/词性/标签分区更明确
  - 结果 chips 改为横向滑动，小屏不再因为长标签或多个词形挤压布局
  - 不改变搜索排序，不新增数据库字段，只把已有搜索能力可视化

## v0.12 基线亮点

- **词汇搜索支持派生词形**
  - 输入 `clarified`、`clarifies` 这类 derivatives 时，可以直接找回 `clarify`
  - 轻微拼错派生词也会回退到词形相似搜索：`clarifed` 也会尝试找 `clarify`
  - 词汇页搜索提示同步说明“原词 / 派生词 / 释义 / 英文解释”都可搜
  - 不新增数据库字段，直接复用词库里的 `derivatives` 列

## v0.11 基线亮点

- **完形题优先挖更合适的词形**
  - 当例句同时出现原词和派生词时，优先挖原词，训练焦点更贴合当前词条
  - 当例句只出现多个派生词时，优先挖例句里最早出现的词形，题干更自然
  - 正确答案继续保留例句里的实际大小写和词形，不破坏语法
  - 无可挖空例句时的提示同步改为“原词或派生词”，避免误导用户

## v0.10 基线亮点

- **完形模式支持派生词挖空**
  - 例句没有原形但包含 `clarified`、`clarifies` 这类 derivatives 时，也能生成完形题
  - 正确答案保留例句里的实际词形，语法更自然：`The note ____ the final goal.` → `clarified`
  - SRS 仍回流到原词条，不新增数据库字段
  - 让内置词库里更多真实例句可用于上下文训练

## v0.9 基线亮点

- **首页焦点一键行动**
  - “今日学习焦点”卡新增行动按钮，不再只是建议文案
  - 复习债 → 直达复习；考试配速/新词推进 → 直达新词；词根覆盖 → 直达词根图谱；完成态 → 进入巩固复习
  - 按钮文案由 `buildStudyFocusCue` 纯函数派生并纳入单测，避免 UI 硬编码策略
  - 首页从“告诉你该做什么”继续升级为“点一下就开始做”

## v0.8 基线亮点

- **首页今日学习焦点**
  - Dashboard 在核心指标下新增“今日学习焦点”卡，直接给出下一步建议
  - 自动在“先清复习债 / 按考试节奏加速 / 补词根覆盖 / 推进新词 / 保持手感”之间切换
  - 依据复习债、自动配速、词根覆盖、新词剩余和今日完成率派生，不新增数据库字段
  - 卡片配合渐变、图标、进度条和双指标 pill，让首页从“看数据”升级为“知道现在该做什么”

## v0.7 基线亮点

- **复习模式内本轮统计**
  - Review Tab 顶部新增“本轮练习仪表”，显示完成数、稳答数、再练数和稳定率
  - 翻卡、选择、完形、拼写、听写五种模式的评分都能进入同一轮统计
  - Learn 和难词专攻的评分不混入 Review session，避免数据语义混乱
  - 支持手动重置，方便做一组 10 题/20 题的自测

## v0.6 基线亮点

- **词汇搜索拼写容错**
  - 词汇 Tab 在直接命中不足时，会按英文词形相似度补充候选
  - 例如输入 `clarfy` 也会尝试找回 `clarify`
  - 仍保留中文释义、英文解释和学习阶段筛选
- **词汇页信息密度升级**
  - 搜索框增加容错提示和结果计数
  - 阶段筛选改成横向滚动 chips，小屏不再挤压
  - 词条行补充词根、词性和标签 chips，浏览时能顺手建立构词线索

## v0.5 基线亮点

- **第五种复习模式：完形/例句挖空**
  - 从词条例句中挖掉目标词，要求根据上下文和中文释义选回正确单词
  - 干扰项继续优先取同根词，答对 → GOOD，答错 → AGAIN，统一回流 SRS
  - 没有可挖空例句或干扰项不足时显示可跳过状态，不再卡在“生成中”
- **复习模式条优化**：顶部训练模式从固定四等分改成横向滑动胶囊条，五种模式在小屏也不挤压。
- **词根图谱体验升级**：词根 Tab / 翻卡里的同根词 chip 点击后打开底部详情卡，展示发音、拆词、释义、例句和学习阶段，不再只是播音。
- **导入逻辑加固**：内置词书导入和用户词书合并加互斥保护，进度初始化改为批量插入，避免首启并发或重复点击造成重复数据。
- **版本元数据修正**：`versionCode=5`、`versionName=0.5.0`，与当前功能版本保持一致。

## v0.4 基线亮点

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

## 维护

- [docs/MAINTENANCE.md](docs/MAINTENANCE.md)：日常维护手册，涵盖改词库、升级依赖、Room 迁移、排错、Git 安全清单等高频场景
- [docs/AI_HANDOFF.md](docs/AI_HANDOFF.md)：接手手册，面向下一个 AI 或人接手时的代码索引、关键不变量、下一步候选

## 进度 / 变更日志

### v0.25（2026-06-08）
- 词根详情 BottomSheet 新增“词根详情导读”，点击同根词或根族成员后先解释当前词应按词根、词形、语境还是速览复盘
- 新增 `RootWordGuide` / `RootWordGuideKind` 和 `buildRootWordGuide` 纯函数
- 新增 `RootWordGuidePanel` / `RootWordGuideMetric`，展示行动标签、指标、强度条和焦点词 chips
- 应用版本元数据同步到 0.25.0
- 测试 104 → 108

### v0.24（2026-06-08）
- 词根 Tab 新增“词根图谱简报”，在浏览单个根族前先解释整本词根覆盖状态和下一步打法
- 新增 `RootAtlasBrief` / `RootAtlasBriefKind` 和 `buildRootAtlasBrief` 纯函数
- 新增 `RootAtlasBriefCard` / `RootAtlasMetric`，展示词根覆盖、聚簇词入轨、进度条和优先根族 chips
- 应用版本元数据同步到 0.24.0
- 测试 99 → 104

### v0.23（2026-06-08）
- Dashboard 新增“七日节奏简报”，在 30 日热力图前解释最近一周复习节奏和下一步收口方式
- 新增 `StudyRhythmBrief` / `StudyRhythmBriefKind` 和 `buildStudyRhythmBrief` 纯函数
- 新增 `StudyRhythmBriefCard` / `StudyRhythmMetric`，展示 7 日节奏柱、动量条、指标和行动标签
- 应用版本元数据同步到 0.23.0
- 测试 94 → 99

### v0.22（2026-06-08）
- Review Tab 新增“复习队列预案”，在本轮统计前提示到期词应先轻量回温、按词根修复、读语境、主动提取或混合清债
- 新增 `ReviewQueueBrief` / `ReviewQueueBriefKind` 和 `buildReviewQueueBrief` 纯函数
- 新增 `ReviewQueueBriefCard` / `ReviewQueueMetric`，展示复习队列策略、强度条、指标和焦点词 chips
- 应用版本元数据同步到 0.22.0
- 测试 87 → 94

### v0.21（2026-06-08）
- 新词页新增“本批新词策略”，在进入单张翻卡前提示本批应先抓词根、词形、语境或混合线索
- 新增 `WordBatchBrief` / `WordBatchBriefKind` 和 `buildWordBatchBrief` 纯函数
- 新增 `WordBatchBriefCard` / `BatchBriefMetric`，展示批次策略、强度条、指标和焦点词 chips
- 应用版本元数据同步到 0.21.0
- 测试 82 → 87

### v0.20（2026-06-08）
- 词汇 Tab 新增“检索洞察”，在结果列表前解释当前命中类型和下一步查看顺序
- 新增 `VocabularySearchInsight` / `VocabularySearchInsightKind` 和 `buildVocabularySearchInsight` 纯函数
- 新增 `VocabularySearchInsightCard` / `VocabularyInsightMetric`，展示命中强度、数量指标和焦点词形/词条
- 应用版本元数据同步到 0.20.0
- 测试 77 → 82

### v0.19（2026-06-08）
- 难词专攻新增“错题战情台”，汇总错题池的主导处方、风险强度和优先打法
- 新增 `ToughWordsBrief` 和 `buildToughWordsBrief` 纯函数
- 新增 `ToughWordsBriefCard` / `ToughBriefMetric`，在难词列表顶部展示错题总量、高风险数量和最高重来次数
- 应用版本元数据同步到 0.19.0
- 测试 73 → 77

### v0.18（2026-06-08）
- 新词翻卡新增“记忆锚”，按词根、派生词形、例句和基础信息生成正面学习切入点
- 新增 `WordMemoryAnchor` / `WordMemoryAnchorKind` 和 `buildWordMemoryAnchor` 纯函数
- 新增 `WordMemoryAnchorPanel` / `MemoryAnchorMetric`，展示记忆锚徽章、线索文案、关键指标和相关词 chips
- 应用版本元数据同步到 0.18.0
- 测试 69 → 73

### v0.17（2026-06-08）
- Dashboard 新增“今日训练路线”，把复习债、错题、词根覆盖和新词推进排成可点击训练步骤
- 新增 `DailyStudyRoute` / `DailyStudyRouteStep` / `DailyStudyRouteTarget` 和 `buildDailyStudyRoute` 纯函数
- 新增 `DailyStudyRouteCard` / `DailyStudyRouteStepCard`，首页显示横向路线轨道并可直达对应 Tab
- 应用版本元数据同步到 0.17.0
- 测试 65 → 69

### v0.16（2026-06-08）
- 难词专攻卡片新增“错题处方”，按错题强度和词条线索生成重建/词根/语境/巩固建议
- 新增 `ToughWordPrescription` / `ToughWordPrescriptionKind` 和 `buildToughWordPrescription` 纯函数
- `ToughWordCard` 视觉升级为渐变处方卡，展示处方徽章、风险强度条、行动标签和关键指标
- 应用版本元数据同步到 0.16.0
- 测试 61 → 65

### v0.15（2026-06-07）
- Review Tab “本轮练习仪表”新增“本轮教练”策略面板
- 新增 `PracticeSessionCoach` / `PracticeSessionCoachKind` 和 `buildPracticeSessionCoach` 纯函数
- 教练策略按当前练习模式、本轮作答数和稳定率派生热身/修复/稳住/加难建议
- 应用版本元数据同步到 0.15.0
- 测试 57 → 61

### v0.14（2026-06-07）
- 词根 Tab 新增“根族路线”，按词根族触达比例生成起步/推进/收尾/稳固阶段
- 新增 `RootGroupInsight` / `RootGroupStage` 和 `buildRootGroupInsight` 纯函数
- `RootGroupCard` 视觉升级为渐变路线卡，展示阶段徽章、推荐关注词、触达比例和剩余词数
- 应用版本元数据同步到 0.14.0
- 测试 53 → 57

### v0.13（2026-06-07）
- 词汇结果卡新增“词形雷达”，展示当前查询命中的原词或派生词形
- 新增 `matchingWordForms` 纯函数，为 UI 提供精确/近似命中词形
- 词汇结果卡视觉层级优化，命中词形和元信息 chips 都改为横向滑动
- 应用版本元数据同步到 0.13.0
- 测试 50 → 53

### v0.12（2026-06-07）
- 词汇搜索直接命中范围扩展到 `derivatives`，搜索派生词/复数/时态词形可找回原词条
- fuzzy fallback 从只比对原词升级为比对原词 + 派生词，支持派生词轻微拼写错误
- 词汇页搜索 label、辅助文案和状态提示同步说明派生词搜索能力
- 应用版本元数据同步到 0.12.0
- 测试 47 → 50

### v0.11（2026-06-07）
- 完形挖空候选从“长词优先”改为“原词优先；无原词时按例句最早出现的派生词优先”
- `buildClozeBlank` 保留实际匹配词形，同时让含多个候选的例句生成更稳定
- 完形空状态文案同步说明会查找原词或派生词
- 应用版本元数据同步到 0.11.0
- 测试 45 → 47

### v0.10（2026-06-07）
- 完形/例句挖空支持原词 derivatives，例句只出现派生词时也能生成题
- 新增 `ClozeBlank` / `buildClozeBlank`，保留匹配到的实际词形作为正确答案
- `buildClozeQuestion` 改用派生词候选，同时 SRS 仍记录到原词条
- 应用版本元数据同步到 0.10.0
- 测试 43 → 45

### v0.9（2026-06-07）
- Dashboard “今日学习焦点”新增行动按钮，按焦点类型跳转到复习、新词或词根 Tab
- `StudyFocusCue` 新增 `actionLabel`，行动文案由纯函数统一派生
- 补齐 NEW_WORDS 分支单测，并为各焦点策略断言行动文案
- 应用版本元数据同步到 0.9.0
- 测试 42 → 43

### v0.8（2026-06-07）
- Dashboard 新增“今日学习焦点”卡，按当前学习状态给出下一步建议
- 新增 `buildStudyFocusCue` 纯函数，复习债、自动配速、词根覆盖、新词推进和完成态都可测试
- 首页卡片增加视觉层级：渐变背景、策略图标、进度条和双指标 pill
- 应用版本元数据同步到 0.8.0
- 测试 38 → 42

### v0.7（2026-06-07）
- Review Tab 新增本轮练习统计卡：完成 / 稳答 / 再练 / 稳定率
- Review 内五种练习模式统一计入本轮统计，Learn/Tough 不混入
- 新增统计重置入口
- 应用版本元数据同步到 0.7.0
- 测试 37 → 38

### v0.6（2026-06-07）
- 词汇 Tab 新增拼写容错搜索，直接命中不足时按词形相似度补候选
- 搜索框增加容错说明和结果状态提示
- 学习阶段筛选改为横向滚动 chips
- 词汇结果行展示词根、词性、标签 chips
- 应用版本元数据同步到 0.6.0
- 测试 34 → 37

### v0.5（2026-06-07）
- 新增完形/例句挖空复习模式，复用同根优先干扰项和 SRS 评分
- 选择题/完形题增加“干扰项不足/例句不可挖空”的失败态和跳过按钮
- 词根同根词 chip 改为打开底部详情卡，发音入口保留在卡内
- 复习模式切换条改为横向滑动胶囊，适配五模式和小屏
- 内置/用户词书导入加互斥保护，进度初始化改为批量插入
- 应用版本元数据同步到 0.5.0
- 测试 30 → 34

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
