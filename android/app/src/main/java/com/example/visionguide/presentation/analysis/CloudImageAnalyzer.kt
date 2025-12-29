package com.example.visionguide.presentation.analysis

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.visionguide.domain.usecase.AnalyzeImageUseCase
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.DetectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class CloudImageAnalyzer(
    private val scope: CoroutineScope,
    private val useCase: AnalyzeImageUseCase,
    private val onResult: (Either<AppError, DetectionResult>) -> Unit,
    private val throttleMs: Long = 2000L,
    private val enableAutoAnalysis: Boolean = true
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private var lastSent = 0L
    private var onCaptureNext: ((String?) -> Unit)? = null

    fun captureNextFrame(callback: (String?) -> Unit) {
        onCaptureNext = callback
    }

    var currentMode: String = "E"

    private val isAnalysisTriggered = AtomicBoolean(false)

    fun triggerOneShotAnalysis() {
        isAnalysisTriggered.set(true)
    }

    override fun analyze(image: ImageProxy) {
        val captureCallback = onCaptureNext
        if (captureCallback != null) {
            android.util.Log.d("VisionGuide", "Capturing frame for one-time request")
            val base64 = image.toBase64()
            image.close()
            
            if (base64 == null) {
                android.util.Log.e("VisionGuide", "toBase64 returned null!")
            }
            captureCallback(base64)
            onCaptureNext = null
            return
        }

        // Allow analysis if auto is enabled OR if a single shot was triggered
        if (!enableAutoAnalysis && !isAnalysisTriggered.get()) {
            image.close()
            return
        }

        val now = System.currentTimeMillis()
        // Throttle only if it's AUTO analysis. Manual trigger should bypass throttle check if possible, 
        // or we just respect throttle to avoid spam. Let's respect throttle for safety, but maybe reduce it?
        // Actually, for specific user tap, we should probably ignore throttle or reset it.
        // Let's just reset lastSent if triggered manually to ensure it runs?
        if (isAnalysisTriggered.get()) {
            // Bypass throttle for manual trigger
        } else if (now - lastSent < throttleMs || isProcessing.get()) {
            image.close()
            return
        }
        
        isProcessing.set(true)
        // Reset trigger flag immediately so we don't process multiple frames for one tap
        isAnalysisTriggered.set(false)
        lastSent = now

        android.util.Log.d("VisionGuide", "Analyzing frame (auto=${enableAutoAnalysis})")
        val base64 = image.toBase64()
        image.close()

        scope.launch(Dispatchers.IO) {
            val res = if (base64 != null) {
                useCase(base64, currentMode)
            } else {
                Either.Left(AppError.Unknown(IllegalStateException("encode")))
            }
            onResult(res)
            isProcessing.set(false)
        }
    }
}

private fun ImageProxy.toBase64(quality: Int = 80): String? {
    try {
        val yPlane = planes.getOrNull(0)?.buffer ?: return null
        val uPlane = planes.getOrNull(1)?.buffer ?: return null
        val vPlane = planes.getOrNull(2)?.buffer ?: return null

        val ySize = yPlane.remaining()
        val uSize = uPlane.remaining()
        val vSize = vPlane.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.get(nv21, 0, ySize)
        val uvPixelStride = planes[1].pixelStride
        val uvRowStride = planes[1].rowStride
        var pos = ySize
        val width = width
        val height = height
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val rowCount = height / 2
        val colCount = width / 2
        val rowLength = uvRowStride

        val vPos = vBuffer.position()
        val uPos = uBuffer.position()

        for (row in 0 until rowCount) {
            var vRowPos = vPos + row * rowLength
            var uRowPos = uPos + row * rowLength
            for (col in 0 until colCount) {
                // Ensure we don't go out of bounds
                if (pos >= nv21.size) break
                if (vRowPos >= vBuffer.limit() || uRowPos >= uBuffer.limit()) break
                
                nv21[pos++] = vBuffer.get(vRowPos)
                nv21[pos++] = uBuffer.get(uRowPos)
                vRowPos += uvPixelStride
                uRowPos += uvPixelStride
            }
        }

        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, width, height), quality, out)
        val jpeg = out.toByteArray()
        return Base64.encodeToString(jpeg, Base64.NO_WRAP)
    } catch (e: Exception) {
        android.util.Log.e("VisionGuide", "toBase64 error: ${e.message}", e)
        return null
    }
}
