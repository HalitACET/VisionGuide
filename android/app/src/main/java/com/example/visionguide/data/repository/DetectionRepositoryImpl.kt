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
    override suspend fun detectObjects(image: String): Either<AppError, DetectionResult> = try {
        val resp = api.detect(ImageUploadRequest(image = image))
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

    override suspend fun segment(base64Image: String, prompt: String): Either<AppError, com.example.visionguide.domain.model.SegmentResult> = try {
        val resp = api.segment(com.example.visionguide.data.network.SegmentRequest(image = base64Image, prompt = prompt))
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

    override suspend fun readText(image: String): Either<AppError, String> = try {
        val resp = api.ocr(ImageUploadRequest(image = image))
        Either.Right(resp.text)
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

    override suspend fun analyze(image: String, feature: String): Either<AppError, String> = try {
        val resp = api.analyze(com.example.visionguide.data.network.AnalyzeRequest(image = image, feature = feature))
        Either.Right(resp.result)
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
