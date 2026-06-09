#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
LexRise wordlist builder.

Reads tools/raw/ecdict.csv (from skywind3000/ECDICT, MIT) and
tools/raw/roots_raw.md (from WithEnglishWeCan/generated-english-roots-list)
and produces:

    app/src/main/assets/books/cet4_core.csv
    app/src/main/assets/books/cet6_core.csv
    app/src/main/assets/books/ky_core.csv
    app/src/main/assets/reference/roots.json

Each book CSV has this header (extended format — the app parses old 6-col
format too):
    term,phonetic,definition,translation,example,tags,rootKey,derivatives,frq,pos,mnemonic

Run from repo root:
    python tools/build_wordlists.py
"""

import csv
import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RAW_DIR = ROOT / "tools" / "raw"
ASSETS_BOOKS = ROOT / "app" / "src" / "main" / "assets" / "books"
ASSETS_REF = ROOT / "app" / "src" / "main" / "assets" / "reference"
MNEMONIC_SEED = ROOT / "tools" / "mnemonics_seed.csv"
RAW_MNEMONICS = RAW_DIR / "mnemonics.csv"

EXAM_SPECS = {
    "cet4": {
        "filename": "cet4_core.csv",
        "max_words": 4000,
        "min_collins": None,
    },
    "cet6": {
        "filename": "cet6_core.csv",
        "max_words": 6000,
        "min_collins": None,
    },
    "ky": {
        "filename": "ky_core.csv",
        "max_words": 5500,
        "min_collins": None,
    },
}

CSV_HEADER = [
    "term",
    "phonetic",
    "definition",
    "translation",
    "example",
    "tags",
    "rootKey",
    "derivatives",
    "frq",
    "pos",
    "mnemonic",
]


def parse_roots_md(path: Path):
    """
    Returns:
        roots: list of {"key": "ab", "meanings": [...], "examples": [...]}
        example_to_root: dict word -> root_key
    """
    text = path.read_text(encoding="utf-8")
    raw_roots = []

    row_re = re.compile(r"^\|\s*(\d+)\s*\|\s*\*\*\[([^\]]+)\]\*\*\s*\|(.*?)\|(.*?)\|\s*$")
    for line in text.splitlines():
        m = row_re.match(line)
        if not m:
            continue
        _, key, meanings_raw, examples_raw = m.groups()
        key = key.strip().lower()
        meanings = [s.strip() for s in meanings_raw.split("<br>") if s.strip()]
        examples = []
        for chunk in examples_raw.split("<br>"):
            word = chunk.split("-")[0].strip().lower()
            word = re.sub(r"[^a-z\-' ]", "", word)
            if word and len(word) >= 2:
                examples.append(word)
        raw_roots.append({"key": key, "meanings": meanings, "examples": examples})

    # Drop roots that are too short AND too promiscuous (every prefix match
    # would pull them in) — e.g. "ab", "re", "ad" create noisy clusters.
    NOISY_SHORT_PREFIXES = {
        "ab", "abs", "ad", "co", "con", "de", "dis", "en", "em", "ex",
        "im", "in", "ir", "il", "non", "pre", "pro", "re", "se", "sub",
        "un", "super", "inter", "trans", "over", "under", "out",
    }
    roots = [r for r in raw_roots if r["key"] not in NOISY_SHORT_PREFIXES]

    example_to_root = {}
    for r in roots:
        for w in r["examples"]:
            example_to_root.setdefault(w, r["key"])
    return roots, example_to_root


def clean_translation(raw: str) -> str:
    """Trim ECDICT translation to the first 2 senses for card display."""
    if not raw:
        return ""
    raw = raw.replace("\r", "")
    # ECDICT uses literal \n between senses
    parts = [p.strip() for p in raw.split("\\n") if p.strip()]
    if not parts:
        parts = [p.strip() for p in raw.split("\n") if p.strip()]
    # drop any "[网络] ..." tail noise
    parts = [p for p in parts if not p.startswith("[网络]")]
    keep = parts[:2]
    return "； ".join(keep)[:140]


def clean_mnemonic(raw: str) -> str:
    """Keep built-in mnemonic hints short enough for phone cards."""
    text = re.sub(r"\s+", " ", (raw or "").strip())
    return text[:120]


def load_mnemonics(paths) -> dict:
    """Load offline mnemonic rows keyed by lowercase term.

    Later files override earlier files, so tools/raw/mnemonics.csv can replace
    the committed seed without requiring online API calls.
    """
    mnemonics = {}
    for path in paths:
        if not path.exists():
            continue
        with path.open("r", encoding="utf-8", newline="") as f:
            reader = csv.DictReader(f)
            fieldnames = {name.strip().lower(): name for name in (reader.fieldnames or [])}
            term_col = fieldnames.get("term") or fieldnames.get("word")
            mnemonic_col = fieldnames.get("mnemonic") or fieldnames.get("note")
            if not term_col or not mnemonic_col:
                raise ValueError(f"{path} must contain term,mnemonic columns")
            for row in reader:
                term = (row.get(term_col) or "").strip().lower()
                mnemonic = clean_mnemonic(row.get(mnemonic_col) or "")
                if term and mnemonic:
                    mnemonics[term] = mnemonic
    return mnemonics


def apply_mnemonics(words: list, mnemonics: dict) -> int:
    count = 0
    for w in words:
        mnemonic = mnemonics.get(w["term"].lower(), "")
        w["mnemonic"] = mnemonic
        if mnemonic:
            count += 1
    return count


def derive_derivatives(exchange: str) -> str:
    """ECDICT exchange column like 'p:abided/i:abiding/s:abides'"""
    if not exchange:
        return ""
    words = set()
    for seg in exchange.split("/"):
        if ":" not in seg:
            continue
        _, value = seg.split(":", 1)
        for w in re.split(r"[,\s]+", value.strip()):
            if w and re.match(r"^[a-zA-Z\-']+$", w):
                words.add(w.lower())
    return "|".join(sorted(words)[:6])


def detect_root(term: str, example_to_root: dict) -> str:
    """Look up full word first, then longest prefix match on the root table."""
    lw = term.lower()
    if lw in example_to_root:
        return example_to_root[lw]
    # prefix match on roots table (longest first), but we don't have roots list here;
    # caller passes a sorted key list via closure if needed. Keep simple for v1.
    return ""


def select_exam(rows, tag_key: str, spec) -> list:
    """Filter ECDICT rows for a CET/Ky tag, dedup, and cap."""
    selected = []
    seen = set()
    for r in rows:
        tags = (r.get("tag") or "").split()
        if tag_key not in tags:
            continue
        term = (r.get("word") or "").strip()
        if not term or not re.match(r"^[a-zA-Z][a-zA-Z\-']*$", term):
            continue
        if term.lower() in seen:
            continue
        trans = clean_translation(r.get("translation") or "")
        if not trans:
            continue
        seen.add(term.lower())
        try:
            frq = int(r.get("frq") or 0)
        except ValueError:
            frq = 0
        selected.append({
            "term": term,
            "phonetic": (r.get("phonetic") or "").strip(),
            "definition": "",
            "translation": trans,
            "example": "",
            "frq": frq,
            "exchange": r.get("exchange") or "",
            "pos": (r.get("pos") or "").strip().replace(":", "/"),
            "mnemonic": "",
        })
    # ECDICT frq: smaller = more common; cap total
    selected.sort(key=lambda x: (0 if x["frq"] > 0 else 1, x["frq"] or 10_000_000, x["term"].lower()))
    if spec["max_words"]:
        selected = selected[: spec["max_words"]]
    return selected


def order_by_root(words: list, example_to_root: dict, roots_sorted_keys: list) -> list:
    """Group same-root words together; within a root group, keep frq order.
    Words with no detectable root come last, frq-ordered."""
    buckets = {}
    orphan = []
    for w in words:
        lw = w["term"].lower()
        root = example_to_root.get(lw, "")
        if not root:
            # prefix match, but only on roots >= 4 chars to avoid noisy clusters
            for k in roots_sorted_keys:
                if len(k) < 4:
                    continue
                if lw.startswith(k) and len(lw) > len(k) + 1:
                    root = k
                    break
        w["rootKey"] = root
        if root:
            buckets.setdefault(root, []).append(w)
        else:
            orphan.append(w)
    # Drop tiny clusters (< 2 words) — a cluster of one defeats the point
    for k in list(buckets.keys()):
        if len(buckets[k]) < 2:
            orphan.extend(buckets.pop(k))
            for w in orphan[-1:]:
                w["rootKey"] = ""
    # order root groups by smallest frq in group (group containing most common word first)
    ordered_roots = sorted(
        buckets.keys(),
        key=lambda k: min((w["frq"] or 10_000_000) for w in buckets[k]),
    )
    ordered = []
    for k in ordered_roots:
        group = sorted(buckets[k], key=lambda w: (w["frq"] or 10_000_000, w["term"].lower()))
        ordered.extend(group)
    orphan.sort(key=lambda w: (w["frq"] or 10_000_000, w["term"].lower()))
    ordered.extend(orphan)
    return ordered


def write_book(words: list, out_path: Path, exam_tag: str):
    with out_path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f, quoting=csv.QUOTE_MINIMAL)
        writer.writerow(CSV_HEADER)
        for w in words:
            tags = f"{exam_tag}|{w['rootKey']}" if w["rootKey"] else exam_tag
            writer.writerow([
                w["term"],
                w["phonetic"],
                w["definition"],
                w["translation"],
                w["example"],
                tags,
                w["rootKey"],
                derive_derivatives(w["exchange"]),
                w["frq"],
                w["pos"],
                w.get("mnemonic", ""),
            ])


def main():
    if not (RAW_DIR / "ecdict.csv").exists():
        print(f"Missing {RAW_DIR/'ecdict.csv'}. See tools/README.md for download steps.", file=sys.stderr)
        sys.exit(1)
    if not (RAW_DIR / "roots_raw.md").exists():
        print(f"Missing {RAW_DIR/'roots_raw.md'}. See tools/README.md.", file=sys.stderr)
        sys.exit(1)

    ASSETS_BOOKS.mkdir(parents=True, exist_ok=True)
    ASSETS_REF.mkdir(parents=True, exist_ok=True)

    print("Parsing roots table...")
    roots, example_to_root = parse_roots_md(RAW_DIR / "roots_raw.md")
    roots_sorted_keys = sorted({r["key"] for r in roots}, key=lambda k: -len(k))
    print(f"  {len(roots)} roots, {len(example_to_root)} tagged example words")

    mnemonics = load_mnemonics([MNEMONIC_SEED, RAW_MNEMONICS])
    print(f"Loaded {len(mnemonics)} offline mnemonic hints")

    print("Reading ECDICT...")
    rows = []
    with (RAW_DIR / "ecdict.csv").open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            rows.append(row)
    print(f"  {len(rows)} rows")

    for tag, spec in EXAM_SPECS.items():
        print(f"\nBuilding {tag}...")
        words = select_exam(rows, tag, spec)
        print(f"  {len(words)} after filter")
        words = order_by_root(words, example_to_root, roots_sorted_keys)
        with_root = sum(1 for w in words if w["rootKey"])
        print(f"  {with_root} words mapped to a root ({100*with_root/max(len(words),1):.1f}%)")
        with_mnemonic = apply_mnemonics(words, mnemonics)
        print(f"  {with_mnemonic} words received a mnemonic hint")
        out = ASSETS_BOOKS / spec["filename"]
        write_book(words, out, tag)
        print(f"  wrote {out} ({out.stat().st_size//1024} KB)")

    # Emit a slimmed roots.json for in-app reference lookups
    roots_out = []
    for r in roots:
        if not r["meanings"] or not r["examples"]:
            continue
        roots_out.append({
            "key": r["key"],
            "meanings": r["meanings"][:3],
            "examples": r["examples"][:8],
        })
    (ASSETS_REF / "roots.json").write_text(
        json.dumps(roots_out, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"\nWrote {ASSETS_REF/'roots.json'} ({(ASSETS_REF/'roots.json').stat().st_size//1024} KB, {len(roots_out)} roots)")


if __name__ == "__main__":
    main()
