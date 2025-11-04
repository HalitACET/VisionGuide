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
import androidx.compose.ui.unit.sp
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme

// Örnek Veri: Normalde bu veriler ViewModel'den gelir.
data class UserProfile(
    val name: String,
    val email: String,
    val joinDate: String,
    val topicCount: Int,
    val posts: List<ProfilePost>
)

data class ProfilePost(
    val id: String,
    val title: String,
    val replyCount: Int,
    val timeAgo: String
)

val sampleUserProfile = UserProfile(
    name = "Ahmet Y.",
    email = "ahmet.y@email.com",
    joinDate = "Ekim 2025",
    topicCount = 2,
    posts = listOf(
        ProfilePost("1", "OrCam MyEye dışında uygun fiyatlı nesne tanıma cihazı önerisi?", 15, "1 gün önce"),
        ProfilePost("2", "Metin okuma uygulamalarında Türkçe karakter sorunu yaşayan var mı?", 5, "2 hafta önce")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {}
) {
    val userProfile = sampleUserProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Başlığı Kartı
            item {
                ProfileHeaderCard(
                    name = userProfile.name,
                    email = userProfile.email,
                    joinDate = userProfile.joinDate,
                    topicCount = userProfile.topicCount
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // "Başlattığı Konular" Başlığı
            item {
                Text(
                    text = "Başlattığı Konular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Kullanıcının Konu Listesi
            items(userProfile.posts, key = { it.id }) { post ->
                ProfileTopicItem(post = post)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(name: String, email: String, joinDate: String, topicCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profil Avatarı",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Divider(modifier = Modifier.padding(vertical = 20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(icon = Icons.Default.Forum, label = "Konu Sayısı", value = topicCount.toString())
                ProfileStat(icon = Icons.Default.CalendarMonth, label = "Katılım Tarihi", value = joinDate)
            }
        }
    }
}

@Composable
private fun ProfileStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ProfileTopicItem(post: ProfilePost) {
    Card(
        onClick = { /* Konu detayına git */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = post.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = post.replyCount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "Cevap", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    VisionGuideTheme {
        ProfileScreen()
    }
}