package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.SavedSolutionEntity
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorySavedScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var filterFavoritesOnly by remember { mutableStateOf(false) }

    val chatSessions by viewModel.chatSessions.collectAsState()
    val savedSolutions by viewModel.savedSolutions.collectAsState()
    val savedQuizzes by viewModel.savedQuizzes.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_saved_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Chat Sessions (${chatSessions.size})") },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_chat_sessions")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Saved Solutions (${savedSolutions.size})") },
                icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_saved_solutions")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Saved Quizzes (${savedQuizzes.size})") },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_saved_quizzes")
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Room-stored Chat Sessions
                ChatSessionsTabContent(
                    sessions = chatSessions,
                    onOpenSession = { viewModel.loadChatSession(it) },
                    onStartNew = {
                        viewModel.startNewChatSession()
                        viewModel.navigateTo(Screen.SolveQuestion)
                    },
                    onTogglePin = { viewModel.togglePinSession(it.id, it.isPinned) },
                    onDelete = { viewModel.deleteChatSession(it.id) }
                )
            }
            1 -> {
                // Tab 1: Saved Solutions Bookmarks
                SavedSolutionsTabContent(
                    savedList = savedSolutions,
                    filterFavoritesOnly = filterFavoritesOnly,
                    onToggleFilterFavorites = { filterFavoritesOnly = !filterFavoritesOnly },
                    onOpenSolution = { viewModel.viewSavedSolution(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it.id, it.isFavorite) },
                    onDelete = { viewModel.deleteSavedSolution(it.id) }
                )
            }
            2 -> {
                // Tab 2: Saved Practice Quizzes
                SavedQuizzesTabContent(
                    quizzes = savedQuizzes,
                    onReviewQuiz = { viewModel.openSavedQuizForReview(it) },
                    onRetakeQuiz = { viewModel.retakeSavedQuiz(it) },
                    onToggleFavorite = { viewModel.toggleSavedQuizFavorite(it.id, it.isFavorite) },
                    onDelete = { viewModel.deleteSavedQuiz(it.id) },
                    onGoToQuizzes = { viewModel.navigateTo(Screen.PracticeQuizzes) }
                )
            }
        }
    }
}

@Composable
private fun ChatSessionsTabContent(
    sessions: List<ChatSessionEntity>,
    onOpenSession: (ChatSessionEntity) -> Unit,
    onStartNew: () -> Unit,
    onTogglePin: (ChatSessionEntity) -> Unit,
    onDelete: (ChatSessionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_sessions_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Previous Study Chats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stored locally in Room database for offline revision",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStartNew,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("start_new_chat_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chat", fontSize = 12.sp)
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Previous Chat Sessions",
                    description = "When you ask StudyAI questions, your conversational study sessions with complete derivation steps will be stored here in Room.",
                    icon = Icons.AutoMirrored.Filled.Chat
                )
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                ChatSessionCard(
                    session = session,
                    onClick = { onOpenSession(session) },
                    onTogglePin = { onTogglePin(session) },
                    onDelete = { onDelete(session) }
                )
            }
        }
    }
}

@Composable
fun ChatSessionCard(
    session: ChatSessionEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chat_session_${session.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = session.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (session.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberTertiary.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Pinned",
                                    tint = AmberTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Pinned",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberTertiary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (session.isPinned) Icons.Default.Star else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin session",
                            tint = if (session.isPinned) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete session",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = session.previewText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(session.updatedAt))
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Resume Chat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedSolutionsTabContent(
    savedList: List<SavedSolutionEntity>,
    filterFavoritesOnly: Boolean,
    onToggleFilterFavorites: () -> Unit,
    onOpenSolution: (SavedSolutionEntity) -> Unit,
    onToggleFavorite: (SavedSolutionEntity) -> Unit,
    onDelete: (SavedSolutionEntity) -> Unit
) {
    val displayedList = if (filterFavoritesOnly) {
        savedList.filter { it.isFavorite }
    } else {
        savedList
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_solutions_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarked Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilterChip(
                    selected = filterFavoritesOnly,
                    onClick = onToggleFilterFavorites,
                    label = { Text("Favorites") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (filterFavoritesOnly) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_favorites_chip")
                )
            }
        }

        if (displayedList.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (filterFavoritesOnly) "No Favorite Solutions" else "No Saved Solutions Yet",
                    description = "When you solve a question in Ask StudyAI, tap the bookmark icon to save it here for later revision.",
                    icon = Icons.Default.Bookmark
                )
            }
        } else {
            items(displayedList, key = { it.id }) { item ->
                SavedSolutionCard(
                    entity = item,
                    onClick = { onOpenSolution(item) },
                    onToggleFavorite = { onToggleFavorite(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
fun SavedSolutionCard(
    entity: SavedSolutionEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("saved_item_${entity.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = entity.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (entity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (entity.isFavorite) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete solution",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entity.question,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entity.directAnswer,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(entity.timestamp))
            Text(
                text = dateStr,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SavedQuizzesTabContent(
    quizzes: List<SavedPracticeQuizEntity>,
    onReviewQuiz: (SavedPracticeQuizEntity) -> Unit,
    onRetakeQuiz: (SavedPracticeQuizEntity) -> Unit,
    onToggleFavorite: (SavedPracticeQuizEntity) -> Unit,
    onDelete: (SavedPracticeQuizEntity) -> Unit,
    onGoToQuizzes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_quizzes_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Practice Quizzes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Full question banks, explanations, and your attempts",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onGoToQuizzes,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("generate_new_quiz_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Quiz", fontSize = 12.sp)
                }
            }
        }

        if (quizzes.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Saved Practice Quizzes Yet",
                    description = "Generate customized practice quizzes in the Quizzes tab. They will be automatically saved in Room with your scores and full explanations for revision.",
                    icon = Icons.Default.Quiz
                )
            }
        } else {
            items(quizzes, key = { it.id }) { quiz ->
                SavedQuizCard(
                    savedQuiz = quiz,
                    onReview = { onReviewQuiz(quiz) },
                    onRetake = { onRetakeQuiz(quiz) },
                    onToggleFavorite = { onToggleFavorite(quiz) },
                    onDelete = { onDelete(quiz) }
                )
            }
        }
    }
}

@Composable
fun SavedQuizCard(
    savedQuiz: SavedPracticeQuizEntity,
    onReview: () -> Unit,
    onRetake: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_quiz_${savedQuiz.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = savedQuiz.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = savedQuiz.difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (savedQuiz.isFavorite) Icons.Default.Star else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite quiz",
                            tint = if (savedQuiz.isFavorite) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete quiz",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = savedQuiz.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${savedQuiz.questionCount} Questions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (savedQuiz.isCompleted && savedQuiz.lastPercentage != null) {
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (savedQuiz.lastPercentage >= 70) SuccessLight else AmberTertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Score: ${savedQuiz.lastPercentage}% (${savedQuiz.lastScore}/${savedQuiz.questionCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (savedQuiz.lastPercentage >= 70) SuccessGreen else AmberTertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (savedQuiz.isCompleted) {
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("review_saved_quiz_${savedQuiz.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Answers", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onRetake,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("retake_saved_quiz_${savedQuiz.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (savedQuiz.isCompleted) "Retake Quiz" else "Start Quiz", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Bookmark
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
