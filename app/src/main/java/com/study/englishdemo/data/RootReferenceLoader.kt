package com.study.englishdemo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

class RootReferenceLoader(private val context: Context) {
    private val mutex = Mutex()
    private var cache: Map<String, RootReference>? = null

    suspend fun lookup(key: String): RootReference? {
        if (key.isBlank()) return null
        val map = ensureLoaded() ?: return null
        return map[key.lowercase()]
    }

    private suspend fun ensureLoaded(): Map<String, RootReference>? {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: loadFromAsset().also { cache = it }
        }
    }

    private suspend fun loadFromAsset(): Map<String, RootReference> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open("reference/roots.json").use { stream ->
                val json = stream.bufferedReader().readText()
                val arr = JSONArray(json)
                val map = LinkedHashMap<String, RootReference>(arr.length())
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val key = obj.optString("key").lowercase()
                    if (key.isEmpty()) continue
                    val meanings = obj.optJSONArray("meanings")?.let { ja ->
                        (0 until ja.length()).map { ja.getString(it) }
                    } ?: emptyList()
                    val examples = obj.optJSONArray("examples")?.let { ja ->
                        (0 until ja.length()).map { ja.getString(it) }
                    } ?: emptyList()
                    map[key] = RootReference(key, meanings, examples)
                }
                map
            }
        }.getOrElse { emptyMap() }
    }
}
