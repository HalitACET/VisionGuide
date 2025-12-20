package com.example.visionguide.data.network

data class PostResponse(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val audio_url: String? = null
)

data class CreatePostRequest(
    val title: String,
    val content: String,
    val author: String = "Anonymous",
    val audio_url: String? = null
)

data class AudioUploadResponse(
    val url: String
)

data class CommentResponse(
    val id: Int,
    val post_id: Int,
    val content: String,
    val author: String,
    val created_at: String
)

data class CommentCreateRequest(
    val content: String,
    val author: String = "Anonymous"
)
