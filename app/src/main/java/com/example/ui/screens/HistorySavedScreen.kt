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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.SavedSolutionEntity
import com.example.ui.Screen
import com.example.ui.StudyViewModel

@Composable
fun HistorySavedScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo(Screen.Home) }

    val savedSolutions by viewModel.savedSolutions.collectAsState()
    val savedQuizzes by viewModel.savedQuizzes.collectAsState()
    val quizRecords by viewModel.quizRecords.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Saved Solutions (${savedSolutions.size})", "Practice Tests (${quizRecords.size})")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Saved & Study History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                    }
                }
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTabIndex == 0) {
                // Saved Solutions
                if (savedSolutions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Saved Solutions Yet",
                            description = "When you solve a question in Ask StudyAI, tap the bookmark icon to save it here for quick revision.",
                            icon = Icons.Default.Bookmark
                        )
                    }
                } else {
                    items(savedSolutions, key = { it.id }) { solution ->
                        SavedSolutionCard(
                            entity = solution,
                            onRevisit = { viewModel.openSavedSolution(solution) },
                            onToggleFavorite = { viewModel.toggleFavorite(solution.id, solution.isFavorite) },
                            onDelete = { viewModel.deleteSavedSolution(solution.id) }
                        )
                    }
                }
            } else {
                // Practice Quizzes & Records
                if (quizRecords.isEmpty() && savedQuizzes.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Past Quizzes Found",
                            description = "Take interactive quizzes in the Quizzes tab. Your score history and analytics will appear here.",
                            icon = Icons.Default.Quiz
                        )
                    }
                } else {
                    if (savedQuizzes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Saved Quizzes for Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(savedQuizzes, key = { it.id }) { sq ->
                            SavedQuizCard(
                                savedQuiz = sq,
                                onReview = { viewModel.openSavedQuizForReview(sq) },
                                onRetake = { viewModel.retakeSavedQuiz(sq) },
                                onToggleFav = { viewModel.toggleSavedQuizFavorite(sq.id, sq.isFavorite) },
                                onDelete = { viewModel.deleteSavedQuiz(sq.id) }
                            )
                        }
                    }

                    if (quizRecords.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Score History & Attempts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(quizRecords, key = { it.id }) { record ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (record.percentage >= 70) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${record.percentage}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (record.percentage >= 70) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${record.correctCount}/${record.totalQuestions} correct • ${record.difficulty}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteQuizRecord(record.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedSolutionCard(
    entity: SavedSolutionEntity,
    onRevisit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = entity.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = if (entity.isFavorite) Color(0xFFF59E0B) else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entity.question,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ans: ${entity.directAnswer}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRevisit,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Revisit Solution",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = savedQuiz.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = savedQuiz.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "${savedQuiz.questionCount} Questions • ${savedQuiz.difficulty} • Score: ${savedQuiz.lastPercentage ?: 0}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onReview,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Review", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onRetake,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Retake", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
