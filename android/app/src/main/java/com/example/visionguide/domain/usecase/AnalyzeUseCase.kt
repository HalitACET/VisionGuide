package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.repository.DetectionRepository
import javax.inject.Inject

class AnalyzeUseCase @Inject constructor(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(base64Image: String, feature: String): Either<AppError, String> {
        return repository.analyze(base64Image, feature)
    }
}
