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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AttachedFile
import com.example.ui.components.AttachedFilesPreviewRow
import com.example.ui.components.AttachmentOptionsBottomSheet
import com.example.ui.components.decodeBitmapFromUri
import com.example.ui.components.queryFileDetails
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun SolveQuestionScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo(Screen.Home) }

    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val questionInput by viewModel.questionInput.collectAsState()
    val explanationStyle by viewModel.explanationStyle.collectAsState()
    val solverUiState by viewModel.solverUiState.collectAsState()
    val currentChatMessages by viewModel.currentChatMessages.collectAsState()
    val followUpInput by viewModel.followUpInput.collectAsState()
    val isSendingFollowUp by viewModel.isSendingFollowUp.collectAsState()

    val context = LocalContext.current
    var showMainAttachmentSheet by remember { mutableStateOf(false) }
    val mainAttachedFiles = remember { mutableStateListOf<AttachedFile>() }

    var showFollowUpAttachmentSheet by remember { mutableStateOf(false) }
    val followUpAttachedFiles = remember { mutableStateListOf<AttachedFile>() }

    if (showMainAttachmentSheet) {
        AttachmentOptionsBottomSheet(
            onDismiss = { showMainAttachmentSheet = false },
            onPhotoTaken = { bitmap ->
                mainAttachedFiles.add(AttachedFile(name = "Camera Photo", mimeType = "image/jpeg", bitmap = bitmap))
            },
            onVideoRecorded = { uri, thumb ->
                mainAttachedFiles.add(AttachedFile(uri = uri, name = "Recorded Video", mimeType = "video/mp4", bitmap = thumb, isVideo = true))
            },
            onMediaPicked = { uri, isVideo ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                val bitmap = if (!isVideo) decodeBitmapFromUri(context, uri) else null
                mainAttachedFiles.add(AttachedFile(uri = uri, name = name, mimeType = if (isVideo) "video/mp4" else "image/jpeg", sizeText = sizeStr, bitmap = bitmap, isVideo = isVideo))
            },
            onDocumentPicked = { uri ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                mainAttachedFiles.add(AttachedFile(uri = uri, name = name, mimeType = "application/pdf", sizeText = sizeStr, isDocument = true))
            }
        )
    }

    if (showFollowUpAttachmentSheet) {
        AttachmentOptionsBottomSheet(
            onDismiss = { showFollowUpAttachmentSheet = false },
            onPhotoTaken = { bitmap ->
                followUpAttachedFiles.add(AttachedFile(name = "Camera Photo", mimeType = "image/jpeg", bitmap = bitmap))
            },
            onVideoRecorded = { uri, thumb ->
                followUpAttachedFiles.add(AttachedFile(uri = uri, name = "Recorded Video", mimeType = "video/mp4", bitmap = thumb, isVideo = true))
            },
            onMediaPicked = { uri, isVideo ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                val bitmap = if (!isVideo) decodeBitmapFromUri(context, uri) else null
                followUpAttachedFiles.add(AttachedFile(uri = uri, name = name, mimeType = if (isVideo) "video/mp4" else "image/jpeg", sizeText = sizeStr, bitmap = bitmap, isVideo = isVideo))
            },
            onDocumentPicked = { uri ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                followUpAttachedFiles.add(AttachedFile(uri = uri, name = name, mimeType = "application/pdf", sizeText = sizeStr, isDocument = true))
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("solve_question_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subject Selector
        item {
            Column {
                Text(
                    text = "Select Academic Field",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SubjectCatalog.subjects) { subj ->
                        FilterChip(
                            selected = selectedSubject.id == subj.id,
                            onClick = { viewModel.selectSubject(subj) },
                            label = { Text("${subj.iconEmoji} ${subj.name}", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Question Input Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enter Problem or Upload File",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Upload / Attachment button like ChatGPT
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { showMainAttachmentSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Attach media or files",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Upload",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Attached preview row
                    AttachedFilesPreviewRow(
                        attachedFiles = mainAttachedFiles,
                        onRemove = { mainAttachedFiles.remove(it) },
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Quick Camera shortcut icon beside writing box
                        IconButton(
                            onClick = { showMainAttachmentSheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("btn_main_attach_shortcut")
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Camera & Upload",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { viewModel.setQuestionInput(it) },
                            placeholder = { Text("Type problem, or snap homework photo / video / PDF...") },
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .testTag("input_solve_question"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            trailingIcon = {
                                if (questionInput.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setQuestionInput("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanation Style Selector
                    Text(
                        text = "Explanation Style",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ExplanationStyle.entries.toTypedArray()) { style ->
                            FilterChip(
                                selected = explanationStyle == style,
                                onClick = { viewModel.setExplanationStyle(style) },
                                label = { Text(style.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val firstImg = mainAttachedFiles.firstOrNull { it.bitmap != null }?.bitmap
                                viewModel.solveQuestion(imageBitmap = firstImg)
                            },
                            enabled = (questionInput.isNotBlank() || mainAttachedFiles.isNotEmpty()) && solverUiState !is SolverUiState.Loading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_solve_question")
                        ) {
                            if (solverUiState is SolverUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Solve with StudyAI", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Discuss with Study AI Live button
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.StudyAiLive) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_talk_live_from_solver")
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Talk Live", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Solution Result View
        when (val state = solverUiState) {
            is SolverUiState.Loading -> {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Deriving Step-by-Step Solution...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Applying academic reasoning & cross-verifying formulas",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is SolverUiState.Success -> {
                item {
                    SolutionView(
                        solution = state.solution,
                        isSaved = state.isSaved,
                        onSave = { viewModel.saveCurrentSolution() },
                        onTalkLive = { viewModel.navigateTo(Screen.StudyAiLive) },
                        onStartQuiz = { viewModel.startQuizFromCurrentQuestion() }
                    )
                }

                // Follow-up Chat Session
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Ask Clarifying Questions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentChatMessages.size} replies",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            currentChatMessages.forEach { msg ->
                                ChatBubble(message = msg)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Preview of attached media/files for follow-up
                            AttachedFilesPreviewRow(
                                attachedFiles = followUpAttachedFiles,
                                onRemove = { followUpAttachedFiles.remove(it) },
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Upload button on the side of text writing box (like ChatGPT)
                                IconButton(
                                    onClick = { showFollowUpAttachmentSheet = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .testTag("btn_follow_up_upload")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Upload Photo, Video or File",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedTextField(
                                    value = followUpInput,
                                    onValueChange = { viewModel.setFollowUpInput(it) },
                                    placeholder = { Text("Ask follow-up or discuss attached file...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_follow_up"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val attachSummary = followUpAttachedFiles.joinToString(", ") { it.name }.takeIf { it.isNotBlank() }
                                        viewModel.sendFollowUpMessage(attachmentContext = attachSummary)
                                        followUpAttachedFiles.clear()
                                    },
                                    enabled = (followUpInput.isNotBlank() || followUpAttachedFiles.isNotEmpty()) && !isSendingFollowUp,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .testTag("btn_send_follow_up")
                                ) {
                                    if (isSendingFollowUp) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is SolverUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            SolverUiState.Idle -> {}
        }
    }
}

@Composable
fun SolutionView(
    solution: QuestionSolution,
    isSaved: Boolean,
    onSave: () -> Unit,
    onTalkLive: () -> Unit,
    onStartQuiz: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Save and Talk Live shortcuts
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
                        text = solution.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onTalkLive, modifier = Modifier.testTag("btn_solution_talk_live")) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Discuss Live",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onSave, modifier = Modifier.testTag("btn_save_solution")) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Solution",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct Answer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Final Answer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = solution.directAnswer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Steps
            Text("Step-by-Step Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            solution.steps.forEach { step ->
                StepCard(step = step)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Core Concepts
            if (solution.coreConcepts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Core Academic Concepts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(solution.coreConcepts) { concept ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(concept, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // Tips & Caveats
            if (solution.tipsAndCommonMistakes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Caveats & Common Traps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                solution.tipsAndCommonMistakes.forEach { tip ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(tip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Practice Question
            if (solution.followUpPractice.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Practice Quiz on This Topic", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepCard(step: SolutionStep) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${step.stepNumber}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step.explanation,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!step.formulaOrDetail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = step.formulaOrDetail,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"
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
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (isUser) "You" else "Study AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
