package com.example.visionguide.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.SettingsViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }
    val speechRate by viewModel.speechRate.collectAsState()

    // Cleanup TTS
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Update speech rate when it changes
    LaunchedEffect(speechRate) {
        ttsManager.setSpeechRate(speechRate)
    }

    val slides = listOf(
        OnboardingSlide(
            title = "VisionGuide'a Hoşgeldiniz",
            description = "Ben VisionGuide. Görmenize yardımcı olmak için buradayım. Sizin gözünüz olacağım.",
            icon = Icons.Default.Check // Placeholder icon
        ),
        OnboardingSlide(
            title = "Nesne Tanıma",
            description = "Etrafınızdaki nesneleri tanımak için kamerayı tutun ve 'Nesne' deyin.",
            icon = Icons.Default.Check
        ),
        OnboardingSlide(
            title = "Metin Okuma",
            description = "Yazıları okumak için 'Oku' deyin. Ben sizin için seslendireceğim.",
            icon = Icons.Default.Check
        ),
        OnboardingSlide(
            title = "Sesli Kontrol",
            description = "Uygulamayı tamamen sesinizle yönetebilirsiniz. Hazırsanız başlayalım!",
            icon = Icons.Default.Check
        )
    )

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]

    // Speak slide content when slide changes
    LaunchedEffect(currentSlideIndex) {
        val textToSpeak = "${currentSlide.title}. ${currentSlide.description}"
        ttsManager.speak(textToSpeak)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Icon (Placeholder)
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = currentSlide.icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = currentSlide.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentSlide.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Button
        Button(
            onClick = {
                if (currentSlideIndex < slides.size - 1) {
                    currentSlideIndex++
                } else {
                    viewModel.completeOnboarding()
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape
        ) {
            Text(
                text = if (currentSlideIndex < slides.size - 1) "Devam Et" else "Başla",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector
)
