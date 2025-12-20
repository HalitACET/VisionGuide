package com.example.visionguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.visionguide.presentation.ui.screens.CommunityScreen
import com.example.visionguide.presentation.ui.screens.HomeScreen
import com.example.visionguide.presentation.ui.screens.LoginScreen
import com.example.visionguide.presentation.ui.screens.NewTopicScreen
import com.example.visionguide.presentation.ui.screens.ProfileScreen
import com.example.visionguide.presentation.ui.screens.RegisterScreen
import com.example.visionguide.presentation.ui.screens.SettingsScreen
import com.example.visionguide.presentation.ui.screens.ThreadDetailScreen
import com.example.visionguide.presentation.ui.screens.ObjectDetectionScreen
import com.example.visionguide.presentation.ui.theme.VisionGuideTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visionguide.presentation.viewmodel.NewTopicViewModel
import com.example.visionguide.presentation.viewmodel.ObjectDetectionViewModel
import com.example.visionguide.presentation.viewmodel.AuthViewModel
import com.example.visionguide.presentation.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: com.example.visionguide.presentation.viewmodel.SettingsViewModel = hiltViewModel()
            val darkMode by settingsViewModel.darkMode.collectAsState()

            VisionGuideTheme(darkTheme = darkMode) {
                AppNav()
            }
        }
    }
}

@Composable
private fun AppNav() {
    val navController: NavHostController = rememberNavController()
    val settingsViewModel: com.example.visionguide.presentation.viewmodel.SettingsViewModel = hiltViewModel()
    val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()

    val authViewModel: com.example.visionguide.presentation.viewmodel.AuthViewModel = hiltViewModel()
    val session by authViewModel.session.collectAsState()

    // Determine start destination based on onboarding status
    // Note: In a real app, we might want to show a splash screen while loading this state.
    // For now, we rely on the initial value from DataStore (which is synchronous-ish via StateFlow initial value, 
    // but might be false initially if loading async. Since we use SharedPreferences in Repo with initial value, it should be fast).
    // However, StateFlow in ViewModel uses 'false' as default. 
    // To avoid flickering, we might need a better loading state. 
    // But for this MVP, let's just use "onboarding" if false. 
    // If the user has completed it, they might see onboarding for a split second if reading from disk is slow.
    // A better approach is to read it in MainActivity before setContent or use a Splash screen.
    // Let's keep it simple: If false, go to onboarding.
    
    val startDestination = when {
        !isOnboardingCompleted -> "onboarding"
        session == null -> "login"
        else -> "home"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            com.example.visionguide.presentation.ui.screens.OnboardingScreen(
                onComplete = {
                    navController.navigate(if (session == null) "login" else "home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateCommunity = { navController.navigate("community") },
                onNavigateObjectDetection = { navController.navigate("objectDetection") },
                onNavigateTextReader = { navController.navigate("textReader") },
                onNavigateCurrency = { navController.navigate("currency") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("community") {
            CommunityScreen(
                onBack = { navController.popBackStack() },
                onOpenThread = { postId -> navController.navigate("threadDetail/$postId") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToNewTopic = { navController.navigate("newTopic") }
            )
        }
        composable(
            route = "threadDetail/{postId}",
            arguments = listOf(androidx.navigation.navArgument("postId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            ThreadDetailScreen(
                postId = postId,
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
            val submissionStatus by vm.submissionStatus.collectAsState()
            
            androidx.compose.runtime.LaunchedEffect(submissionStatus) {
                if (submissionStatus == true) {
                    navController.popBackStack()
                    vm.resetStatus()
                }
            }
            
            NewTopicScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { title, content, audioFile ->
                    vm.submit(title, content, audioFile)
                }
            )
        }
        composable("objectDetection") {
            val vm: ObjectDetectionViewModel = hiltViewModel()
            ObjectDetectionScreen(viewModel = vm)
        }
        composable("textReader") {
            val vm: ObjectDetectionViewModel = hiltViewModel()
            com.example.visionguide.presentation.ui.screens.TextReaderScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("currency") {
            val vm: ObjectDetectionViewModel = hiltViewModel()
            com.example.visionguide.presentation.ui.screens.CurrencyScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}