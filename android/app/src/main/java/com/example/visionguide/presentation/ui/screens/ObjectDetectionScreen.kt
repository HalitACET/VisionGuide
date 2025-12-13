package com.example.visionguide.presentation.ui.screens

import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.visionguide.presentation.util.SpeechRecognizerManager
import com.example.visionguide.presentation.util.SpeechState
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
    var ttsReady by remember { mutableStateOf(false) }
    var lastSpokenLabel by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine?.setLanguage(Locale.US) // İngilizce label'lar için
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // İngilizce yoksa varsayılan dili kullan
                    engine?.setLanguage(Locale.getDefault())
                }
                ttsReady = true
            }
        }
        tts = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
            tts = null
            ttsReady = false
            engine = null
        }
    }

    val state = viewModel.state.collectAsState().value

    LaunchedEffect(state.lastLabel) {
        val label = state.lastLabel
        // Label varsa, değişmişse ve TTS hazırsa konuş
        if (label != null && label != lastSpokenLabel && ttsReady) {
            tts?.speak(label, TextToSpeech.QUEUE_FLUSH, null, "visionguide_tts")
            lastSpokenLabel = label
        }
    }

    if (!cameraPermission.status.isGranted) {
        LaunchedEffect(Unit) { cameraPermission.launchPermissionRequest() }
        Box(Modifier.fillMaxSize())
        return
    }

    val audioPermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val speechManager = remember { SpeechRecognizerManager(context) }
    val speechState = speechManager.speechState.collectAsState().value

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    LaunchedEffect(speechState) {
        if (speechState is SpeechState.Result) {
            viewModel.onVoiceCommand(speechState.text)
        }
    }

    val segments = viewModel.segmentState.collectAsState().value
    LaunchedEffect(segments) {
        if (segments.isNotEmpty()) {
            val label = segments.first().label
            android.widget.Toast.makeText(context, "Bulundu: $label", android.widget.Toast.LENGTH_LONG).show()
        }
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

        // Microphone Button
        FloatingActionButton(
            onClick = {
                if (audioPermission.status.isGranted) {
                    speechManager.startListening()
                } else {
                    audioPermission.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            val icon = if (speechState is SpeechState.Listening) {
                Icons.Filled.Mic
            } else {
                Icons.Filled.MicNone
            }
            Icon(
                imageVector = icon, 
                contentDescription = if (speechState is SpeechState.Listening) "Dinleniyor, durdurmak için dokunun" else "Konuşmak için dokunun"
            )
        }
        
        // Mask Overlay
        val segments = viewModel.segmentState.collectAsState().value
        com.example.visionguide.presentation.ui.components.MaskOverlay(
            segments = segments,
            modifier = Modifier.fillMaxSize()
        )

        // Show recognized text or status
        if (speechState is SpeechState.Listening || speechState is SpeechState.Speaking) {
             val text = if (speechState is SpeechState.Speaking && speechState.partialText.isNotEmpty()) {
                 speechState.partialText
             } else {
                 "Dinliyorum..."
             }
             
             Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                color = Color.White
            )
        }
    }
}
