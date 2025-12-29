package com.example.visionguide.presentation.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme
import com.example.visionguide.presentation.util.SpeechState
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable

fun HomeScreen(
    onNavigateCommunity: () -> Unit = {},
    onNavigateObjectDetection: () -> Unit = {},
    onNavigateTextReader: () -> Unit = {},
    onNavigateCurrency: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showMore by remember { mutableStateOf(false) }
    val audioPermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val speechManager = remember { com.example.visionguide.presentation.util.SpeechRecognizerManager(context) }
    val speechState by speechManager.speechState.collectAsState()
    
    // TTS Manager
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Handle Voice Commands
    LaunchedEffect(speechState) {
        if (speechState is SpeechState.Result) {
            val command = (speechState as SpeechState.Result).text.lowercase()

            if (command.contains("nesne") || command.contains("object")) {
                onNavigateObjectDetection()
            } else if (command.contains("topluluk") || command.contains("community")) {
                onNavigateCommunity()
            } else if (command.contains("oku") || command.contains("read") || command.contains("metin")) {
                onNavigateTextReader()
            } else if (command.contains("para") || command.contains("money")) {
                onNavigateCurrency()
            }
        }
    }

    val onStartListening = {
        if (audioPermissionState.status == PermissionStatus.Granted) {
            ttsManager.speak("Dinliyorum")
            speechManager.startListening()
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onStartListening() }
                )
            }
    ) {
        CameraPermissionGate(
            permissionState = cameraPermissionState
        ) {
            CameraPreview(modifier = Modifier.fillMaxSize())
        }

        // Microphone FAB
        FloatingActionButton(
            onClick = onStartListening,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp), // Positioned above the bottom bar
            containerColor = if (speechState is SpeechState.Listening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = if (speechState is SpeechState.Listening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Sesli Komut. Konuşmak için basılı tutun.",
                tint = if (speechState is SpeechState.Listening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
        }
        
        // DEBUG: Show recognized text
        if (speechState is SpeechState.Result) {
            Text(
                text = "Algılanan: ${(speechState as SpeechState.Result).text}",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (speechState is SpeechState.Error) {
             Text(
                text = "Hata: ${(speechState as SpeechState.Error).message}",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        FloatingControlBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            onObjectDetect = onNavigateObjectDetection,
            onCommunity = onNavigateCommunity,
            onMoreClick = { showMore = true }
        )
    }

    if (showMore) {
        ModalBottomSheet(
            onDismissRequest = { showMore = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            MoreCategoriesSheetContent(
                onTextReadClick = onNavigateTextReader,
                onSettingsClick = onNavigateToSettings,
                onCategoryClick = { item ->
                    if (item.label == "Para Tanıma") {
                        showMore = false
                        onNavigateCurrency()
                    }
                    /* TODO: Other categories */
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermissionGate(
    permissionState: PermissionState,
    content: @Composable () -> Unit
) {
    when (val status = permissionState.status) {
        is PermissionStatus.Granted -> content()
        is PermissionStatus.Denied -> PermissionDeniedContent(
            rationale = status.shouldShowRationale,
            onRequest = { permissionState.launchPermissionRequest() }
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    rationale: Boolean,
    onRequest: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val msg = if (rationale)
            "Kamera izni gerekli. Lütfen izin verin."
        else
            "Kamera izni verilmedi. İzin istemek için dokunun veya Ayarlar'dan verin."
        Text(msg)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRequest,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) { Text("İzin İste") }
        if (!rationale) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openAppSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) { Text("Ayarları Aç") }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
}

@Composable
private fun FloatingControlBar(
    modifier: Modifier = Modifier,
    onObjectDetect: () -> Unit,
    onCommunity: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        modifier = modifier.shadow(12.dp, RoundedCornerShape(28.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(icon = Icons.Default.Visibility, label = "Nesne", onClick = onObjectDetect)
            ControlButton(icon = Icons.Default.Groups, label = "Topluluk", onClick = onCommunity)
            ControlButton(icon = Icons.Default.MoreHoriz, label = "Diğer", onClick = onMoreClick)
        }
    }
}

@Composable
private fun ControlButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(80.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MoreCategoriesSheetContent(
    onTextReadClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCategoryClick: (ActionItem) -> Unit
) {
    val items = remember { buildMoreItems() }
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Diğer Özellikler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SheetButton(item = ActionItem(Icons.Default.TextFields, "Metin Oku"), onClick = onTextReadClick, modifier = Modifier.weight(1f))
            SheetButton(item = ActionItem(Icons.Default.Settings, "Ayarlar"), onClick = onSettingsClick, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items) { item ->
                SheetButton(item = item, onClick = { onCategoryClick(item) })
            }
        }
    }
}

@Composable
private fun SheetButton(item: ActionItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
        ) {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = item.label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(cameraSelector) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            CameraXPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        )
    }
    AndroidView(factory = { previewView.apply { this.scaleType = scaleType } }, modifier = modifier)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({ continuation.resume(future.get()) }, ContextCompat.getMainExecutor(this))
    }
}

private data class ActionItem(val icon: ImageVector, val label: String)

private fun buildMoreItems(): List<ActionItem> = listOf(
    ActionItem(Icons.Default.ColorLens, "Renk Tanıma"),
    ActionItem(Icons.Default.AttachMoney, "Para Tanıma"),
    ActionItem(Icons.Default.QrCode, "Barkod Okuma"),
    ActionItem(Icons.Default.Face, "Yüz Tanıma")
)

@Preview
@Composable
private fun HomeScreenPreview() {
    VisionGuideTheme {
        HomeScreen()
    }
}