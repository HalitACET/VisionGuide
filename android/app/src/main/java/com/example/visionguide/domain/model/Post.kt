package com.example.visionguide.domain.model

data class Post(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val replyCount: Int = 0,
    val lastReplyAuthor: String = "",
    val createdAt: Long = 0L
)
