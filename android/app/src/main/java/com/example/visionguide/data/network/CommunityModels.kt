package com.example.visionguide.data.network

data class PostResponse(
    val id: Int,
    val title: String,
    val content: String,
    val author: String
)

data class CreatePostRequest(
    val title: String,
    val content: String,
    val author: String = "Anonymous"
)
