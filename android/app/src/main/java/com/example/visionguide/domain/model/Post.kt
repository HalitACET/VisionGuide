package com.example.visionguide.domain.model

data class Post(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val replyCount: Int = 0,
    val lastReplyAuthor: String = "",
    val createdAt: Long = 0L,
    val audioUrl: String? = null
)
