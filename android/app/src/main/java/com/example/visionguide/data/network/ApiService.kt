package com.example.visionguide.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/detect")
    suspend fun detect(@Body body: ImageUploadRequest): DetectionResponse

    @POST("segment")
    suspend fun segment(@Body request: SegmentRequest): SegmentResponse

    @POST("ocr")
    suspend fun ocr(@Body request: ImageUploadRequest): OcrResponse

    @POST("analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse

    @GET("posts")
    suspend fun getPosts(): List<PostResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): PostResponse
}
