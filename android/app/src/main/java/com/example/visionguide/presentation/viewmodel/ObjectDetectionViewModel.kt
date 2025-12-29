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
import kotlinx.coroutines.launch

data class DetectionUiState(
    val lastLabel: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mode: String = "E",
    val modeName: String = "Ev Modu"
)

@HiltViewModel
class ObjectDetectionViewModel @Inject constructor(
    private val analyzeImage: AnalyzeImageUseCase,
    private val segmentObjects: com.example.visionguide.domain.usecase.SegmentObjectsUseCase,
    private val readText: com.example.visionguide.domain.usecase.ReadTextUseCase,
    private val analyze: com.example.visionguide.domain.usecase.AnalyzeUseCase,
    private val askGemini: com.example.visionguide.domain.usecase.AskGeminiUseCase
) : ViewModel() {

    // ... (existing code)
    // ... (existing code)

    fun setMode(mode: String) {
        _state.update { it.copy(mode = mode) }
        analyzerInstance?.currentMode = mode
    }

    fun onVoiceCommand(text: String) {
        android.util.Log.d("VisionGuide", "Voice Command: $text")
        
        val command = text.lowercase(java.util.Locale.getDefault())
        
        if (command.contains("oku") || command.contains("read")) {
            // OCR Mode
            captureAndReadText()
        } else if (command.contains("para") || command.contains("money") || command.contains("lira")) {
            // Currency Mode
            detectCurrency()
        } else if (command.contains("renk") || command.contains("color") || command.contains("boya")) {
            // Color Mode
            analyzerInstance?.captureNextFrame { base64 ->
                if (base64 == null) return@captureNextFrame
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    when (val result = analyze(base64, "color")) {
                        is Either.Right -> {
                            _state.update { it.copy(isLoading = false, lastLabel = result.value) }
                        }
                        is Either.Left -> {
                            _state.update { it.copy(isLoading = false, error = "Renk algılanamadı") }
                        }
                    }
                }
            }
        } else if (command.contains("nerede") || command.contains("where")) {
            // Navigation Mode
            val objectName = command.replace("nerede", "").replace("where is", "").replace("where", "").trim()
            
            analyzerInstance?.captureNextFrame { base64 ->
                if (base64 == null) return@captureNextFrame
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    when (val result = segmentObjects(base64, objectName)) {
                        is Either.Right -> {
                            val segments = result.value.results
                            if (segments.isNotEmpty()) {
                                val mask = segments.first().mask
                                if (mask.isNotEmpty()) {
                                    val avgX = mask.map { it[0] }.average()
                                    val direction = when {
                                        avgX < 200 -> "solunuzda"
                                        avgX > 300 -> "sağınızda"
                                        else -> "önünüzde"
                                    }
                                    val feedback = "$objectName $direction"
                                    _state.update { it.copy(isLoading = false, lastLabel = feedback) }
                                    _segmentState.value = segments
                                } else {
                                     _state.update { it.copy(isLoading = false, lastLabel = "$objectName bulundu fakat konumu belirsiz") }
                                }
                            } else {
                                _state.update { it.copy(isLoading = false, lastLabel = "$objectName bulunamadı") }
                            }
                        }
                        is Either.Left -> {
                            _state.update { it.copy(isLoading = false, error = "Arama başarısız") }
                        }
                    }
                }
            }
        } else {
            // General Query / Description Mode (Default fallback for everything else)
            // e.g. "betimle", "bu nedir", "describe this", "what is this", or just random text
             analyzerInstance?.captureNextFrame { base64 ->
                if (base64 == null) {
                     _state.update { it.copy(isLoading = false, error = "Görüntü alınamadı") }
                     return@captureNextFrame
                }
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    // Pass the original text as prompt
                    when (val result = askGemini(base64, text)) {
                        is Either.Right -> {
                            _state.update { it.copy(isLoading = false, lastLabel = result.value) }
                        }
                        is Either.Left -> {
                             val msg = when(result.value) {
                                  AppError.Network -> "İnternet bağlantısı yok"
                                  else -> "Cevap alınamadı"
                             }
                            _state.update { it.copy(isLoading = false, error = msg) }
                        }
                    }
                }
            }
        }
    }

    private val _segmentState = MutableStateFlow<List<com.example.visionguide.domain.model.Segment>>(emptyList())
    val segmentState: StateFlow<List<com.example.visionguide.domain.model.Segment>> = _segmentState

    fun segment(base64Image: String, prompt: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = segmentObjects(base64Image, prompt)) {
                is Either.Right -> {
                    _segmentState.value = result.value.results
                    _state.update { it.copy(isLoading = false) }
                }
                is Either.Left -> {
                    // Handle error similarly to onDetectionResult
                     _state.update { it.copy(isLoading = false, error = "Segmentation failed") }
                }
            }
        }
    }

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
                val translatedLabel = if (label != null) {
                    com.example.visionguide.presentation.util.TranslationHelper.translateToTurkish(label)
                } else {
                    null
                }
                
                android.util.Log.d("VisionGuide", "Selected label: $label -> $translatedLabel (score: ${bestDetection?.score})")
                _state.update { 
                    it.copy(
                        lastLabel = translatedLabel, 
                        isLoading = false, 
                        error = null,
                        mode = result.value.mode,
                        modeName = result.value.modeName 
                    ) 
                }
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

    private var analyzerInstance: com.example.visionguide.presentation.analysis.CloudImageAnalyzer? = null

    fun analyzer(enableAutoAnalysis: Boolean = true): com.example.visionguide.presentation.analysis.CloudImageAnalyzer {
        if (analyzerInstance == null) {
            analyzerInstance = com.example.visionguide.presentation.analysis.CloudImageAnalyzer(
                scope = viewModelScope,
                useCase = analyzeImage,
                onResult = { res -> onDetectionResult(res) },
                enableAutoAnalysis = enableAutoAnalysis
            )
        }
        return analyzerInstance!!
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun captureAndReadText() {
        analyzerInstance?.captureNextFrame { base64 ->
            viewModelScope.launch {
                if (base64 == null) {
                    _state.update { it.copy(isLoading = false, error = "Görüntü alınamadı (Kamera hatası)") }
                    return@launch
                }
                _state.update { it.copy(isLoading = true) }
                when (val result = readText(base64)) {
                    is Either.Right -> {
                        _state.update { it.copy(isLoading = false, lastLabel = result.value) }
                    }
                    is Either.Left -> {
                        val message = when (result.value) {
                            AppError.Network -> "Ağ bağlantı hatası. İnternetinizi kontrol edin."
                            AppError.Timeout -> "Zaman aşımı. Sunucu yanıt vermiyor."
                            AppError.Unauthorized -> "Yetkisiz erişim API Anahtarını kontrol edin."
                            is AppError.Server -> "Sunucu hatası: ${result.value.code}. ${result.value.message}"
                            is AppError.Unknown -> "Bilinmeyen hata: ${result.value.cause?.message}"
                        }
                        _state.update { it.copy(isLoading = false, error = message) }
                    }
                }
            }
        }
    }
    fun detectSingleObject() {
        _state.update { it.copy(isLoading = true, lastLabel = null) } // Accessing loading state to show feedback
        analyzerInstance?.triggerOneShotAnalysis()
    }

    fun detectCurrency() {
        analyzerInstance?.captureNextFrame { base64 ->
            if (base64 == null) return@captureNextFrame
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                when (val result = analyze(base64, "currency")) {
                    is Either.Right -> {
                        _state.update { it.copy(isLoading = false, lastLabel = result.value) }
                    }
                    is Either.Left -> {
                        _state.update { it.copy(isLoading = false, error = "Para tanınamadı") }
                    }
                }
            }
        }
    }
}
