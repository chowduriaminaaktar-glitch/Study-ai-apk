package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import com.example.ui.components.AttachedFile
import com.example.ui.components.AttachedFilesPreviewRow
import com.example.ui.components.AttachmentOptionsBottomSheet
import com.example.ui.components.ChatAttachmentsGrid
import com.example.ui.components.VoiceDictationBottomSheet
import com.example.ui.components.decodeBitmapFromUri
import com.example.ui.components.deserializeAttachedFiles
import com.example.ui.components.queryFileDetails
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo(Screen.Home) }

    val context = LocalContext.current
    val currentSessionId by viewModel.currentChatSessionId.collectAsState()
    val currentSession by viewModel.currentChatSession.collectAsState()
    val messages by viewModel.currentChatMessages.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()

    val pendingAttachments = remember { mutableStateListOf<AttachedFile>() }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showVoiceDictationSheet by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }

    // TextToSpeech engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.getDefault()
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, isChatLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Attachment Options Bottom Sheet (Camera Photo, Camera Video, Photos, Videos, Files)
    if (showAttachmentSheet) {
        AttachmentOptionsBottomSheet(
            onDismiss = { showAttachmentSheet = false },
            onPhotoTaken = { bitmap ->
                pendingAttachments.add(
                    AttachedFile(
                        name = "Camera Photo",
                        mimeType = "image/jpeg",
                        bitmap = bitmap
                    )
                )
            },
            onVideoRecorded = { uri, thumb ->
                pendingAttachments.add(
                    AttachedFile(
                        uri = uri,
                        name = "Recorded Video",
                        mimeType = "video/mp4",
                        bitmap = thumb,
                        isVideo = true
                    )
                )
            },
            onMediaPicked = { uri, isVideo ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                val bitmap = if (!isVideo) decodeBitmapFromUri(context, uri) else null
                pendingAttachments.add(
                    AttachedFile(
                        uri = uri,
                        name = name,
                        mimeType = if (isVideo) "video/mp4" else "image/jpeg",
                        sizeText = sizeStr,
                        bitmap = bitmap,
                        isVideo = isVideo
                    )
                )
            },
            onDocumentPicked = { uri ->
                val (name, sizeStr) = queryFileDetails(context, uri)
                pendingAttachments.add(
                    AttachedFile(
                        uri = uri,
                        name = name,
                        mimeType = "application/pdf",
                        sizeText = sizeStr,
                        isDocument = true
                    )
                )
            }
        )
    }

    // Voice Dictation Bottom Sheet
    if (showVoiceDictationSheet) {
        VoiceDictationBottomSheet(
            onDismissRequest = { showVoiceDictationSheet = false },
            onSpeechResult = { text ->
                viewModel.appendChatVoiceText(text)
                showVoiceDictationSheet = false
            }
        )
    }

    // Previous Chat Sessions Dialog
    if (showSessionsDialog) {
        ChatSessionsListDialog(
            sessions = chatSessions,
            currentSessionId = currentSessionId,
            onSelectSession = { session ->
                viewModel.openChatSession(session)
                showSessionsDialog = false
            },
            onDeleteSession = { sid ->
                viewModel.deleteChatSession(sid)
            },
            onDismiss = { showSessionsDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Top Bar
        ChatHeaderBar(
            sessionTitle = currentSession?.title ?: "New Chat",
            hasActiveSession = currentSessionId != null,
            onBackClick = { viewModel.navigateTo(Screen.Home) },
            onNewChatClick = {
                viewModel.startNewChatSession()
                pendingAttachments.clear()
            },
            onHistoryClick = { showSessionsDialog = true }
        )

        // Messages Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !isChatLoading) {
                // ChatGPT style Welcome & Starter Prompts
                ChatEmptyWelcomeView(
                    onSelectPrompt = { promptText ->
                        viewModel.setChatInput(promptText)
                    },
                    onTriggerUpload = { showAttachmentSheet = true }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            message = msg,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Study AI", text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onSpeak = { text ->
                                if (isTtsSpeaking) {
                                    ttsEngine?.stop()
                                    isTtsSpeaking = false
                                } else {
                                    ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "msg_${msg.id}")
                                    isTtsSpeaking = true
                                }
                            },
                            onDeepDiveSolve = {
                                viewModel.setQuestionInput(msg.text)
                                viewModel.navigateTo(Screen.SolveQuestion)
                            }
                        )
                    }

                    if (isChatLoading) {
                        item {
                            ChatThinkingBubble()
                        }
                    }
                }
            }
        }

        // ChatGPT Style Input Writing Box
        ChatBottomWritingBox(
            inputValue = chatInput,
            onInputChange = { viewModel.setChatInput(it) },
            pendingAttachments = pendingAttachments,
            onRemoveAttachment = { pendingAttachments.remove(it) },
            onOpenUpload = { showAttachmentSheet = true },
            onOpenVoice = { showVoiceDictationSheet = true },
            isLoading = isChatLoading,
            onSend = {
                val toSend = chatInput
                val filesToSend = pendingAttachments.toList()
                viewModel.sendChatPrompt(toSend, filesToSend)
                pendingAttachments.clear()
            }
        )
    }
}

