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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val darkMode: StateFlow<Boolean> = repository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateSpeechRate(rate: Float) {
        repository.setSpeechRate(rate)
    }

    fun updateDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }

    val isOnboardingCompleted: StateFlow<Boolean> = repository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
    }
}
