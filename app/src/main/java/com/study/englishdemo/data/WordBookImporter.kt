package com.study.englishdemo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

interface BookImporter {
    suspend fun importBundledBook(assetPath: String): ImportPreview
    suspend fun import(stream: InputStream, filename: String, fallbackTitle: String): ImportPreview
}

class WordBookImporter(private val context: Context) : BookImporter {
    private val parser = WordBookParser()

    override suspend fun importBundledBook(assetPath: String): ImportPreview = withContext(Dispatchers.IO) {
        context.assets.open(assetPath).use { stream ->
            parser.parseCsv(stream, "自定义词书")
        }
    }

    override suspend fun import(stream: InputStream, filename: String, fallbackTitle: String): ImportPreview =
        withContext(Dispatchers.IO) {
            parser.parse(stream, filename, fallbackTitle)
        }
}
