package com.example.visionguide.data.mapper

import com.example.visionguide.data.network.DetectionItem
import com.example.visionguide.data.network.DetectionResponse
import com.example.visionguide.domain.model.Detection
import com.example.visionguide.domain.model.DetectionBox
import com.example.visionguide.domain.model.DetectionResult

fun DetectionItem.toDomain(): Detection {
    val bx = box
    val boxDomain = if (bx.size >= 4) {
        DetectionBox(
            x1 = bx[0],
            y1 = bx[1],
            x2 = bx[2],
            y2 = bx[3]
        )
    } else {
        DetectionBox(0f, 0f, 0f, 0f)
    }
    return Detection(
        label = label,
        score = score,
        box = boxDomain
    )
}

fun DetectionResponse.toDomain(): DetectionResult =
    DetectionResult(detections = detections.map { it.toDomain() })

fun com.example.visionguide.data.network.SegmentItem.toDomain(): com.example.visionguide.domain.model.Segment =
    com.example.visionguide.domain.model.Segment(
        label = label,
        score = score,
        mask = mask
    )

fun com.example.visionguide.data.network.SegmentResponse.toDomain(): com.example.visionguide.domain.model.SegmentResult =
    com.example.visionguide.domain.model.SegmentResult(results = results.map { it.toDomain() })

