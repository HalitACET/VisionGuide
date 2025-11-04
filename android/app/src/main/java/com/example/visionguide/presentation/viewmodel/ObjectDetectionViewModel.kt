package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.usecase.AnalyzeImageUseCase
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.common.AppError
import com.example.visionguide.domain.model.DetectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class DetectionUiState(
    val lastLabel: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ObjectDetectionViewModel @Inject constructor(
    private val analyzeImage: AnalyzeImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetectionUiState())
    val state: StateFlow<DetectionUiState> = _state

    fun onDetectionResult(result: Either<AppError, DetectionResult>) {
        when (result) {
            is Either.Right -> {
                // Debug: Tüm detection'ları logla
                if (result.value.detections.isNotEmpty()) {
                    result.value.detections.forEach { detection ->
                        android.util.Log.d("VisionGuide", "Detection: ${detection.label}, Score: ${detection.score}")
                    }
                }
                // En yüksek score'lu detection'ı seç, ancak:
                // 1. Score 0.6'dan yüksek olmalı
                // 2. "unknown" label'ını filtrele
                val filteredDetections = result.value.detections
                    .filter { it.score > 0.6f && it.label != "unknown" }
                
                val bestDetection = if (filteredDetections.isNotEmpty()) {
                    filteredDetections.maxByOrNull { it.score }
                } else {
                    // Eğer yüksek skorlu detection yoksa, en yüksek olanı al ama "unknown" değilse
                    result.value.detections.filter { it.label != "unknown" }.maxByOrNull { it.score }
                }
                
                val label = bestDetection?.label
                android.util.Log.d("VisionGuide", "Selected label: $label (score: ${bestDetection?.score})")
                _state.update { it.copy(lastLabel = label, isLoading = false, error = null) }
            }
            is Either.Left -> {
                val message = when (result.value) {
                    AppError.Network -> "Ağ bağlantı hatası"
                    AppError.Timeout -> "Zaman aşımı"
                    AppError.Unauthorized -> "Yetkisiz erişim"
                    is AppError.Server -> "Sunucu hatası ${'$'}{result.value.code}"
                    is AppError.Unknown -> result.value.cause?.message ?: "Bilinmeyen hata"
                }
                _state.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun analyzer(scopeProvider: () -> kotlinx.coroutines.CoroutineScope): com.example.visionguide.presentation.analysis.CloudImageAnalyzer {
        return com.example.visionguide.presentation.analysis.CloudImageAnalyzer(
            scope = scopeProvider(),
            useCase = analyzeImage,
            onResult = { res -> onDetectionResult(res) }
        )
    }
}
