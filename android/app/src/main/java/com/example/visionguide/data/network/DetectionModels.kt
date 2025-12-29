package com.example.visionguide.data.network

data class ImageUploadRequest(
    val image: String
)

data class DetectionRequest(
    val image: String,
    val mode: String = "E"
)

data class DetectionResponse(
    val success: Boolean,
    val mode: String,
    val mode_name: String,
    val detections: List<DetectionItem>,
    val image_width: Int,
    val image_height: Int,
    val processing_time_ms: Double
)

data class DetectionItem(
    val label: String,
    val label_en: String,
    val score: Float,
    val box: BoundingBox,
    val box_pixels: BoundingBox
)

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
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
