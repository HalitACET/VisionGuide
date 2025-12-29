package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun submit() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "E-posta ve şifre zorunludur") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val result = repository.login(email, password)
            when (result) {
                is Either.Right -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is Either.Left -> {
                    val msg = when (result.value) {
                        AppError.Unauthorized -> "E-posta veya şifre hatalı"
                        AppError.Network -> "Ağ hatası"
                        AppError.Timeout -> "Zaman aşımı"
                        is AppError.Server -> "Sunucu hatası: ${result.value.code}"
                        is AppError.Unknown -> result.value.cause?.message ?: "Bilinmeyen hata"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
