package com.example.visionguide.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/detect")
    suspend fun detect(@Body body: ImageUploadRequest): DetectionResponse
}
