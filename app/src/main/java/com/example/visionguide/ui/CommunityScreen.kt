package com.example.visionguide.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionguide.ui.theme.VisionGuideTheme

// Veri Sınıfı: Her bir konuyu temsil eder
data class ForumTopic(
    val title: String,
    val author: String,
    val replyCount: Int,
    val lastReplyAuthor: String,
    val timeAgo: String
)

// Örnek veri listesi
val sampleTopics = listOf(
    ForumTopic("Yeni çıkan sesli kitap okuyucusu", "Ahmet Y.", 3, "Zeynep A.", "2 saat önce"),
    ForumTopic("En iyi ekran okuyucu hangisi?", "Ayşe K.", 8, "Mehmet B.", "5 saat önce"),
    ForumTopic("Görme engelliler için navigasyon", "Fatma S.", 12, "Ali V.", "1 gün önce"),
    ForumTopic("iOS vs Android erişilebilirlik", "Can T.", 21, "Elif G.", "2 gün önce")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBack: () -> Unit = {},
    onOpenThread: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTopic: () -> Unit = {}
) {
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
            items(sampleTopics) { topic ->
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