package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.SegmentResult
import com.example.visionguide.domain.repository.DetectionRepository
import javax.inject.Inject

class SegmentObjectsUseCase @Inject constructor(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(base64Image: String, prompt: String): Either<AppError, SegmentResult> {
        return repository.segment(base64Image, prompt)
    }
}
