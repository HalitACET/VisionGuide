package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.visionguide.domain.model.AuthSession
import com.example.visionguide.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    val session: StateFlow<AuthSession?> = repository.session

    fun logout() {
        repository.logout()
    }
}
