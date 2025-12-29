package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.model.Post
import com.example.visionguide.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<List<Post>> = repository.getPosts()
}
