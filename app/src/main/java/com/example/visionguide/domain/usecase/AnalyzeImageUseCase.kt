package com.example.visionguide.domain.usecase

import com.example.visionguide.data.DetectionRepository
import com.example.visionguide.network.DetectionResponse

class AnalyzeImageUseCase(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(base64Image: String): Result<DetectionResponse> =
        repository.analyze(base64Image)
}
