package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.usecase.AnalyzeImageUseCase
import com.example.visionguide.data.network.DetectionResponse
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

    fun onDetectionResult(result: Result<DetectionResponse>) {
        result.onSuccess { resp ->
            val label = resp.detections.maxByOrNull { it.score }?.label
            _state.update { it.copy(lastLabel = label, isLoading = false, error = null) }
        }.onFailure { t ->
            _state.update { it.copy(error = t.message, isLoading = false) }
        }
    }

    fun analyzer(scopeProvider: () -> kotlinx.coroutines.CoroutineScope): com.example.visionguide.analysis.CloudImageAnalyzer {
        return com.example.visionguide.analysis.CloudImageAnalyzer(
            scope = scopeProvider(),
            useCase = analyzeImage,
            onResult = { res -> onDetectionResult(res) }
        )
    }
}
