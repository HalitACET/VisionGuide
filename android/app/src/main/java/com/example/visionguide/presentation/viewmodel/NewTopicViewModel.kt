package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.repository.CommunityRepository
import com.example.visionguide.domain.common.Either
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class NewTopicViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _submissionStatus = MutableStateFlow<Boolean?>(null)
    val submissionStatus: StateFlow<Boolean?> = _submissionStatus

    fun submit(title: String, content: String, audioFile: java.io.File? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _submissionStatus.value = null
            
            var audioUrl: String? = null
             // 1. Upload Audio if exists
            if (audioFile != null) {
                when (val uploadResult = repository.uploadAudio(audioFile)) {
                    is Either.Right -> {
                        audioUrl = uploadResult.value
                    }
                    is Either.Left -> {
                        // Error handling? For now just log or toast via sidechannel
                        _isLoading.value = false
                        _submissionStatus.value = false
                        return@launch
                    }
                }
            }

            // 2. Create Post
            val result = repository.createPost(title, content, audioUrl)
            _isLoading.value = false
            _submissionStatus.value = result is Either.Right
        }
    }
    
    fun resetStatus() {
        _submissionStatus.value = null
    }
}
