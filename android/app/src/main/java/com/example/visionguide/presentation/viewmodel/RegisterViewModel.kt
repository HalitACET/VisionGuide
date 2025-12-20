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

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun submit() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password
        val confirm = uiState.value.confirmPassword

        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            _uiState.update { it.copy(error = "Tüm alanlar zorunludur") }
            return
        }
        if (password != confirm) {
            _uiState.update { it.copy(error = "Şifreler eşleşmiyor") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val result = repository.register(email, password)
            when (result) {
                is Either.Right -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is Either.Left -> {
                    val msg = when (result.value) {
                        AppError.Unauthorized -> "Yetkisiz"
                        AppError.Network -> "Ağ hatası"
                        AppError.Timeout -> "Zaman aşımı"
                        is AppError.Server -> result.value.message ?: "Geçersiz bilgi"
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
