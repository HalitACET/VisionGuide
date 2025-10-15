package com.example.visionguide.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.hilt.navigation.compose.hiltViewModel
// import com.example.visionguide.domain.model.Post
// import com.example.visionguide.presentation.viewmodel.CommunityViewModel
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme

// ÖNEMLİ: Bu data class ve mapper fonksiyonu normalde domain katmanında olur.
// Sadece bu ekranın çalışması için geçici olarak burada bırakıldı.
data class ForumTopic(
    val id: String,
    val title: String,
    val author: String,
    val replyCount: Int,
    val timeAgo: String
)

// Örnek Veri
val sampleTopics = listOf(
    ForumTopic("1", "Android 15'in yeni erişilebilirlik özellikleri", "Elif G.", 8, "2 saat önce"),
    ForumTopic("2", "OrCam MyEye dışında uygun fiyatlı nesne tanıma cihazı önerisi?", "Ahmet Y.", 15, "1 gün önce"),
    ForumTopic("3", "Görme engelliler için en iyi navigasyon uygulaması hangisi?", "Zeynep A.", 21, "3 gün önce")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBack: () -> Unit = {},
    onOpenThread: (topicId: String) -> Unit = {}, // Artık hangi konunun açıldığını bilmeliyiz
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTopic: () -> Unit = {}
) {
    // ViewModel'den veri geldiğini varsayalım. Şimdilik statik veri kullanıyoruz.
    // val viewModel: CommunityViewModel = hiltViewModel()
    // val posts by viewModel.posts.collectAsState(initial = null)
    val topics: List<ForumTopic>? = sampleTopics // posts?.map { it.toForumTopic() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Topluluk Forumu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profilim", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToNewTopic,
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yeni Konu Aç", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                // Veri yükleniyor durumu
                topics == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Veri var ama liste boş durumu
                topics.isEmpty() -> {
                    EmptyState(
                        text = "Henüz hiç konu açılmamış.\nİlk konuyu sen aç!"
                    )
                }
                // Veri başarıyla yüklendi durumu
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topics, key = { it.id }) { topic ->
                            ModernTopicListItem(
                                topic = topic,
                                onClick = { onOpenThread(topic.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTopicListItem(topic: ForumTopic, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Yazar ve Cevap Sayısı
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip(icon = Icons.Default.Person, text = topic.author)
                    InfoChip(icon = Icons.Default.ChatBubbleOutline, text = "${topic.replyCount} Cevap")
                }
                // Zaman
                Text(
                    text = topic.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    VisionGuideTheme {
        CommunityScreen()
    }
}