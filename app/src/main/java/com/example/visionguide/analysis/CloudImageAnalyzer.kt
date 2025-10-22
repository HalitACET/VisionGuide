package com.example.visionguide.analysis

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.visionguide.domain.AnalyzeImageUseCase
import com.example.visionguide.network.DetectionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class CloudImageAnalyzer(
    private val scope: CoroutineScope,
    private val useCase: AnalyzeImageUseCase,
    private val onResult: (Result<DetectionResponse>) -> Unit,
    private val throttleMs: Long = 2000L
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private var lastSent = 0L

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastSent < throttleMs || isProcessing.get()) {
            image.close()
            return
        }
        isProcessing.set(true)
        lastSent = now

        val base64 = image.toBase64()
        image.close()

        scope.launch(Dispatchers.IO) {
            val res = if (base64 != null) useCase(base64) else Result.failure(IllegalStateException("encode"))
            onResult(res)
            isProcessing.set(false)
        }
    }
}

private fun ImageProxy.toBase64(quality: Int = 80): String? {
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
}
