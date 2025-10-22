package com.example.visionguide.data.repository


import com.example.visionguide.network.ApiService
import com.example.visionguide.network.DetectionResponse
import com.example.visionguide.network.ImageUploadRequest

class DetectionRepositoryImpl(
    private val api: ApiService
) : DetectionRepository {
    override suspend fun analyze(base64Image: String): Result<DetectionResponse> = try {
        val resp = api.detect(ImageUploadRequest(image = base64Image))
        Result.success(resp)
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
