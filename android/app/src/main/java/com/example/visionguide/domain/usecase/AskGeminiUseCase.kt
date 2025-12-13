package com.example.visionguide.domain.usecase

import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.repository.DetectionRepository
import javax.inject.Inject

class AskGeminiUseCase @Inject constructor(
    private val repository: DetectionRepository
) {
    suspend operator fun invoke(image: String, prompt: String): Either<AppError, String> {
        return repository.askGemini(image, prompt)
    }
}
