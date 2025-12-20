package com.example.visionguide.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.model.AuthSession
import com.example.visionguide.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : AuthRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("vision_guide_auth", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow<AuthSession?>(prefs.getString(KEY_LOGGED_IN_EMAIL, null)?.let { AuthSession(it) })
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override suspend fun login(email: String, password: String): Either<AppError, AuthSession> = try {
        val storedEmail = prefs.getString(KEY_USER_EMAIL, null)
        val storedPassword = prefs.getString(KEY_USER_PASSWORD, null)

        if (storedEmail.isNullOrBlank() || storedPassword.isNullOrBlank()) {
            Either.Left(AppError.Unauthorized)
        } else if (storedEmail == email && storedPassword == password) {
            prefs.edit().putString(KEY_LOGGED_IN_EMAIL, email).apply()
            val session = AuthSession(email)
            _session.value = session
            Either.Right(session)
        } else {
            Either.Left(AppError.Unauthorized)
        }
    } catch (t: Throwable) {
        Either.Left(AppError.Unknown(t))
    }

    override suspend fun register(email: String, password: String): Either<AppError, AuthSession> = try {
        if (email.isBlank() || password.isBlank()) {
            Either.Left(AppError.Server(code = 400, message = "Geçersiz bilgi"))
        } else {
            prefs.edit()
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PASSWORD, password)
                .putString(KEY_LOGGED_IN_EMAIL, email)
                .apply()
            val session = AuthSession(email)
            _session.value = session
            Either.Right(session)
        }
    } catch (t: Throwable) {
        Either.Left(AppError.Unknown(t))
    }

    override fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_EMAIL).apply()
        _session.value = null
    }

    private companion object {
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_LOGGED_IN_EMAIL = "logged_in_email"
    }
}
