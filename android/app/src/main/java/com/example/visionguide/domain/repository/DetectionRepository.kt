package com.example.visionguide.domain.repository

import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.DetectionResult

interface DetectionRepository {
    suspend fun detectObjects(image: String): Either<AppError, DetectionResult>
    suspend fun segment(image: String, prompt: String): Either<AppError, com.example.visionguide.domain.model.SegmentResult>
    suspend fun readText(image: String): Either<AppError, String>
    suspend fun analyze(image: String, feature: String): Either<AppError, String>
}
