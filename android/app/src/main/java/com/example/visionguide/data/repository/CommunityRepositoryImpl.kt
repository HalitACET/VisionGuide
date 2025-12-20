package com.example.visionguide.data.repository

import com.example.visionguide.data.network.ApiService
import com.example.visionguide.data.network.CreatePostRequest
import com.example.visionguide.data.network.PostResponse
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.repository.CommunityRepository
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException
import javax.inject.Inject

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class CommunityRepositoryImpl @Inject constructor(
    private val api: ApiService
) : CommunityRepository {

    override suspend fun getPosts(): Either<AppError, List<PostResponse>> = try {
        val posts = api.getPosts()
        Either.Right(posts)
    } catch (t: Throwable) {
        Either.Left(mapError(t))
    }

    override suspend fun createPost(title: String, content: String, audioUrl: String?): Either<AppError, PostResponse> = try {
        val post = api.createPost(CreatePostRequest(title, content, author = "AndroidUser", audio_url = audioUrl))
        Either.Right(post)
    } catch (t: Throwable) {
        Either.Left(mapError(t))
    }

    override suspend fun uploadAudio(file: java.io.File): Either<AppError, String> = try {
        val mediaType = "audio/mpeg".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = api.uploadAudio(body)
        Either.Right(response.url)
    } catch (t: Throwable) {
        Either.Left(mapError(t))
    }

    private fun mapError(t: Throwable): AppError {
        return when (t) {
            is SocketTimeoutException -> AppError.Timeout
            is IOException -> AppError.Network
            is HttpException -> {
                val code = t.code()
                if (code == 401) AppError.Unauthorized else AppError.Server(code, t.message())
            }
            else -> AppError.Unknown(t)
        }
    }
}
