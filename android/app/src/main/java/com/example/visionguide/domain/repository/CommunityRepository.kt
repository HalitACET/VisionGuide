package com.example.visionguide.domain.repository

import com.example.visionguide.data.network.PostResponse
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either

interface CommunityRepository {
    suspend fun getPosts(): Either<AppError, List<PostResponse>>
    suspend fun createPost(title: String, content: String, audioUrl: String? = null): Either<AppError, PostResponse>
    suspend fun uploadAudio(file: java.io.File): Either<AppError, String>
}
