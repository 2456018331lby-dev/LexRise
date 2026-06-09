package com.study.englishdemo.data

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class WordBookParser {
    fun parse(stream: InputStream, filename: String, fallbackTitle: String): ImportPreview {
        return if (filename.endsWith(".txt", ignoreCase = true)) {
            parseText(stream, fallbackTitle)
        } else {
            parseCsv(stream, fallbackTitle)
        }
    }

    fun parseCsv(stream: InputStream, fallbackTitle: String): ImportPreview {
        val lines = BufferedReader(InputStreamReader(stream))
            .readLines()
            .filter { it.isNotBlank() }
        require(lines.size > 1) { "词书内容为空" }

        val words = normalizeWords(lines.drop(1).map { line ->
            val cols = parseCsvLine(line)
            ImportedWord(
                term = cols.getOrElse(0) { "" }.trim(),
                phonetic = cols.getOrElse(1) { "" }.trim(),
                definition = cols.getOrElse(2) { "" }.trim(),
                translation = cols.getOrElse(3) { "" }.trim(),
                example = cols.getOrElse(4) { "" }.trim(),
                tags = cols.getOrElse(5) { "" }
                    .split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() },
                rootKey = cols.getOrElse(6) { "" }.trim(),
                derivatives = cols.getOrElse(7) { "" }
                    .split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() },
                frq = cols.getOrElse(8) { "" }.trim().toIntOrNull() ?: 0,
                pos = cols.getOrElse(9) { "" }.trim(),
                mnemonic = cols.getOrElse(10) { "" }.trim(),
            )
        })

        val primaryTag = words.firstOrNull()?.tags?.firstOrNull() ?: "custom"
        return ImportPreview(
            title = fallbackTitle,
            description = "共 ${words.size} 个词条，适合离线背词与复习训练。",
            source = "imported",
            examTag = primaryTag,
            words = words,
        )
    }

    fun parseText(stream: InputStream, fallbackTitle: String): ImportPreview {
        val words = normalizeWords(BufferedReader(InputStreamReader(stream))
            .readLines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val normalized = line.trim()
                val parts = when {
                    normalized.contains("::") -> normalized.split("::", limit = 3)
                    normalized.contains("\t") -> normalized.split("\t")
                    normalized.contains(" - ") -> normalized.split(" - ", limit = 3)
                    else -> emptyList()
                }
                if (parts.size < 2) return@mapNotNull null
                ImportedWord(
                    term = parts[0].trim(),
                    phonetic = "",
                    definition = parts[1].trim(),
                    translation = parts[1].trim(),
                    example = parts.getOrElse(2) { "" }.trim(),
                    tags = listOf("custom"),
                )
            })
        return ImportPreview(
            title = fallbackTitle,
            description = "从 TXT 导入的简洁词书，共 ${words.size} 个词条。",
            source = "imported",
            examTag = "custom",
            words = words,
        )
    }

    private fun normalizeWords(words: List<ImportedWord>): List<ImportedWord> {
        val uniqueWords = words
            .filter { it.term.isNotBlank() && (it.translation.isNotBlank() || it.definition.isNotBlank()) }
            .distinctBy { it.term.lowercase() }
        require(uniqueWords.isNotEmpty()) { "未解析到有效词条，请检查 TXT/CSV 格式" }
        return uniqueWords
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        line.forEach { char ->
            when {
                char == '"' -> insideQuotes = !insideQuotes
                char == ',' && !insideQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result += current.toString()
        return result
    }
}
