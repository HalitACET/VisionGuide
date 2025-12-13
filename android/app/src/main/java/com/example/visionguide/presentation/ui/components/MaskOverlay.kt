package com.example.visionguide.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.visionguide.domain.model.Segment

@Composable
fun MaskOverlay(
    segments: List<Segment>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        segments.forEach { segment ->
            val path = Path()
            if (segment.mask.isNotEmpty()) {
                // Move to first point
                val firstPoint = segment.mask.first()
                if (firstPoint.size >= 2) {
                    path.moveTo(firstPoint[0].toFloat(), firstPoint[1].toFloat())
                }

                // Line to other points
                for (i in 1 until segment.mask.size) {
                    val point = segment.mask[i]
                    if (point.size >= 2) {
                        path.lineTo(point[0].toFloat(), point[1].toFloat())
                    }
                }
                path.close()

                // Draw Fill
                drawPath(
                    path = path,
                    color = Color.Green.copy(alpha = 0.3f),
                    style = Fill
                )

                // Draw Stroke
                drawPath(
                    path = path,
                    color = Color.Green,
                    style = Stroke(width = 5f)
                )
            }
        }
    }
}
