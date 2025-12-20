package com.example.visionguide.domain.repository

import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.model.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<AuthSession?>

    suspend fun login(email: String, password: String): Either<AppError, AuthSession>
    suspend fun register(email: String, password: String): Either<AppError, AuthSession>
    fun logout()
}
