package com.example.visionguide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionguide.ui.theme.VisionGuideTheme

data class Post(
    val author: String,
    val content: String,
    val isMainPost: Boolean = false
)

val mainPost = Post(
    author = "Ahmet Y.",
    content = "Merhaba arkadaşlar, yeni çıkan sesli kitap okuyucusu hakkında ne düşünüyorsunuz?",
    isMainPost = true
)

val replies = listOf(
    Post(author = "Zeynep A.", content = "Ben denedim, oldukça başarılı buldum."),
    Post(author = "Ali V.", content = "Ben denedim, 5 Ceva başarılı buldum."),
    Post(author = "Ali V.", content = "Pil ömrü konusunda biraz endişelerim var.")
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(onBack: () -> Unit = {}) {
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
                    IconButton(onClick = { /* Yenile */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Kalan tüm alanı kapla
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Ana Mesaj
                item {
                    PostItem(post = mainPost)
                }
                // Cevaplar
                items(replies) { reply ->
                    PostItem(post = reply)
                }
            }

            // Cevap Yaz Butonu
            Button(
                onClick = { /* Cevap Yaz */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Sesli Dikte", tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CEVAP YAZ (Sesli Dikte)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    val cardLabel = if (post.isMainPost) "Ana Mesaj" else "Cevap"
    val fontWeight = if (post.isMainPost) FontWeight.Bold else FontWeight.Normal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cardLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.content,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "- ${post.author}",
                fontSize = 14.sp,
                fontWeight = fontWeight,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThreadDetailScreenPreview() {
    VisionGuideTheme {
        ThreadDetailScreen(onBack = {})
    }
}