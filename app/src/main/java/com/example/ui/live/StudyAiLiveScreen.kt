package com.example.ui.live

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.remote.GeminiStudyService
import com.example.ui.Screen
import com.example.ui.StudyViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class LiveVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class LiveDialogueTurn(
    val id: String = java.util.UUID.randomUUID().toString(),
    val speaker: String, // "You" or "Study AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun StudyAiLiveScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var liveState by remember { mutableStateOf(LiveVoiceState.IDLE) }
    var soundAmplitude by remember { mutableFloatStateOf(0f) }
    var userPartialTranscript by remember { mutableStateOf("") }
    var currentAiReplyText by remember { mutableStateOf("") }
    var textFallbackInput by remember { mutableStateOf("") }
    var isTtsMuted by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf("General Study Coach") }
    var selectedPersona by remember { mutableStateOf("Encouraging Tutor") }

    val dialogueTurns = remember {
        mutableStateListOf(
            LiveDialogueTurn(
                speaker = "Study AI",
                text = "Hi! I am Study AI Live. You can speak to me naturally about any homework problem, concept, or test topic. Tap the mic and start talking!"
            )
        )
    }

    val listState = rememberLazyListState()

    // Setup TextToSpeech
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val speechEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speechEngine?.language = Locale.US
                speechEngine?.setPitch(1.05f)
                speechEngine?.setSpeechRate(1.0f)
                isTtsReady = true
            }
        }
        speechEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                liveState = LiveVoiceState.SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                liveState = LiveVoiceState.IDLE
            }

            override fun onError(utteranceId: String?) {
                liveState = LiveVoiceState.IDLE
            }
        })
        tts = speechEngine

        onDispose {
            speechEngine.stop()
            speechEngine.shutdown()
        }
    }

    fun speakAloud(text: String) {
        if (!isTtsMuted && isTtsReady && tts != null) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "StudyAiLiveUtterance")
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "StudyAiLiveUtterance")
            liveState = LiveVoiceState.SPEAKING
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        if (liveState == LiveVoiceState.SPEAKING) {
            liveState = LiveVoiceState.IDLE
        }
    }

    // SpeechRecognizer
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }

    val processUserSpeechTurn: (String) -> Unit = { speechText ->
        val trimmed = speechText.trim()
        if (trimmed.isNotEmpty()) {
            stopSpeaking()
            dialogueTurns.add(LiveDialogueTurn(speaker = "You", text = trimmed))
            userPartialTranscript = ""
            liveState = LiveVoiceState.THINKING

            coroutineScope.launch {
                listState.animateScrollToItem(dialogueTurns.size - 1)
                val history = dialogueTurns.takeLast(6).map {
                    (if (it.speaker == "You") "user" else "model") to it.text
                }
                val result = viewModel.geminiService.speakLiveTurn(
                    userVoiceInput = trimmed,
                    subjectContext = selectedSubject,
                    persona = selectedPersona,
                    conversationHistory = history
                )
                val aiReply = result.getOrDefault("I'm right here with you. What part should we break down first?")
                currentAiReplyText = aiReply
                dialogueTurns.add(LiveDialogueTurn(speaker = "Study AI", text = aiReply))
                liveState = LiveVoiceState.IDLE
                speakAloud(aiReply)
                listState.animateScrollToItem(dialogueTurns.size - 1)
            }
        } else {
            liveState = LiveVoiceState.IDLE
        }
    }

    DisposableEffect(context) {
        val recognizer = if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        liveState = LiveVoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        liveState = LiveVoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        soundAmplitude = ((rmsdB + 2f) / 10f).coerceIn(0.1f, 1.0f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        liveState = LiveVoiceState.THINKING
                    }

                    override fun onError(error: Int) {
                        liveState = LiveVoiceState.IDLE
                        soundAmplitude = 0f
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        processUserSpeechTurn(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        userPartialTranscript = matches?.firstOrNull() ?: ""
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else null
        speechRecognizer = recognizer

        onDispose {
            recognizer?.destroy()
        }
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
                liveState = LiveVoiceState.LISTENING
            } catch (e: Exception) {
                liveState = LiveVoiceState.IDLE
            }
        }
    }

    fun startListening() {
        stopSpeaking()
        if (hasAudioPermission) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
                liveState = LiveVoiceState.LISTENING
            } catch (e: Exception) {
                liveState = LiveVoiceState.IDLE
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        liveState = LiveVoiceState.IDLE
        soundAmplitude = 0f
    }

    fun toggleMic() {
        if (liveState == LiveVoiceState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    // Dynamic wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val orbRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRotate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_live_back")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study AI Live",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "LIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = when (liveState) {
                                    LiveVoiceState.LISTENING -> "Listening to you speak..."
                                    LiveVoiceState.THINKING -> "Thinking through solution..."
                                    LiveVoiceState.SPEAKING -> "Study AI is speaking (tap to interrupt)"
                                    LiveVoiceState.IDLE -> "Real-time Voice Conversation"
                                },
                                fontSize = 12.sp,
                                color = if (liveState == LiveVoiceState.LISTENING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Speaker mute toggle
                        IconButton(
                            onClick = {
                                isTtsMuted = !isTtsMuted
                                if (isTtsMuted) stopSpeaking()
                            },
                            modifier = Modifier.testTag("btn_live_mute_tts")
                        ) {
                            Icon(
                                imageVector = if (isTtsMuted) Icons.Default.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isTtsMuted) "Unmute Speech" else "Mute Speech",
                                tint = if (isTtsMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Clear conversation
                        IconButton(
                            onClick = {
                                stopSpeaking()
                                dialogueTurns.clear()
                                dialogueTurns.add(
                                    LiveDialogueTurn(
                                        speaker = "Study AI",
                                        text = "Session refreshed. What topic should we explore next?"
                                    )
                                )
                            },
                            modifier = Modifier.testTag("btn_live_clear")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Session")
                        }
                    }
                }

                // Subject focus chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subjects = listOf(
                        "General Study Coach",
                        "Math Tutor",
                        "Physics & Chem",
                        "Coding & CS",
                        "Biology & Health",
                        "History & Essay"
                    )
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Live Voice Visualizer Orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val dynamicScale = when (liveState) {
                LiveVoiceState.LISTENING -> (1.0f + soundAmplitude * 0.4f).coerceAtMost(1.5f)
                LiveVoiceState.SPEAKING -> pulseScale
                LiveVoiceState.THINKING -> 1.05f
                LiveVoiceState.IDLE -> 1.0f
            }

            val ringColor = when (liveState) {
                LiveVoiceState.LISTENING -> MaterialTheme.colorScheme.primary
                LiveVoiceState.SPEAKING -> MaterialTheme.colorScheme.tertiary
                LiveVoiceState.THINKING -> MaterialTheme.colorScheme.secondary
                LiveVoiceState.IDLE -> MaterialTheme.colorScheme.outlineVariant
            }

            // Outer pulse glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(dynamicScale)
                    .clip(CircleShape)
                    .background(ringColor.copy(alpha = 0.15f))
            )

            // Inner glowing ripple
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ringColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Core Interactive Mic Orb
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { toggleMic() }
                    .testTag("btn_live_orb"),
                shape = CircleShape,
                color = when (liveState) {
                    LiveVoiceState.LISTENING -> MaterialTheme.colorScheme.primary
                    LiveVoiceState.SPEAKING -> MaterialTheme.colorScheme.tertiary
                    LiveVoiceState.THINKING -> MaterialTheme.colorScheme.secondary
                    LiveVoiceState.IDLE -> MaterialTheme.colorScheme.primaryContainer
                },
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when (liveState) {
                        LiveVoiceState.THINKING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 3.dp
                            )
                        }
                        LiveVoiceState.SPEAKING -> {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Speaking",
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        LiveVoiceState.LISTENING -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        LiveVoiceState.IDLE -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start Speaking",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live status action banner (Interrupt or Tap to Speak)
        AnimatedVisibility(
            visible = liveState == LiveVoiceState.SPEAKING,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { stopSpeaking() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_live_interrupt")
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tap to Interrupt AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Ongoing partial speech preview
        AnimatedVisibility(
            visible = userPartialTranscript.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userPartialTranscript,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // Quick Spoken Prompts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Explain step-by-step",
                "Quiz me on this",
                "Give a real example",
                "Why does this work?",
                "Summarize in 2 sentences"
            )
            items(suggestions) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { processUserSpeechTurn(prompt) }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Live Dialogue Transcript Scroll
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(dialogueTurns, key = { it.id }) { turn ->
                val isUser = turn.speaker == "You"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = turn.speaker,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                )
                                if (!isUser) {
                                    IconButton(
                                        onClick = { speakAloud(turn.text) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak Again",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = turn.text,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Bottom Bar with Mic / Text Fallback Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = { toggleMic() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (liveState == LiveVoiceState.LISTENING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .testTag("btn_live_bottom_mic")
                ) {
                    Icon(
                        imageVector = if (liveState == LiveVoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Fallback (in case user wants to type or add details)
                OutlinedTextField(
                    value = textFallbackInput,
                    onValueChange = { textFallbackInput = it },
                    placeholder = { Text("Speak or type to Study AI...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_live_text_fallback"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 2,
                    trailingIcon = {
                        if (textFallbackInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val input = textFallbackInput
                                    textFallbackInput = ""
                                    processUserSpeechTurn(input)
                                },
                                modifier = Modifier.testTag("btn_live_send_text")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Spoken Prompt",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
