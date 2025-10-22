package com.example.visionguide.domain.usecase

import com.example.visionguide.data.repository.DetectionRepository
import com.example.visionguide.data.network.DetectionResponse

class AnalyzeImageUseCase(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(base64Image: String): Result<DetectionResponse> =
        repository.analyze(base64Image)
}
