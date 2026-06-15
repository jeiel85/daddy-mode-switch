package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DadModeViewModel
import com.example.ui.viewmodel.DadModeViewModelFactory

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { AppRepository(database.appDao()) }
    private val viewModel: DadModeViewModel by viewModels {
        DadModeViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Graceful runtime notification permission prompt for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            MyApplicationTheme {
                MainAppHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppHost(viewModel: DadModeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // Only show bottom navigation on primary level screens
    val showBottomBar = currentRoute in listOf("home", "history", "settings")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "홈") },
                        label = { Text("홈", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )

                    // Logs Tab (History)
                    NavigationBarItem(
                        selected = currentRoute == "history",
                        onClick = {
                            if (currentRoute != "history") {
                                navController.navigate("history") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Book, contentDescription = "기록첩") },
                        label = { Text("기록첩", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )

                    // Settings Tab
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "설정") },
                        label = { Text("설정", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Dashboard
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onStartTransition = {
                        navController.navigate("mood_check")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToHistory = {
                        navController.navigate("history")
                    }
                )
            }

            // Step 1: Stress & Condition Check
            composable("mood_check") {
                MoodCheckScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onMoodSaved = {
                        navController.navigate("breath") {
                            popUpTo("mood_check") { inclusive = true }
                        }
                    }
                )
            }

            // Step 2: 3-Minute breathing meditation
            composable("breath") {
                BreathScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onBreathCompleted = {
                        navController.navigate("phrase_choice") {
                            popUpTo("breath") { inclusive = true }
                        }
                    }
                )
            }

            // Step 3 & 4: Warm word pledge and family actions selections
            composable("phrase_choice") {
                PhraseScreen(
                    viewModel = viewModel,
                    onBackToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }

            // Logs Screen
            composable("history") {
                HistoryScreen(viewModel = viewModel)
            }

            // Settings Screen
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
