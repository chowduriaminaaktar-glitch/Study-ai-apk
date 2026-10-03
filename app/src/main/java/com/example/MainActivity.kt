package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import com.example.ui.auth.AccountProfileDialog
import com.example.ui.auth.AuthScreen
import com.example.ui.components.StudyTopBar
import com.example.ui.library.LibraryScreen
import com.example.ui.live.StudyAiLiveScreen
import com.example.ui.screens.HistorySavedScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SolveQuestionScreen
import com.example.ui.screens.SubjectExploreScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val studyViewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudyApp(viewModel = studyViewModel)
            }
        }
    }
}

@Composable
fun StudyApp(viewModel: StudyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAccountDialog by remember { mutableStateOf(false) }

    if (showAccountDialog) {
        AccountProfileDialog(
            currentUser = currentUser,
            onDismiss = { showAccountDialog = false },
            onSignInClick = { viewModel.navigateTo(Screen.Auth) },
            onSignOut = { viewModel.signOut() }
        )
    }

    Scaffold(
        topBar = {
            if (currentScreen !is Screen.StudyAiLive) {
                StudyTopBar(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    onAccountClick = { showAccountDialog = true },
                    onLiveClick = { viewModel.navigateTo(Screen.StudyAiLive) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = currentScreen is Screen.Home,
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_home")
                )

                // 2. Study AI Live (Replaces old text AI chat / ChatGPT with real-time talking tutor)
                NavigationBarItem(
                    selected = currentScreen is Screen.StudyAiLive,
                    onClick = { viewModel.navigateTo(Screen.StudyAiLive) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Study AI Live"
                        )
                    },
                    label = { Text("Live AI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_study_ai_live")
                )

                // 3. Ask StudyAI (Solver)
                NavigationBarItem(
                    selected = currentScreen is Screen.SolveQuestion,
                    onClick = { viewModel.navigateTo(Screen.SolveQuestion) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Solve") },
                    label = { Text("Solve", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_solve")
                )

                // 4. Practice Quizzes
                NavigationBarItem(
                    selected = currentScreen is Screen.PracticeQuizzes,
                    onClick = { viewModel.navigateTo(Screen.PracticeQuizzes) },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Quizzes") },
                    label = { Text("Quizzes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_quizzes")
                )

                // 5. Digital Library
                NavigationBarItem(
                    selected = currentScreen is Screen.Library,
                    onClick = { viewModel.navigateTo(Screen.Library) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_library")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Home -> HomeScreen(viewModel = viewModel)
                is Screen.StudyAiLive -> StudyAiLiveScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.Home) }
                )
                is Screen.SolveQuestion -> SolveQuestionScreen(viewModel = viewModel)
                is Screen.PracticeQuizzes -> QuizScreen(viewModel = viewModel)
                is Screen.Library -> LibraryScreen(
                    onDiscussWithStudyAiLive = { _, _ ->
                        viewModel.navigateTo(Screen.StudyAiLive)
                    },
                    onBack = { viewModel.navigateTo(Screen.Home) }
                )
                is Screen.SubjectExplore -> SubjectExploreScreen(viewModel = viewModel)
                is Screen.SavedAndHistory -> HistorySavedScreen(viewModel = viewModel)
                is Screen.Auth -> AuthScreen(
                    onAuthSuccess = { viewModel.navigateTo(Screen.Home) }
                )
            }
        }
    }
}
