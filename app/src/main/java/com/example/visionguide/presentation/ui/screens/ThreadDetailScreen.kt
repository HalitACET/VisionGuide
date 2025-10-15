package com.example.visionguide.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme

// Bu data class ve veriler normalde ViewModel'den gelir.
data class ThreadPostUI(
    val id: String,
    val author: String,
    val content: String,
    val timeAgo: String,
    val isMainPost: Boolean = false
)

// CommunityScreen ile tutarlı, anlamlı içerik
val mainPost = ThreadPostUI(
    id = "p1",
    author = "Ahmet Y.",
    content = "Merhaba arkadaşlar, OrCam MyEye dışında daha uygun fiyatlı, günlük hayatta pratik olarak kullanabileceğim nesne tanıma cihazı veya uygulama öneriniz var mı? Özellikle market alışverişinde ürünleri ayırt etmek için arıyorum.",
    timeAgo = "1 gün önce",
    isMainPost = true
)

val replies = listOf(
    ThreadPostUI(id = "r1", author = "Elif G.", content = "Selam Ahmet, ben telefonumda 'Seeing AI' uygulamasını kullanıyorum. Tamamen ücretsiz ve barkod okuma, metin okuma, renk tanıma gibi birçok özelliği var. Market ürünleri için barkod okuyucusu çok işe yarıyor.", timeAgo = "22 saat önce"),
    ThreadPostUI(id = "r2", author = "Mehmet B.", content = "'Seeing AI' gerçekten başarılı. Bir de 'Envision AI' var, o da çok yetenekli ama bazı özellikleri ücretli abonelik istiyor. İkisini de deneyip karşılaştırabilirsin.", timeAgo = "18 saat önce"),
    ThreadPostUI(id = "r3", author = "Ahmet Y.", content = "Harika öneriler, çok teşekkür ederim! Seeing AI'ı hemen deneyeceğim.", timeAgo = "15 saat önce")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Konu Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Yenile */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Cevap yazma ekranını aç */ },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Sesli Cevap Yaz")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "OrCam MyEye dışında uygun fiyatlı nesne tanıma cihazı önerisi?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                ModernPostItem(post = mainPost)
            }
            items(replies, key = { it.id }) { reply ->
                ModernPostItem(post = reply)
            }
        }
    }
}

@Composable
fun ModernPostItem(post: ThreadPostUI) {
    val backgroundColor = if (post.isMainPost) {
        MaterialTheme.colorScheme.surfaceVariant // Ana gönderi için farklı renk
    } else {
        MaterialTheme.colorScheme.surface // Cevaplar için standart renk
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (post.isMainPost) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Kart Başlığı (Avatar, İsim, Zaman)
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
                    Text(text = post.author, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = post.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Gönderi İçeriği
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ThreadDetailScreenPreview() {
    VisionGuideTheme {
        ThreadDetailScreen()
    }
}