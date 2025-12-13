package com.example.visionguide.data.network

data class ImageUploadRequest(
    val image: String
)

data class DetectionItem(
    val label: String,
    val score: Float,
    val box: List<Float>
)

data class DetectionResponse(
    val detections: List<DetectionItem>,
    val total_detections: Int? = null
)

data class SegmentRequest(
    val image: String,
    val prompt: String
)

data class SegmentItem(
    val label: String,
    val score: Float,
    val mask: List<List<Int>> // Simplified mask representation for now
)

data class SegmentResponse(
    val results: List<SegmentItem>
)

data class OcrResponse(
    val text: String
)

data class AnalyzeRequest(
    val image: String,
    val feature: String
)

data class AnalyzeResponse(
    val result: String
)
