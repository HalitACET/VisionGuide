package com.example.visionguide.domain.model

data class AuthSession(
    val email: String,
    val accessToken: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
