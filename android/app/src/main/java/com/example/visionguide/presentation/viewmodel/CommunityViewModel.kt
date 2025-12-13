package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.data.network.PostResponse
import com.example.visionguide.domain.common.Either
import com.example.visionguide.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val posts: List<com.example.visionguide.domain.model.Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getPosts()) {
                is Either.Right -> {
                    val domainPosts = result.value.map { response ->
                        com.example.visionguide.domain.model.Post(
                            id = response.id.toString(),
                            title = response.title,
                            content = response.content,
                            author = response.author
                        )
                    }
                    _state.update { it.copy(isLoading = false, posts = domainPosts) }
                }
                is Either.Left -> {
                    _state.update { it.copy(isLoading = false, error = "Gönderiler yüklenemedi") }
                }
            }
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createPost(title, content)) {
                is Either.Right -> {
                    // Reload posts to show the new one
                    loadPosts()
                }
                is Either.Left -> {
                    _state.update { it.copy(isLoading = false, error = "Gönderi oluşturulamadı") }
                }
            }
        }
    }
}
