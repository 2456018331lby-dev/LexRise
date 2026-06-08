package com.study.englishdemo.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class WordSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private val speaker = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            speaker.language = Locale.US
            speaker.setSpeechRate(0.95f)
        }
    }

    fun speak(text: String) {
        if (ready) {
            speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word:$text")
        }
    }
}
