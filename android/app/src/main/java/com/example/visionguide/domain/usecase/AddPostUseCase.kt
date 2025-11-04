package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.model.Post
import com.example.visionguide.domain.repository.PostRepository
import javax.inject.Inject

class AddPostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(post: Post) = repository.addPost(post)
}
