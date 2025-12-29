package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.repository.DetectionRepository
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.DetectionResult

class AnalyzeImageUseCase(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(base64Image: String, mode: String = "E"): Either<AppError, DetectionResult> =
        repository.detectObjects(base64Image, mode)
}
