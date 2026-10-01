package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import com.example.data.local.SavedPracticeQuizEntity
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizQuestion
import com.example.data.model.SubjectCatalog
import com.example.ui.QuizFlowState
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import com.example.ui.components.ApiKeyStatusBanner
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.ErrorLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessLight

@Composable
fun QuizScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quizFlow by viewModel.quizFlowState.collectAsState()

    BackHandler {
        when (quizFlow) {
            is QuizFlowState.Active, is QuizFlowState.Completed -> viewModel.resetQuizSetup()
            else -> viewModel.navigateTo(Screen.Home)
        }
    }

    when (val state = quizFlow) {
        is QuizFlowState.Setup -> {
            QuizSetupView(viewModel = viewModel, modifier = modifier)
        }
        is QuizFlowState.Generating -> {
            QuizGeneratingView(modifier = modifier)
        }
        is QuizFlowState.Active -> {
            QuizActiveView(
                state = state,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        is QuizFlowState.Completed -> {
            QuizCompletedView(
                state = state,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizSetupView(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quizSubject by viewModel.quizSubject.collectAsState()
    val quizTopic by viewModel.quizTopic.collectAsState()
    val quizDifficulty by viewModel.quizDifficulty.collectAsState()
    val quizQuestionCount by viewModel.quizQuestionCount.collectAsState()
    val savedQuizzes by viewModel.savedQuizzes.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_setup_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ApiKeyStatusBanner(isConfigured = viewModel.isApiKeyConfigured)
        }

        // Header Title Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Personalized Practice Quiz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "AI crafts exam-style questions with detailed rationale.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Subject Selector
        item {
            Column {
                Text(
                    text = "1. Choose Academic Subject",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SubjectCatalog.subjects) { sub ->
                        val isSelected = sub.id == quizSubject.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setQuizSubject(sub) },
                            label = { Text(sub.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = sub.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else sub.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("quiz_subject_${sub.id}")
                        )
                    }
                }
            }
        }

        // Topic Selector / Input
        item {
            Column {
                Text(
                    text = "2. Specific Topic / Chapter",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quizTopic,
                    onValueChange = { viewModel.setQuizTopic(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_topic_input"),
                    label = { Text("Topic name") },
                    placeholder = { Text("e.g. Newton's Laws, Integral Calculus, Cell Mitosis...") },
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Topic Suggestions
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quizSubject.popularTopics.forEach { topic ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setQuizTopic(topic) }
                                .testTag("topic_suggestion_$topic"),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Difficulty Selector
        item {
            Column {
                Text(
                    text = "3. Target Difficulty Level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuizDifficulty.values().forEach { diff ->
                        val isSelected = diff == quizDifficulty
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setQuizDifficulty(diff) },
                            label = { Text(diff.label, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("difficulty_${diff.name}")
                        )
                    }
                }
            }
        }

        // Number of Questions
        item {
            Column {
                Text(
                    text = "4. Number of Questions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(3, 5, 10).forEach { count ->
                        val isSelected = count == quizQuestionCount
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setQuizQuestionCount(count) },
                            label = { Text("$count Questions", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("count_$count")
                        )
                    }
                }
            }
        }

        // Launch Quiz Button
        item {
            Button(
                onClick = { viewModel.startQuizGeneration() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_quiz_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = quizTopic.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Personalized Quiz",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Saved Practice Quizzes (Stored in Room DB)
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saved Practice Quizzes (${savedQuizzes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Stored in Room database for reviewing question answers & retakes",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (savedQuizzes.isNotEmpty()) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanSecondary,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.SavedAndHistory) }
                                .padding(4.dp)
                                .testTag("quiz_view_all_saved_button")
                        )
                    }
                }
            }
        }

        if (savedQuizzes.isEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = CyanSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Saved Practice Quizzes Yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate a quiz above! Quizzes, user answers, scores, and explanations are automatically preserved in the Room database.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedQuizzes, key = { it.id }) { savedQuiz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_quiz_card_${savedQuiz.id}"),
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
                                        text = "${savedQuiz.questionCount} Questions • ${savedQuiz.difficulty}",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (savedQuiz.lastPercentage != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (savedQuiz.lastPercentage >= 70) SuccessGreen.copy(alpha = 0.2f) else AmberTertiary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${savedQuiz.lastPercentage}% (${savedQuiz.lastScore}/${savedQuiz.questionCount})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (savedQuiz.lastPercentage >= 70) SuccessGreen else AmberTertiary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = savedQuiz.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Topic: ${savedQuiz.topic}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.retakeSavedQuiz(savedQuiz) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("retake_quiz_btn_${savedQuiz.id}")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retake", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.openSavedQuizForReview(savedQuiz) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("review_quiz_btn_${savedQuiz.id}")
                            ) {
                                Text("Review Answers", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizGeneratingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("quiz_generating_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = CyanSecondary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Crafting Personalized Quiz...",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formulating concept questions, distractor options, and explanations",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizActiveView(
    state: QuizFlowState.Active,
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quiz = state.quiz
    val currentQ = quiz.questions[state.currentQuestionIndex]
    val selectedOptionIndex = state.userAnswers[currentQ.id]
    val isAnswered = selectedOptionIndex != null
    val isLastQuestion = state.currentQuestionIndex == quiz.questions.size - 1

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_active_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Progress and stats row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${state.currentQuestionIndex + 1} of ${quiz.questions.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = quiz.difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (state.currentQuestionIndex + 1).toFloat() / quiz.questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Question Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_question_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = currentQ.conceptTested,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentQ.questionText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Option Cards
        items(currentQ.options.size) { optIndex ->
            val optionText = currentQ.options[optIndex]
            val isSelected = selectedOptionIndex == optIndex
            val isCorrect = optIndex == currentQ.correctIndex

            val containerColor = when {
                !isAnswered -> MaterialTheme.colorScheme.surface
                isSelected && isCorrect -> SuccessLight
                isSelected && !isCorrect -> ErrorLight
                isCorrect -> SuccessLight
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                isAnswered && isCorrect -> SuccessGreen
                isAnswered && isSelected && !isCorrect -> ErrorRed
                else -> Color.Transparent
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isAnswered) {
                        viewModel.selectQuizAnswer(currentQ.id, optIndex)
                    }
                    .testTag("quiz_option_$optIndex"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                border = if (borderColor != Color.Transparent) {
                    androidx.compose.foundation.BorderStroke(2.dp, borderColor)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isAnswered && isCorrect -> SuccessGreen
                                    isAnswered && isSelected && !isCorrect -> ErrorRed
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = ('A' + optIndex).toString()
                        if (isAnswered && isCorrect) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else if (isAnswered && isSelected && !isCorrect) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = optionText,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected || (isAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isAnswered && isCorrect -> Color(0xFF065F46)
                            isAnswered && isSelected && !isCorrect -> Color(0xFF991B1B)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Instant Explanation Card (visible once answered)
        if (isAnswered) {
            item {
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_explanation_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOptionIndex == currentQ.correctIndex) {
                                SuccessGreen.copy(alpha = 0.12f)
                            } else {
                                AmberTertiary.copy(alpha = 0.12f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (selectedOptionIndex == currentQ.correctIndex) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.Lightbulb
                                    },
                                    contentDescription = null,
                                    tint = if (selectedOptionIndex == currentQ.correctIndex) SuccessGreen else AmberTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedOptionIndex == currentQ.correctIndex) {
                                        "Correct! Key Concept Insight"
                                    } else {
                                        "Explanation & Concept Review"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuizQuestion() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quiz_prev_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    shape = RoundedCornerShape(12.dp),
                    enabled = isAnswered,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.testTag("quiz_next_button")
                ) {
                    Text(if (isLastQuestion) "Finish & View Score" else "Next Question")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuizCompletedView(
    state: QuizFlowState.Completed,
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quiz = state.quiz

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_completed_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Header Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    state.percentage >= 80 -> SuccessLight
                                    state.percentage >= 50 -> AmberTertiary.copy(alpha = 0.2f)
                                    else -> ErrorLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = when {
                                state.percentage >= 80 -> SuccessGreen
                                state.percentage >= 50 -> AmberTertiary
                                else -> ErrorRed
                            },
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "${state.percentage}%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${state.score} of ${state.total} Correct",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            state.percentage == 100 -> "Perfect Mastery! You fully grasped ${quiz.topic}."
                            state.percentage >= 80 -> "Excellent understanding! Ready for advanced challenges."
                            state.percentage >= 60 -> "Solid effort! Review the explanations below."
                            else -> "Keep studying! Try reviewing the concepts and retake."
                        },
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.retakeCurrentQuiz(quiz) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retake_quiz_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake")
                        }

                        Button(
                            onClick = { viewModel.resetQuizSetup() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_quiz_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                        ) {
                            Text("New Quiz")
                        }
                    }
                }
            }
        }

        // Section Title: Detailed Review
        item {
            Text(
                text = "Question-by-Question Review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Full Review of each question
        items(quiz.questions.size) { index ->
            val q = quiz.questions[index]
            val userPick = state.userAnswers[q.id]
            val isCorrect = userPick == q.correctIndex

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_card_$index"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCorrect) SuccessLight else ErrorLight
                        ) {
                            Text(
                                text = if (isCorrect) "CORRECT" else "INCORRECT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) SuccessGreen else ErrorRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = q.questionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    if (userPick != null && userPick != q.correctIndex) {
                        Text(
                            text = "Your Answer: ${q.options[userPick]}",
                            fontSize = 12.sp,
                            color = ErrorRed
                        )
                    }
                    Text(
                        text = "Correct Answer: ${q.options[q.correctIndex]}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = q.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
