package com.example.visionguide.data.network

data class ImageUploadRequest(
    val image: String
)

data class DetectionItem(
    val label: String,
    val score: Float,
    val box: List<Float>,
    val position_desc: String? = null,
    val proximity_desc: String? = null
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

data class DescribeSceneRequest(
    val image: String
)

data class DescribeSceneResponse(
    val description: String
)

data class AskGeminiRequest(
    val image: String,
    val prompt: String
)

data class AskGeminiResponse(
    val answer: String
)
