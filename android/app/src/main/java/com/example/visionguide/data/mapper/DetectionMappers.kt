package com.example.visionguide.data.mapper

import com.example.visionguide.data.network.DetectionItem
import com.example.visionguide.data.network.DetectionResponse
import com.example.visionguide.domain.model.Detection
import com.example.visionguide.domain.model.DetectionBox
import com.example.visionguide.domain.model.DetectionResult

fun DetectionItem.toDomain(): Detection {
    return Detection(
        label = label,
        labelEn = label_en,
        score = score,
        box = DetectionBox(box.x1, box.y1, box.x2, box.y2)
    )
}

fun DetectionResponse.toDomain(): DetectionResult =
    DetectionResult(
        detections = detections.map { it.toDomain() },
        mode = mode,
        modeName = mode_name,
        success = success
    )

fun com.example.visionguide.data.network.SegmentItem.toDomain(): com.example.visionguide.domain.model.Segment =
    com.example.visionguide.domain.model.Segment(
        label = label,
        score = score,
        mask = mask
    )

fun com.example.visionguide.data.network.SegmentResponse.toDomain(): com.example.visionguide.domain.model.SegmentResult =
    com.example.visionguide.domain.model.SegmentResult(results = results.map { it.toDomain() })

