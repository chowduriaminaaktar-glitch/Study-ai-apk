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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import com.example.ui.components.StudyTopBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistorySavedScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SolveQuestionScreen
import com.example.ui.screens.SubjectExploreScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.auth.AccountProfileDialog
import com.example.ui.auth.AuthScreen
import com.example.ui.chat.ChatGptScreen
import com.example.ui.library.LibraryScreen

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
            onSignOut = { viewModel.signOut() },
            onSignInClick = { viewModel.navigateTo(Screen.Auth) }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_app_scaffold"),
        topBar = {
            StudyTopBar(
                currentScreen = currentScreen,
                onBackClick = if (currentScreen != Screen.Home) {
                    { viewModel.navigateTo(Screen.Home) }
                } else null,
                currentUser = currentUser,
                onAccountClick = { showAccountDialog = true },
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("study_bottom_navigation"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_item_home")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Chat,
                    onClick = { viewModel.navigateTo(Screen.Chat) },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    modifier = Modifier.testTag("nav_item_chat")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Library,
                    onClick = { viewModel.navigateTo(Screen.Library) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Library") },
                    label = { Text("Library") },
                    modifier = Modifier.testTag("nav_item_library")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.SolveQuestion,
                    onClick = { viewModel.navigateTo(Screen.SolveQuestion) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Solve") },
                    label = { Text("Solve") },
                    modifier = Modifier.testTag("nav_item_solve")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.PracticeQuizzes,
                    onClick = { viewModel.navigateTo(Screen.PracticeQuizzes) },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Quizzes") },
                    label = { Text("Quizzes") },
                    modifier = Modifier.testTag("nav_item_quizzes")
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
                is Screen.Chat -> ChatGptScreen()
                is Screen.Library -> LibraryScreen(
                    onAskAiAboutBook = { bookTitle, chapterTitle ->
                        viewModel.navigateTo(Screen.Chat)
                    },
                    onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                )
                is Screen.SolveQuestion -> SolveQuestionScreen(viewModel = viewModel)
                is Screen.PracticeQuizzes -> QuizScreen(viewModel = viewModel)
                is Screen.SubjectExplore -> SubjectExploreScreen(viewModel = viewModel)
                is Screen.SavedAndHistory -> HistorySavedScreen(viewModel = viewModel)
                is Screen.Auth -> AuthScreen(onAuthSuccess = { viewModel.navigateTo(Screen.Home) })
            }
        }
    }
}
