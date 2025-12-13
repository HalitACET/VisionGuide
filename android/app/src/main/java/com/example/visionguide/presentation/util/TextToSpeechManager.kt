package com.example.visionguide.presentation.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("tr", "TR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    android.util.Log.e("VisionGuideTTS", "Turkish language not supported or missing data")
                    // Fallback to English if Turkish is not supported
                    tts?.setLanguage(Locale.ENGLISH)
                } else {
                    android.util.Log.d("VisionGuideTTS", "TTS Initialized successfully")
                }
                isInitialized = true
            } else {
                android.util.Log.e("VisionGuideTTS", "TTS Initialization failed")
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        if (isInitialized) {
            android.util.Log.d("VisionGuideTTS", "Speaking: $text")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            android.util.Log.e("VisionGuideTTS", "TTS not initialized yet")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
