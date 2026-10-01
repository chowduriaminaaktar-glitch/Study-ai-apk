package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ExplanationStyle
import com.example.data.model.QuestionSolution
import com.example.data.model.SolutionStep
import com.example.data.model.SubjectCatalog
import com.example.ui.Screen
import com.example.ui.SolverUiState
import com.example.ui.StudyViewModel
import com.example.ui.components.ApiKeyStatusBanner
import com.example.ui.components.VoiceDictationBottomSheet
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SolveQuestionScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val context = LocalContext.current
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val questionInput by viewModel.questionInput.collectAsState()
    val explanationStyle by viewModel.explanationStyle.collectAsState()
    val solverState by viewModel.solverUiState.collectAsState()

    val currentChatSession by viewModel.currentChatSession.collectAsState()
    val currentChatMessages by viewModel.currentChatMessages.collectAsState()
    val followUpInput by viewModel.followUpInput.collectAsState()
    val isSendingFollowUp by viewModel.isSendingFollowUp.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()

    var showVoiceDictationSheet by remember { mutableStateOf(false) }
    var isDictatingFollowUp by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("solve_question_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ApiKeyStatusBanner(isConfigured = viewModel.isApiKeyConfigured)
        }

        // Active Chat Session Banner or Previous Sessions Bar
        item {
            if (currentChatSession != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_session_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Study Session: ${currentChatSession?.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentChatSession?.subject} • ${currentChatMessages.size} messages",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            OutlinedButton(
                                onClick = { viewModel.startNewChatSession() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("new_study_session_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Chat", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else if (chatSessions.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Previous Study Chats",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "View All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.SavedAndHistory) }
                                .padding(4.dp)
                                .testTag("view_all_chat_sessions")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatSessions.take(4)) { session ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.loadChatSession(session) }
                                    .testTag("recent_session_chip_${session.id}"),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = session.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subject Selector Chips
        item {
            Column {
                Text(
                    text = "Select Academic Subject",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SubjectCatalog.subjects) { sub ->
                        val isSelected = sub.id == selectedSubject.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectSubject(sub) },
                            label = { Text(sub.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = sub.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else sub.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("subject_chip_${sub.id}")
                        )
                    }
                }
            }
        }

        // Question Input Field & Speech Dictation Banner
        item {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            isDictatingFollowUp = false
                            showVoiceDictationSheet = true
                        }
                        .testTag("voice_dictation_banner"),
                    color = IndigoPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dictate Academic Question",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                                Text(
                                    text = "Speak formulas, math, or chemistry with AI transcription",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Voice Input",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { viewModel.setQuestionInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .testTag("question_input_field"),
                    label = { Text("Ask any question in ${selectedSubject.name}") },
                    placeholder = { Text("Type, paste, or tap the mic to dictate formulas, theorems, derivations...") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isDictatingFollowUp = false
                                    showVoiceDictationSheet = true
                                },
                                modifier = Modifier.testTag("dictate_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Dictate question",
                                    tint = IndigoPrimary
                                )
                            }
                            if (questionInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setQuestionInput("") },
                                    modifier = Modifier.testTag("clear_question_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear input")
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick Math & Academic Symbol Shortcuts
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Symbols:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val quickSymbols = listOf("²", "³", "√", "∫", "±", "π", "θ", "Δ", "∞", "≠", "≤", "≥", "→", "÷", "×", "d/dx")
                    quickSymbols.forEach { sym ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setQuestionInput("$questionInput$sym")
                                }
                                .testTag("quick_sym_$sym"),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = sym,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sample Questions Chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedSubject.sampleQuestions.forEach { sampleQ ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.pickSampleQuestion(sampleQ) }
                                .testTag("sample_q_chip"),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sampleQ,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Explanation Style Selector
        item {
            Column {
                Text(
                    text = "Explanation Depth",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExplanationStyle.values().forEach { style ->
                        val isSelected = style == explanationStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setExplanationStyle(style) },
                            label = { Text(style.label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("style_chip_${style.name}")
                        )
                    }
                }
            }
        }

        // Solve Action Button
        item {
            Button(
                onClick = { viewModel.solveQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("solve_submit_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = questionInput.isNotBlank() && solverState !is SolverUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                if (solverState is SolverUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Solving & Saving to Room...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solve with StudyAI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Solution Display Area
        item {
            when (val state = solverState) {
                is SolverUiState.Idle -> {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ask any question above",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Every study chat session is automatically saved to the local Room database.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is SolverUiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(44.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzing academic principles...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Deriving steps, formulas, and creating Room chat session...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is SolverUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unable to solve",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                is SolverUiState.Success -> {
                    SolutionView(
                        solution = state.solution,
                        isSaved = state.isSaved,
                        onBookmarkClick = {
                            viewModel.saveCurrentSolution(state.solution)
                            Toast.makeText(context, "Solution saved to Room Database!", Toast.LENGTH_SHORT).show()
                        },
                        onCopyClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("StudyAI Solution", buildCopyText(state.solution))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Full solution copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onGenerateQuizClick = {
                            viewModel.quizFromSolution(state.solution)
                        }
                    )
                }
            }
        }

        // Conversational Follow-Up Thread & Input
        if (currentChatSession != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("follow_up_discussion_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Session Q&A & Follow-Ups",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Ask questions about this derivation or request clarifications",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Show messages from this session
                        val displayMessages = if (solverState is SolverUiState.Success) {
                            currentChatMessages.filterIndexed { index, _ -> index >= 2 }
                        } else {
                            currentChatMessages
                        }
                        if (displayMessages.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                displayMessages.forEach { msg ->
                                    ChatBubble(message = msg)
                                }
                            }
                        }

                        // Quick follow-up suggestions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val suggestions = listOf(
                                "Explain Step 2 in more detail",
                                "What are the SI units & dimensions?",
                                "Show an alternative method to solve this",
                                "What is an intuitive analogy for this?"
                            )
                            suggestions.forEach { sugg ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.setFollowUpInput(sugg) }
                                        .testTag("suggestion_chip"),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = sugg,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Follow-up Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = followUpInput,
                                onValueChange = { viewModel.setFollowUpInput(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("follow_up_input_field"),
                                placeholder = { Text("Ask follow-up (e.g. clarify step 2)...", fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isDictatingFollowUp = true
                                            showVoiceDictationSheet = true
                                        },
                                        modifier = Modifier.testTag("follow_up_mic_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Dictate follow-up",
                                            tint = IndigoPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.sendFollowUpMessage() },
                                enabled = followUpInput.isNotBlank() && !isSendingFollowUp,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (followUpInput.isNotBlank() && !isSendingFollowUp) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("send_follow_up_button")
                            ) {
                                if (isSendingFollowUp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send follow-up",
                                        tint = if (followUpInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVoiceDictationSheet) {
        VoiceDictationBottomSheet(
            subjectName = selectedSubject.name,
            existingPrompt = if (isDictatingFollowUp) followUpInput else questionInput,
            onDismiss = { showVoiceDictationSheet = false },
            onApplyText = { spokenText, append ->
                if (isDictatingFollowUp) {
                    if (append && followUpInput.isNotBlank()) {
                        viewModel.setFollowUpInput("${followUpInput.trimEnd()} ${spokenText.trim()}")
                    } else {
                        viewModel.setFollowUpInput(spokenText.trim())
                    }
                } else {
                    if (append) {
                        viewModel.appendSpokenText(spokenText)
                    } else {
                        viewModel.replaceSpokenText(spokenText)
                    }
                }
            }
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "USER"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "Student" else "StudyAI Tutor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else IndigoPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SolutionView(
    solution: QuestionSolution,
    isSaved: Boolean,
    onBookmarkClick: () -> Unit,
    onCopyClick: () -> Unit,
    onGenerateQuizClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.testTag("solution_view_container")
    ) {
        // Direct Answer Box
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Direct Answer",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onCopyClick,
                            modifier = Modifier.testTag("copy_solution_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy solution",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onBookmarkClick,
                            modifier = Modifier.testTag("bookmark_solution_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save solution",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = solution.directAnswer,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Steps Breakdown
        Text(
            text = "Step-by-Step Derivation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        solution.steps.forEach { step ->
            StepCard(step = step)
        }

        // Core Concepts
        if (solution.coreConcepts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmberTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Core Concepts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        solution.coreConcepts.forEach { concept ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = concept,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tips & Common Traps
        if (solution.tipsAndCommonMistakes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AmberTertiary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pro Tips & Common Exam Traps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmberDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    solution.tipsAndCommonMistakes.forEach { tip ->
                        Text(
                            text = "• $tip",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Follow Up Practice Challenge
        if (solution.followUpPractice.isNotBlank()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Follow-Up Practice Challenge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = solution.followUpPractice,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action: Practice Quiz from this topic
        Button(
            onClick = onGenerateQuizClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_quiz_from_solution_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Practice Quiz on This Topic")
        }
    }
}

@Composable
fun StepCard(step: SolutionStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_card_${step.stepNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.stepNumber.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.explanation,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!step.formulaOrDetail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = step.formulaOrDetail,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

private fun buildCopyText(s: QuestionSolution): String {
    val sb = StringBuilder()
    sb.appendLine("Question: ${s.question}")
    sb.appendLine("Subject: ${s.subject}")
    sb.appendLine()
    sb.appendLine("Direct Answer: ${s.directAnswer}")
    sb.appendLine()
    sb.appendLine("Step-by-Step Derivation:")
    s.steps.forEach { step ->
        sb.appendLine("${step.stepNumber}. ${step.title}")
        sb.appendLine(step.explanation)
        if (!step.formulaOrDetail.isNullOrBlank()) {
            sb.appendLine("   Formula: ${step.formulaOrDetail}")
        }
        sb.appendLine()
    }
    if (s.coreConcepts.isNotEmpty()) {
        sb.appendLine("Core Concepts: ${s.coreConcepts.joinToString(", ")}")
    }
    if (s.tipsAndCommonMistakes.isNotEmpty()) {
        sb.appendLine("Tips & Common Traps:")
        s.tipsAndCommonMistakes.forEach { sb.appendLine("• $it") }
    }
    return sb.toString()
}
