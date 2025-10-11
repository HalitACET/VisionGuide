package com.example.visionguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.visionguide.ui.NewTopicScreen
import com.example.visionguide.ui.*
import com.example.visionguide.ui.theme.VisionGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VisionGuideTheme {
                AppNav()
            }
        }
    }
}

@Composable
private fun AppNav() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateCommunity = { navController.navigate("community") },
                onNavigateObjectDetection = { /* Şimdilik boş */ },
                onNavigateTextReader = { /* Şimdilik boş */ },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("community") {
            CommunityScreen(
                onBack = { navController.popBackStack() },
                onOpenThread = { navController.navigate("threadDetail") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToNewTopic = { navController.navigate("newTopic") }
            )
        }
        composable("threadDetail") {
            ThreadDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("newTopic") {
            NewTopicScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { title, content ->
                    // TODO: Konuyu veritabanına veya API'ye gönderme işlemleri burada yapılacak.
                    navController.popBackStack()
                }
            )
        }
    }
}