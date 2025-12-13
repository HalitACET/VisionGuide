package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val speechRate: StateFlow<Float> = repository.speechRate
    
    val darkMode: StateFlow<Boolean> = repository.darkMode

    fun updateSpeechRate(rate: Float) {
        repository.setSpeechRate(rate)
    }

    fun updateDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }

    val isOnboardingCompleted: StateFlow<Boolean> = repository.isOnboardingCompleted

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
    }
}
