package com.example.visionguide.presentation.ui.screens

import android.Manifest
import android.content.Context
import android.util.Size
import android.view.ViewGroup
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onReady: (ImageCapture) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Box(modifier = modifier) {
            CircularProgressIndicator()
        }
        return
    }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            val pv = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            previewView = pv
            pv
        },
        onRelease = {
            previewView = null
        }
    )

    LaunchedEffect(previewView) {
        val pv = previewView ?: return@LaunchedEffect
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder()
            .setTargetResolution(Size(pv.width.coerceAtLeast(720), pv.height.coerceAtLeast(1280)))
            .build().also {
                it.setSurfaceProvider(pv.surfaceProvider)
            }

        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(pv.display?.rotation ?: 0)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onReady(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ImageCapture.takePicture(
    context: Context,
    onError: (ImageCaptureException) -> Unit,
    onSaved: () -> Unit
) {
    val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(file).build()

    takePicture(
        output,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) = onError(exception)
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) = onSaved()
        }
    )
}
