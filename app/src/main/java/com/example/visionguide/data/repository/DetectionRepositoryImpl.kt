package com.example.visionguide.data.repository

import com.example.visionguide.data.network.ApiService
import com.example.visionguide.data.network.ImageUploadRequest
import com.example.visionguide.data.mapper.toDomain
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.model.DetectionResult
import com.example.visionguide.domain.repository.DetectionRepository
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

class DetectionRepositoryImpl(
    private val api: ApiService
) : DetectionRepository {
    override suspend fun analyze(base64Image: String): Either<AppError, DetectionResult> = try {
        val resp = api.detect(ImageUploadRequest(image = base64Image))
        Either.Right(resp.toDomain())
    } catch (t: Throwable) {
        val err = when (t) {
            is SocketTimeoutException -> AppError.Timeout
            is IOException -> AppError.Network
            is HttpException -> {
                val code = t.code()
                if (code == 401) AppError.Unauthorized else AppError.Server(code, t.message())
            }
            else -> AppError.Unknown(t)
        }
        Either.Left(err)
    }
}
