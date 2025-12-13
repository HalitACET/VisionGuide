package com.example.visionguide.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vision_guide_settings", Context.MODE_PRIVATE)

    private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false)) // Default to system/false for now
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat("speech_rate", rate).apply()
        _speechRate.value = rate
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _darkMode.value = enabled
    }

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _isOnboardingCompleted.value = completed
    }
}
