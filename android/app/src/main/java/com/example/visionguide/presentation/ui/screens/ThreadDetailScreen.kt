package com.example.visionguide.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionguide.data.network.ApiClient
import com.example.visionguide.data.network.CommentCreateRequest
import com.example.visionguide.data.network.CommentResponse
import com.example.visionguide.data.network.PostResponse
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun ThreadDetailScreen(
    postId: Int, 
    onBack: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val session by authViewModel.session.collectAsState()

    val scope = rememberCoroutineScope()
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCommentDialog by remember { mutableStateOf(false) }

    // Voice & TTS State
    val context = androidx.compose.ui.platform.LocalContext.current
    val speechManager = remember { com.example.visionguide.presentation.util.SpeechRecognizerManager(context) }
    val speechState by speechManager.speechState.collectAsState()
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }
    
    val audioPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )
    
    var commentText by remember { mutableStateOf("") } // Hoisted state

    DisposableEffect(Unit) {
        onDispose { 
            speechManager.destroy()
            ttsManager.shutdown()
        }
    }

    // Handle Speech Results
    LaunchedEffect(speechState) {
        if (speechState is com.example.visionguide.presentation.util.SpeechState.Result) {
            val text = (speechState as com.example.visionguide.presentation.util.SpeechState.Result).text
            if (text.isNotBlank()) {
                commentText = text
                ttsManager.speak("Yorum anlaşıldı: $text")
            }
        }
    }

    val startListening = {
        if (audioPermissionState.status == com.google.accompanist.permissions.PermissionStatus.Granted) {
             ttsManager.speak("Yorumunuzu söyleyin")
             speechManager.startListening()
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    // Fetch Data
    LaunchedEffect(postId) {
        if (postId != 0) {
            try {
                isLoading = true
                val fetchedPost = ApiClient.api.getPost(postId)
                val fetchedComments = ApiClient.api.getComments(postId)
                post = fetchedPost
                comments = fetchedComments
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Konu Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Refresh
                         scope.launch {
                            try {
                                isLoading = true
                                if (postId != 0) {
                                    post = ApiClient.api.getPost(postId)
                                    comments = ApiClient.api.getComments(postId)
                                }
                            } catch(e: Exception) { e.printStackTrace() } 
                            finally { isLoading = false }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    commentText = "" // Reset text
                    showCommentDialog = true 
                },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yorum Ekle")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else if (post != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = post!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    // Main Post
                    ModernPostItem(
                        author = post!!.author,
                        content = post!!.content,
                        timeAgo = "Az önce", // Date parsing omitted for brevity
                        isMainPost = true
                    )
                }
                
                item {
                     Text("Yorumlar (${comments.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                }

                items(comments) { comment ->
                    ModernPostItem(
                        author = comment.author,
                        content = comment.content,
                        timeAgo = comment.created_at.take(10), // Simple substring for date
                        isMainPost = false
                    )
                }
            }
        } else {
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                 Text("Gönderi bulunamadı.")
             }
        }
    }

    if (showCommentDialog) {
        AddCommentDialog(
            value = commentText,
            onValueChange = { commentText = it },
            onVoiceClick = startListening,
            onDismiss = { showCommentDialog = false },
            onSubmit = { 
                scope.launch {
                    try {
                        val author = listOfNotNull(session?.firstName?.trim(), session?.lastName?.trim())
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { "AndroidUser" }

                        ApiClient.api.createComment(
                            postId,
                            CommentCreateRequest(content = commentText, author = author)
                        )
                        // Refresh comments
                        comments = ApiClient.api.getComments(postId)
                        showCommentDialog = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}

@Composable
fun AddCommentDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onDismiss: () -> Unit, 
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yorum Yaz") },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Yorumunuz") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onVoiceClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sesle Yaz")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = value.isNotBlank()
            ) {
                Text("Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ModernPostItem(author: String, content: String, timeAgo: String, isMainPost: Boolean) {
    val backgroundColor = if (isMainPost) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMainPost) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Yazar Avatarı",
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                    )
                    Text(text = author, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}