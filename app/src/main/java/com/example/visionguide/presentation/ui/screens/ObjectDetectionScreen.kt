package com.example.visionguide.presentation.ui.screens

import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.visionguide.presentation.viewmodel.ObjectDetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObjectDetectionScreen(
    viewModel: ObjectDetectionViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance.language = Locale.getDefault()
            }
        }
        tts = instance
        onDispose {
            instance.stop()
            instance.shutdown()
            tts = null
        }
    }

    val state = viewModel.state

    LaunchedEffect(state) {
        state.collect { s ->
            s.lastLabel?.let { label ->
                tts?.speak(label, TextToSpeech.QUEUE_FLUSH, null, "visionguide_tts")
            }
        }
    }

    if (!cameraPermission.status.isGranted) {
        LaunchedEffect(Unit) { cameraPermission.launchPermissionRequest() }
        Box(Modifier.fillMaxSize())
        return
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = androidx.camera.view.PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analysisExecutor = Executors.newSingleThreadExecutor()
                    val analyzer = viewModel.analyzer { scope }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also {
                            it.setAnalyzer(analysisExecutor, analyzer)
                        }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            analysis
                        )
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
