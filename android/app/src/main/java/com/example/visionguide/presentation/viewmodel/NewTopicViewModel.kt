package com.example.visionguide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visionguide.domain.model.Post
import com.example.visionguide.domain.usecase.AddPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class NewTopicViewModel @Inject constructor(
    private val addPost: AddPostUseCase
) : ViewModel() {

    fun submit(title: String, content: String, author: String = "Anonim") {
        viewModelScope.launch {
            addPost(
                Post(
                    title = title,
                    author = author,
                    replyCount = 0,
                    lastReplyAuthor = "",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
