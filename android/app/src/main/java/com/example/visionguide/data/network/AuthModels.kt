package com.example.visionguide.data.network

data class AuthRegisterRequest(
    val email: String,
    val password: String
)

data class AuthLoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val email: String
)
