package com.example.visionguide.domain.model

data class DetectionBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

data class Detection(
    val label: String,
    val labelEn: String,
    val score: Float,
    val box: DetectionBox
)

data class DetectionResult(
    val detections: List<Detection>,
    val mode: String,
    val modeName: String,
    val success: Boolean
)

data class Segment(
    val label: String,
    val score: Float,
    val mask: List<List<Int>>
)

data class SegmentResult(
    val results: List<Segment>
)

