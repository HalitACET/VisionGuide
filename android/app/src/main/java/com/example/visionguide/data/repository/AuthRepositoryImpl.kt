package com.example.visionguide.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.visionguide.data.network.ApiService
import com.example.visionguide.data.network.AuthLoginRequest
import com.example.visionguide.data.network.AuthRegisterRequest
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
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val api: ApiService
) : AuthRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("vision_guide_auth", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow<AuthSession?>(
        prefs.getString(KEY_LOGGED_IN_EMAIL, null)?.let { email ->
            val token = prefs.getString(KEY_ACCESS_TOKEN, null)
            AuthSession(email = email, accessToken = token)
        }
    )
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override suspend fun login(email: String, password: String): Either<AppError, AuthSession> = try {
        val resp = api.login(AuthLoginRequest(email = email, password = password))
        prefs.edit()
            .putString(KEY_LOGGED_IN_EMAIL, resp.email)
            .putString(KEY_ACCESS_TOKEN, resp.access_token)
            .apply()
        val session = AuthSession(email = resp.email, accessToken = resp.access_token)
        _session.value = session
        Either.Right(session)
    } catch (t: Throwable) {
        Either.Left(mapError(t))
    }

    override suspend fun register(email: String, password: String): Either<AppError, AuthSession> = try {
        val resp = api.register(AuthRegisterRequest(email = email, password = password))
        prefs.edit()
            .putString(KEY_LOGGED_IN_EMAIL, resp.email)
            .putString(KEY_ACCESS_TOKEN, resp.access_token)
            .apply()
        val session = AuthSession(email = resp.email, accessToken = resp.access_token)
        _session.value = session
        Either.Right(session)
    } catch (t: Throwable) {
        Either.Left(mapError(t))
    }

    override fun logout() {
        prefs.edit()
            .remove(KEY_LOGGED_IN_EMAIL)
            .remove(KEY_ACCESS_TOKEN)
            .apply()
        _session.value = null
    }

    private fun mapError(t: Throwable): AppError {
        return when (t) {
            is SocketTimeoutException -> AppError.Timeout
            is IOException -> AppError.Network
            is HttpException -> {
                val code = t.code()
                if (code == 401) AppError.Unauthorized else AppError.Server(code, t.message())
            }
            else -> AppError.Unknown(t)
        }
    }

    private companion object {
        private const val KEY_LOGGED_IN_EMAIL = "logged_in_email"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
