package com.example.visionguide.presentation.ui.screens



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme


@OptIn(ExperimentalMaterial3Api::class, com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun NewTopicScreen(
    onBack: () -> Unit,
    onSubmit: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    // Voice & TTS State
    val context = androidx.compose.ui.platform.LocalContext.current
    val speechManager = remember { com.example.visionguide.presentation.util.SpeechRecognizerManager(context) }
    val speechState by speechManager.speechState.collectAsState()
    val ttsManager = remember { com.example.visionguide.presentation.util.TextToSpeechManager(context) }
    
    val audioPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )
    
    // Track which field is active for voice input: "title", "content", or null
    var activeVoiceField by remember { mutableStateOf<String?>(null) }

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
                if (activeVoiceField == "title") {
                    title = text
                    ttsManager.speak("Başlık yazıldı: $text")
                } else if (activeVoiceField == "content") {
                    content = text
                    ttsManager.speak("İçerik yazıldı.")
                }
                activeVoiceField = null
            }
        }
    }

    val startListening = { field: String ->
        if (audioPermissionState.status == com.google.accompanist.permissions.PermissionStatus.Granted) {
            activeVoiceField = field
            
            // TTS Prompt
            val prompt = if (field == "title") "Başlığı söyleyin" else "Mesajınızı söyleyin"
            ttsManager.speak(prompt)
            
            // Small delay to allow TTS to start? Actually speech recognizer handles it usually, 
            // but let's just start listening. Note: System might mute listening if TTS talks.
            // Ideal: Wait for TTS end. But simplifying:
            // "Dinliyorum" implies ready.
            speechManager.startListening()
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Konu Aç", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TITLE SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Konu Başlığı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Örn: Market alışverişi") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { startListening("title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Başlığı Sesle Yaz")
                    }
                }
            }

            // CONTENT SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("İçerik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        placeholder = { Text("Mesajınızı buraya yazın...") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { startListening("content") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mesajı Sesle Yaz")
                    }
                }
            }

            // SUBMIT BUTTON
            Button(
                onClick = { onSubmit(title, content) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("GÖNDER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewTopicScreenPreview() {
    VisionGuideTheme {
        NewTopicScreen(onBack = {}, onSubmit = { _, _ -> })
    }
}