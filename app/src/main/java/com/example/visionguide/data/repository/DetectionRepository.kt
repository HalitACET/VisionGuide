package com.example.visionguide.data.repository

import com.example.visionguide.network.DetectionResponse

interface DetectionRepository {
    suspend fun analyze(base64Image: String): Result<DetectionResponse>
}
