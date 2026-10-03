package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.QuizDifficulty
import com.example.data.model.SubjectCatalog
import com.example.ui.QuizFlowState
import com.example.ui.Screen
import com.example.ui.StudyViewModel

@Composable
fun QuizScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        val flowState = viewModel.quizFlowState.value
        if (flowState !is QuizFlowState.Setup) {
            viewModel.resetQuizSetup()
        } else {
            viewModel.navigateTo(Screen.Home)
        }
    }

    val quizFlowState by viewModel.quizFlowState.collectAsState()

    when (val state = quizFlowState) {
        is QuizFlowState.Setup -> {
            QuizSetupView(viewModel = viewModel, modifier = modifier)
        }
        is QuizFlowState.Generating -> {
            QuizGeneratingView(modifier = modifier)
        }
        is QuizFlowState.Active -> {
            QuizActiveView(state = state, viewModel = viewModel, modifier = modifier)
        }
        is QuizFlowState.Completed -> {
            QuizCompletedView(state = state, viewModel = viewModel, modifier = modifier)
        }
        is QuizFlowState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Generation Failed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.resetQuizSetup() }) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun QuizSetupView(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quizSubject by viewModel.quizSubject.collectAsState()
    val quizTopic by viewModel.quizTopic.collectAsState()
    val quizDifficulty by viewModel.quizDifficulty.collectAsState()
    val quizCount by viewModel.quizQuestionCount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("quiz_setup_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Interactive Practice Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Generate tailored academic quizzes with step-by-step explanations.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("1. Academic Field", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SubjectCatalog.subjects) { subj ->
                            FilterChip(
                                selected = quizSubject.id == subj.id,
                                onClick = { viewModel.selectQuizSubject(subj) },
                                label = { Text("${subj.iconEmoji} ${subj.name}", fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("2. Topic or Chapter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = quizTopic,
                        onValueChange = { viewModel.setQuizTopic(it) },
                        placeholder = { Text("e.g. Calculus Derivatives, Quantum Spin, Mitochondria...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quiz_topic"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Popular topic suggestions
                    if (quizSubject.popularTopics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quizSubject.popularTopics) { topic ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { viewModel.setQuizTopic(topic) }
                                ) {
                                    Text(
                                        text = topic,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("3. Difficulty Level", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuizDifficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = quizDifficulty == diff,
                                onClick = { viewModel.setQuizDifficulty(diff) },
                                label = { Text(diff.label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("4. Question Count", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 5, 10).forEach { count ->
                            FilterChip(
                                selected = quizCount == count,
                                onClick = { viewModel.setQuizQuestionCount(count) },
                                label = { Text("$count Questions", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.generateQuiz() },
                        enabled = quizTopic.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_generate_quiz")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Quiz with AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizGeneratingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Crafting High-Yield Questions...",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Designing multiple choice options, distractor analysis, and detailed explanations.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuizActiveView(
    state: QuizFlowState.Active,
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val quiz = state.quiz
    val currentIdx = state.currentQuestionIndex
    val question = quiz.questions.getOrNull(currentIdx) ?: quiz.questions.first()
    val total = quiz.questions.size
    val selectedOption = state.userAnswers[question.id]
    val isChecked = state.isAnswerChecked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${quiz.subject} • Question ${currentIdx + 1} of $total",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = quiz.difficulty,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (currentIdx + 1).toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = question.conceptTested,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = question.questionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Options
        question.options.forEachIndexed { optIdx, optText ->
            val isOptionSelected = selectedOption == optIdx
            val isCorrect = optIdx == question.correctIndex
            val borderColor = when {
                !isChecked && isOptionSelected -> MaterialTheme.colorScheme.primary
                isChecked && isCorrect -> Color(0xFF10B981)
                isChecked && isOptionSelected && !isCorrect -> MaterialTheme.colorScheme.error
                else -> Color.Transparent
            }
            val bgColor = when {
                !isChecked && isOptionSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                isChecked && isCorrect -> Color(0xFFD1FAE5)
                isChecked && isOptionSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isChecked) {
                        viewModel.selectQuizAnswer(question.id, optIdx)
                    }
                    .testTag("quiz_option_$optIdx")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = when (optIdx) {
                        0 -> "A"; 1 -> "B"; 2 -> "C"; else -> "D"
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isOptionSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isOptionSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = optText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isChecked) {
                        if (isCorrect) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        } else if (isOptionSelected) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Explanation if checked
        if (isChecked) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Explanation:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = question.explanation, fontSize = 13.sp, lineHeight = 17.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation bottom controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.previousQuestion() },
                enabled = currentIdx > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!isChecked) {
                Button(
                    onClick = { viewModel.checkAnswer() },
                    enabled = selectedOption != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_check_answer")
                ) {
                    Text("Check Answer", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.nextQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_next_question")
                ) {
                    Text(if (currentIdx == total - 1) "Finish Quiz" else "Next Question", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quiz Complete!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${state.quiz.title} • ${state.quiz.difficulty}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Score Badge
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.percentage >= 70) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.score}/${state.total}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = if (state.percentage >= 70) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                            Text(
                                text = "${state.percentage}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.percentage >= 70) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.retakeCurrentQuiz(state.quiz) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake Quiz")
                        }

                        // Discuss with Study AI Live button
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.StudyAiLive) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Discuss Live")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Question Review",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Review Question Cards
        items(state.quiz.questions) { q ->
            val userAns = state.userAnswers[q.id]
            val isCorrect = userAns == q.correctIndex
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = q.questionText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Correct Answer: ${q.options[q.correctIndex]}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                    Text(
                        text = q.explanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
