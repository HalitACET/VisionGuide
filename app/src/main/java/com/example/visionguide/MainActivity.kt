package com.example.visionguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.visionguide.presentation.ui.screens.CommunityScreen
import com.example.visionguide.presentation.ui.screens.HomeScreen
import com.example.visionguide.presentation.ui.screens.NewTopicScreen
import com.example.visionguide.presentation.ui.screens.ProfileScreen
import com.example.visionguide.presentation.ui.screens.SettingsScreen
import com.example.visionguide.presentation.ui.screens.ThreadDetailScreen
import com.example.visionguide.presentation.ui.screens.ObjectDetectionScreen
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.NewTopicViewModel
import com.example.visionguide.presentation.viewmodel.ObjectDetectionViewModel

@AndroidEntryPoint
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
                onNavigateObjectDetection = { navController.navigate("objectDetection") },
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
            val vm: NewTopicViewModel = hiltViewModel()
            NewTopicScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { title, content ->
                    vm.submit(title, content)
                    navController.popBackStack()
                }
            )
        }
        composable("objectDetection") {
            val vm: ObjectDetectionViewModel = hiltViewModel()
            ObjectDetectionScreen(viewModel = vm)
        }
    }
}