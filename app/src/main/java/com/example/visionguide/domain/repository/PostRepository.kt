package com.example.visionguide.domain.repository

import com.example.visionguide.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    suspend fun addPost(post: Post)
}
