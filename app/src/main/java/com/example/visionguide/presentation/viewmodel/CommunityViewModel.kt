package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.model.Post
import com.example.visionguide.domain.usecase.AddPostUseCase
import com.example.visionguide.domain.usecase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getPosts: GetPostsUseCase,
    private val addPost: AddPostUseCase
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    init {
        observePosts()
    }

    private fun observePosts() {
        viewModelScope.launch {
            getPosts().collectLatest { list ->
                _posts.value = list
            }
        }
    }

    fun addSamplePost() {
        viewModelScope.launch {
            addPost(
                Post(
                    title = "Örnek Başlık",
                    author = "Sistem",
                    replyCount = 0,
                    lastReplyAuthor = "",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
