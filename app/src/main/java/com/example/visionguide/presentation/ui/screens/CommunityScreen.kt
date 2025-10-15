package com.example.visionguide.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.CommunityViewModel
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme

data class ForumTopic(
    val title: String,
    val author: String,
    val replyCount: Int,
    val lastReplyAuthor: String,
    val timeAgo: String
)

private fun com.example.visionguide.domain.model.Post.toForumTopic(): ForumTopic {
    val now = System.currentTimeMillis()
    val diff = (now - createdAt).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val timeAgo = when {
        diff < minute -> "az önce"
        diff < hour -> "${diff / minute} dk önce"
        diff < day -> "${diff / hour} sa önce"
        else -> "${diff / day} gün önce"
    }
    return ForumTopic(
        title = title,
        author = author,
        replyCount = replyCount,
        lastReplyAuthor = lastReplyAuthor,
        timeAgo = timeAgo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBack: () -> Unit = {},
    onOpenThread: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTopic: () -> Unit = {}
) {
    val viewModel: CommunityViewModel = hiltViewModel()
    val posts by viewModel.posts.collectAsState(initial = emptyList())
    val topics = posts.map { it.toForumTopic() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TOPLULUK", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profilim", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewTopic,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Konu Aç", tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("YENİ KONU AÇ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topics) { topic ->
                TopicListItem(
                    topic = topic,
                    modifier = Modifier.clickable { onOpenThread() }
                )
            }
        }
    }
}

@Composable
fun TopicListItem(topic: ForumTopic, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = topic.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Yazar: ${topic.author} - ${topic.replyCount} Cevap - Son Cevap: ${topic.lastReplyAuthor}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = topic.timeAgo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    VisionGuideTheme {
        CommunityScreen(onBack = {}, onOpenThread = {})
        CommunityScreen()
    }
}
