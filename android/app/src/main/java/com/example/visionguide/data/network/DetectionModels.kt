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
    val total_detections: Int? = null  // Backend'den gelen total_detections alanı (opsiyonel)
)
