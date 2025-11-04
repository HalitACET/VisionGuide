package com.example.visionguide.domain.repository

import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.DetectionResult

interface DetectionRepository {
    suspend fun analyze(base64Image: String): Either<AppError, DetectionResult>
}
