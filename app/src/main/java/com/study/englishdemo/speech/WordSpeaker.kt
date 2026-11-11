package com.study.englishdemo.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class WordSpeaker(private val context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ttsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val cacheDir = File(context.applicationContext.cacheDir, "audio_cache").apply {
        if (!exists()) mkdirs()
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts.language = Locale.US
            tts.setSpeechRate(0.92f)
        }
    }

    fun speak(text: String, accentType: Int = 2) { // 2 = US, 1 = UK
        val cleanWord = text.trim().lowercase()
        if (cleanWord.isBlank()) return

        val cachedFile = File(cacheDir, "${cleanWord}_$accentType.mp3")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            playAudioFile(cachedFile) { fallbackTts(text) }
            return
        }

        // Attempt online high-definition fetch in background
        scope.launch {
            val success = downloadAudio(cleanWord, accentType, cachedFile)
            if (success && cachedFile.exists()) {
                playAudioFile(cachedFile) { fallbackTts(text) }
            } else {
                fallbackTts(text)
            }
        }
    }

    private fun downloadAudio(word: String, type: Int, target: File): Boolean {
        return try {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val urlString = "https://dict.youdao.com/dictvoice?audio=$encoded&type=$type"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.instanceFollowRedirects = true
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                target.length() > 500 // Valid MP3 is typically > 500 bytes
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun playAudioFile(file: File, onErrorFallback: () -> Unit) {
        scope.launch(Dispatchers.Main) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .build()
                    )
                    setDataSource(context.applicationContext, Uri.fromFile(file))
                    setOnErrorListener { _, _, _ ->
                        onErrorFallback()
                        true
                    }
                    setOnCompletionListener {
                        it.release()
                        if (mediaPlayer == it) mediaPlayer = null
                    }
                    prepareAsync()
                    setOnPreparedListener { start() }
                }
            } catch (_: Exception) {
                onErrorFallback()
            }
        }
    }

    private fun fallbackTts(text: String) {
        scope.launch(Dispatchers.Main) {
            if (ttsReady) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word:$text")
            }
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            tts.stop()
            tts.shutdown()
        } catch (_: Exception) {}
    }
}
