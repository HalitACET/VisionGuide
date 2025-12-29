package com.example.visionguide.presentation.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.ObjectDetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: ObjectDetectionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val state by viewModel.state.collectAsState()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = androidx.compose.ui.platform.LocalView.current
    
    // Analyzer initialization
    val analyzer = remember { viewModel.analyzer(enableAutoAnalysis = false) }

    // TTS Initialization
    val context = LocalContext.current
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }

    // Cleanup TTS on dispose
    DisposableEffect(Unit) {
        // Accessibility Announcement on Entry
        ttsManager.speak("Para tanıma modu. Parayı tanıtmak için ekrana bir kez dokunun.")
        onDispose {
            ttsManager.shutdown()
        }
    }

    var showResultDialog by remember { mutableStateOf(false) }

    // Show dialog and speak when we have a result
    LaunchedEffect(state.lastLabel) {
        if (!state.lastLabel.isNullOrEmpty() && !state.isLoading) {
            showResultDialog = true
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            ttsManager.speak(state.lastLabel!!)
        }
    }
    
    // Error Handling with TTS
    LaunchedEffect(state.error) {
        if (state.error != null) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            ttsManager.speak("Hata oluştu. ${state.error}")
        }
    }

    // Interaction Handler
    val onCaptureTriggered = {
        if (!state.isLoading) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
            ttsManager.speak("Para taranıyor, lütfen bekleyin...")
            viewModel.detectCurrency()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onCaptureTriggered() }
                )
            }
            .semantics {
                contentDescription = "Para Tanıma Ekranı. Fotoğraf çekmek için ekrana dokunun."
            }
    ) {
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            CameraPreviewWithAnalysisForCurrency(
                analyzer = analyzer,
                modifier = Modifier.fillMaxSize()
            )

        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Kamera İzni Ver")
                }
            }
        }

        // Overlay for Loading
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("İşleniyor...", color = Color.White, fontSize = 24.sp)
                }
            }
        }

        // Visual Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), 
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ExtendedFloatingActionButton(
                onClick = onCaptureTriggered,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                icon = { Icon(Icons.Default.AttachMoney, null) },
                text = { Text("PARA TANI", fontWeight = FontWeight.Black) },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }

    // Accessible Error Dialog
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Hata") },
            text = { Text(state.error!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("TAMAM")
                }
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
            textContentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) }
        )
    }

    // Accessible Result Dialog
    if (showResultDialog && state.lastLabel != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Sonuç") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = state.lastLabel!!,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showResultDialog = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("KAPAT", fontSize = 18.sp)
                }
            }
        )
    }
}

@Composable
fun CameraPreviewWithAnalysisForCurrency(
    analyzer: com.example.visionguide.presentation.analysis.CloudImageAnalyzer,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context).get()
        
        val preview = androidx.camera.core.Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        
        imageAnalysis.setAnalyzer(androidx.core.content.ContextCompat.getMainExecutor(context), analyzer)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
