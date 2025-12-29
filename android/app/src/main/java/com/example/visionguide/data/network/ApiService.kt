package com.example.visionguide.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): AuthResponse

    @POST("/detect")
    suspend fun detect(@Body body: DetectionRequest): DetectionResponse

    @POST("segment")
    suspend fun segment(@Body request: SegmentRequest): SegmentResponse

    @POST("ocr")
    suspend fun ocr(@Body request: ImageUploadRequest): OcrResponse

    @POST("analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse

    @POST("describe_scene")
    suspend fun describeScene(@Body request: DescribeSceneRequest): DescribeSceneResponse

    @POST("ask_gemini")
    suspend fun askGemini(@Body request: AskGeminiRequest): AskGeminiResponse

    @GET("posts")
    suspend fun getPosts(): List<PostResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): PostResponse

    @GET("posts/{postId}")
    suspend fun getPost(@retrofit2.http.Path("postId") postId: Int): PostResponse

    @GET("posts/{postId}/comments")
    suspend fun getComments(@retrofit2.http.Path("postId") postId: Int): List<CommentResponse>

    @POST("posts/{postId}/comments")
    suspend fun createComment(@retrofit2.http.Path("postId") postId: Int, @Body request: CommentCreateRequest): CommentResponse

    @retrofit2.http.Multipart
    @POST("upload-audio")
    suspend fun uploadAudio(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): AudioUploadResponse
}
