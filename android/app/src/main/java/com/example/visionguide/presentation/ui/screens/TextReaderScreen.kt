package com.example.visionguide.presentation.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.ObjectDetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.example.visionguide.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TextReaderScreen(
    viewModel: ObjectDetectionViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val state by viewModel.state.collectAsState()
    val speechRate by settingsViewModel.speechRate.collectAsState()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // Analyzer initialization
    val analyzer = remember { viewModel.analyzer { scope } }

    // TTS Initialization
    val context = LocalContext.current
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }

    // Cleanup TTS on dispose
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    var showResultDialog by remember { mutableStateOf(false) }

    // Show dialog and speak when we have a result
    LaunchedEffect(state.lastLabel) {
        if (!state.lastLabel.isNullOrEmpty() && !state.isLoading) {
            showResultDialog = true
            ttsManager.setSpeechRate(speechRate)
            ttsManager.speak(state.lastLabel!!)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            CameraPreview(
                modifier = Modifier.fillMaxSize()
            )
            
            // Camera Analysis Setup (Bind analyzer to lifecycle)
            // Note: CameraPreview in HomeScreen handles binding, but here we need to bind the analyzer use case
            // We might need to duplicate the CameraPreview logic or make it reusable with analyzer support.
            // For simplicity, reusing the CameraPreview from HomeScreen but we need to attach the analyzer.
            // Since CameraPreview in HomeScreen is simple, let's use a custom one here or update the shared one.
            // Actually, let's just use the same CameraPreview but we need to pass the analyzer to it?
            // The current CameraPreview in HomeScreen doesn't support analysis.
            // Let's implement a local CameraPreviewWithAnalysis here for now.
            
            CameraPreviewWithAnalysis(
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Capture Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    // Trigger OCR
                    // We can reuse onVoiceCommand("oku") for now as it triggers the exact logic we need
                    viewModel.onVoiceCommand("oku")
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Metni Oku",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (showResultDialog && state.lastLabel != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Okunan Metin") },
            text = {
                Column {
                    Text(
                        text = state.lastLabel!!,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
fun CameraPreviewWithAnalysis(
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
