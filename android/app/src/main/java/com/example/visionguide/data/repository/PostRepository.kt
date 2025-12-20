package com.example.visionguide.data.repository

import com.example.visionguide.domain.model.Post
import com.example.visionguide.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPostRepository @Inject constructor() : PostRepository {
    private val state = MutableStateFlow<List<Post>>(emptyList())

    override fun getPosts(): Flow<List<Post>> = state.asStateFlow()

    override suspend fun addPost(post: Post) {
        val newPost = if (post.id == 0) post.copy(id = generateId(), createdAt = System.currentTimeMillis()) else post
        state.value = listOf(newPost) + state.value
    }

    private fun generateId(): Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}