@Composable
fun ChatHeaderBar(
    sessionTitle: String,
    hasActiveSession: Boolean,
    onBackClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Study AI Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasActiveSession) sessionTitle else "ChatGPT Mode • Photos, Videos & Files",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // New Chat button
            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.testTag("chat_new_session_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.primary)
            }

            // Sessions / History button
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier.testTag("chat_history_menu_btn")
            ) {
                Icon(Icons.Default.History, contentDescription = "Chat History")
            }
        }
    }
}

@Composable
fun ChatEmptyWelcomeView(
    onSelectPrompt: (String) -> Unit,
    onTriggerUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "How can Study AI help you?",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ask any homework problem, click/upload photos, record videos, or attach study notes & PDFs.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Prompt Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PromptSuggestionCard(
                icon = "📸",
                title = "Solve Math & Science Problems",
                subtitle = "Click or upload homework photos for step-by-step solutions",
                onClick = { onTriggerUpload() }
            )

            PromptSuggestionCard(
                icon = "🎥",
                title = "Explain Lecture Videos",
                subtitle = "Record or upload video clips to break down complex topics",
                onClick = { onTriggerUpload() }
            )

            PromptSuggestionCard(
                icon = "📁",
                title = "Summarize PDF Notes & Docs",
                subtitle = "Upload study guides or documents for instant summaries",
                onClick = { onTriggerUpload() }
            )

            PromptSuggestionCard(
                icon = "💡",
                title = "Brainstorm Study Strategies",
                subtitle = "Create flashcards or exam prep timelines",
                onClick = { onSelectPrompt("Help me create an efficient exam study plan for my upcoming tests.") }
            )
        }
    }
}

@Composable
fun PromptSuggestionCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onCopy: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onDeepDiveSolve: () -> Unit
) {
    val isUser = message.sender == "user"
    val attachedFiles = remember(message.attachmentsJson) {
        deserializeAttachedFiles(message.attachmentsJson)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Study AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.90f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("chat_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Render user's attached media inside bubble
                    if (isUser && attachedFiles.isNotEmpty()) {
                        ChatAttachmentsGrid(
                            attachments = attachedFiles,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Assistant Toolbar (Copy, Read Aloud TTS, Solve deep-dive)
            if (!isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onCopy(message.text) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSpeak(message.text) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeepDiveSolve,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Solve deep dive",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Study AI is analyzing...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * ChatGPT-Style Writing Box at the bottom:
 * - On the side: Upload button ("+") with photos, videos, files & camera
 * - Attached files preview strip above input
 * - Center: Writing box
 * - Right side: Voice dictation mic button + Send button
 */
@Composable
fun ChatBottomWritingBox(
    inputValue: String,
    onInputChange: (String) -> Unit,
    pendingAttachments: List<AttachedFile>,
    onRemoveAttachment: (AttachedFile) -> Unit,
    onOpenUpload: () -> Unit,
    onOpenVoice: () -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bottom_input_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Pending attachments preview strip
            AttachedFilesPreviewRow(
                attachedFiles = pendingAttachments,
                onRemove = onRemoveAttachment,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // ChatGPT Side Upload Button ("+")
                IconButton(
                    onClick = onOpenUpload,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_upload_side_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload Photos, Videos or Files",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text writing box
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = if (pendingAttachments.isNotEmpty()) "Add instructions or question..." else "Message Study AI...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputValue.isNotBlank() || pendingAttachments.isNotEmpty()) {
                            onSend()
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice Dictation Mic Button ("that I can say so add it")
                IconButton(
                    onClick = onOpenVoice,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_voice_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Dictation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                val canSend = (inputValue.isNotBlank() || pendingAttachments.isNotEmpty()) && !isLoading
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("chat_send_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSessionsListDialog(
    sessions: List<ChatSessionEntity>,
    currentSessionId: Long?,
    onSelectSession: (ChatSessionEntity) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat History", fontWeight = FontWeight.Bold) },
        text = {
            if (sessions.isEmpty()) {
                Text("No previous conversations saved yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions, key = { it.id }) { s ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (s.id == currentSessionId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSession(s) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${s.messageCount} messages • ${s.subject}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSession(s.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete session",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
