# LexRise 词库构建工具

`tools/build_wordlists.py` 把开源词典转换成 LexRise APK 需要的 CSV/JSON 资源。

## 原始数据源（全部合法、可商用/可嵌入）

- **skywind3000/ECDICT**（MIT）— 词本体 + 翻译 + 词形变化。  
  下载：`https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv`（约 63 MB）
- **WithEnglishWeCan/generated-english-roots-list** — 1061 词根例词表。  
  下载：`https://raw.githubusercontent.com/WithEnglishWeCan/generated-english-roots-list/master/README.md`（约 80 KB）

这些原始文件**不入仓**（体积过大 + 遵循各自 license 即可），放在 `tools/raw/`，已在 `.gitignore` 中忽略。

## 一次性准备

```bash
mkdir -p tools/raw
curl -fL -o tools/raw/ecdict.csv \
  https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv
curl -fL -o tools/raw/roots_raw.md \
  https://raw.githubusercontent.com/WithEnglishWeCan/generated-english-roots-list/master/README.md
```

在大陆网络下这两个 URL 通常能走通（raw.githubusercontent.com 被代理友好）。若失败换个时段或挂代理。

## 生成 APK 资源

```bash
python tools/build_wordlists.py
```

输出（都是重建的，脚本幂等）：

- `app/src/main/assets/books/cet4_core.csv` — 约 3800 词
- `app/src/main/assets/books/cet6_core.csv` — 约 5400 词
- `app/src/main/assets/books/ky_core.csv` — 约 4800 词（考研大纲）
- `app/src/main/assets/reference/roots.json` — 约 1000 词根（JSON）

每个 CSV 列顺序：

```
term,phonetic,definition,translation,example,tags,rootKey,derivatives,frq,pos
```

`rootKey` 为空表示没有可靠聚簇（按高频继续排）。当前约 21–25% 的词能被稳定聚簇；其余保持 ECDICT 频率序。

## 为什么不用 WordNet / 红宝书 / 闪过

- 墨墨、不背单词、百词斩、红宝书、考研闪过 都是商业内容，**不可嵌入**。
- Open English WordNet（CC-BY 4.0，同义反义）体积大且 XML 结构复杂，第一版先不集成；`mnemonic`/`synonyms`/`antonyms` 列在 schema 已预留，后续版本补。

## 脚本调优点

短前缀（`ab/re/ad` 等）聚在一起反而误导学习，脚本已在 `NOISY_SHORT_PREFIXES` 列表过滤；前缀匹配仅在 ≥ 4 字母词根上开启。如果你手动添加更多词根，按 `example_to_root` 精确例词映射即可，避免增加 2–3 字母长度的前缀。
