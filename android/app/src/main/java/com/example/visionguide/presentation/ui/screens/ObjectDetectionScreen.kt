package com.example.visionguide.presentation.ui.screens

import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.TextSnippet



import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
    val view = LocalView.current

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    // Initialize analyzer with auto-analysis DISABLED for manual "Tap to Detect" mode
    // Note: We access this inside the AndroidView, but we can init it here or let the view do it. 
    // To ensure consistency, we should force it to false in the View logic or here. 
    // The previous logic inside AndroidView called viewModel.analyzer(). We will update that call.

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var lastSpokenLabel by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Turkish first, then English, then Default
                val result = engine?.setLanguage(Locale("tr", "TR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                     engine?.setLanguage(Locale.US)
                }
                ttsReady = true
                // Announcement on entry
                engine?.speak("Nesne tanıma modu. Tanımak için ekrana bir kez dokunun veya sesli komut söylemek için iki kez dokunun.", TextToSpeech.QUEUE_FLUSH, null, "intro")
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
        // Label varsa ve değişmişse konuş. 
        // Manual modda her click yeni bir "loading" -> "result" akışı yaratır, bu yüzden lastLabel değişmese bile 
        // kullanıcı tekrar basarsa okumalıyız. Ancak state flow olduğu için değişim gerekir.
        // ViewModel'de detectSingleObject çağrıldığında lastLabel = null yapıyoruz, bu yüzden her yeni sonuç değişim sayılır.
        if (label != null && ttsReady) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            tts?.speak("$label", TextToSpeech.QUEUE_FLUSH, null, "detection_result")
        }
    }
    
    // Error feedback
    LaunchedEffect(state.error) {
         if (state.error != null && ttsReady) {
             view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
             tts?.speak("Hata: ${state.error}", TextToSpeech.QUEUE_FLUSH, null, "error")
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

    // Interaction Handler
    val onDetectTriggered = {
        if (!state.isLoading) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
            // Announcement for processing start
            tts?.speak("Bakıyorum...", TextToSpeech.QUEUE_FLUSH, null, "processing")
            viewModel.detectSingleObject()
        }
    }

    val onStartListening = {
        if (audioPermission.status.isGranted) {
            if (ttsReady) {
                tts?.speak("Dinliyorum", TextToSpeech.QUEUE_ADD, null, "listening")
            }
            speechManager.startListening()
        } else {
            audioPermission.launchPermissionRequest()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(0.dp)
            // Full screen tap gesture
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onDetectTriggered() },
                    onDoubleTap = { onStartListening() }
                )
            }
            .semantics {
                contentDescription = "Nesne Tanıma Ekranı. Ne olduğunu öğrenmek için dokunun. Sesli komut için iki kez dokunun."
            }
    ) {
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
                    // Initialize with auto-analysis DISABLED
                    val analyzer = viewModel.analyzer(enableAutoAnalysis = false)

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


        // Mode Selector
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf("E" to "Ev", "S" to "Sokak", "M" to "Market")
            modes.forEach { (code, name) ->
                val isSelected = state.mode == code
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.setMode(code) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = name, style = MaterialTheme.typography.labelLarge, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // Loading Overlay
        if (state.isLoading) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                 CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        
        // Manual Trigger Button (Bottom Center) - Accessible styling
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), 
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
             horizontalAlignment = Alignment.CenterHorizontally
        ) {
              ExtendedFloatingActionButton(
                onClick = onDetectTriggered,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Filled.TextSnippet, null) }, // Using generic icon as placeholder
                text = { Text("NESNEYİ TANI", fontWeight = FontWeight.Black) },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        // Voice Command FAB (Bottom End) - Kept but made smaller/secondary visual priority or same
        FloatingActionButton(
            onClick = onStartListening,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp) // Adjusted position
        ) {
            val icon = if (speechState is SpeechState.Listening) {
                Icons.Filled.Mic
            } else {
                Icons.Filled.MicNone
            }
            Icon(
                imageVector = icon, 
                contentDescription = if (speechState is SpeechState.Listening) "Dinleniyor..." else "Sesli Komut"
            )
        }
        
        // Mask Overlay
        val segments = viewModel.segmentState.collectAsState().value
        com.example.visionguide.presentation.ui.components.MaskOverlay(
            segments = segments,
            modifier = Modifier.fillMaxSize()
        )

        // Show status text for voice commands
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
        
        // Result Overlay (Large Text for Low Vision)
         if (state.lastLabel != null && !state.isLoading) {
             Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = state.lastLabel!!,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
         }
    }
}
