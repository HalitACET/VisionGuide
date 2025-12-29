package com.example.visionguide.domain.common

sealed class AppError {
    object Network : AppError()
    object Timeout : AppError()
    object Unauthorized : AppError()
    data class Server(val code: Int, val message: String? = null) : AppError()
    data class Unknown(val cause: Throwable? = null) : AppError()
}

