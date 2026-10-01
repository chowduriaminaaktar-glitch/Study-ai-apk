package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.util.AcademicSpeechFormatter
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDictationBottomSheet(
    subjectName: String,
    existingPrompt: String,
    onDismiss: () -> Unit,
    onApplyText: (spokenText: String, append: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var speechStatus by remember { mutableStateOf("Ready to listen. Tap the microphone to dictate.") }
    var autoFormatSymbols by remember { mutableStateOf(true) }
    var soundLevel by remember { mutableFloatStateOf(0f) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var isRecognizerSupported by remember {
        mutableStateOf(SpeechRecognizer.isRecognitionAvailable(context))
    }

    // Animation scale for the listening pulse ring
    val pulseAnim = remember { Animatable(1f) }

    LaunchedEffect(isListening) {
        if (isListening) {
            pulseAnim.animateTo(
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(1f)
        }
    }

    // Launcher for system Google speech dialog fallback
    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val resultText = spokenMatches?.firstOrNull() ?: ""
            if (resultText.isNotEmpty()) {
                val formatted = if (autoFormatSymbols) {
                    AcademicSpeechFormatter.formatAcademicSpeech(resultText)
                } else {
                    resultText
                }
                recognizedText = formatted
                speechStatus = "Captured successfully! Review or insert into your prompt."
            }
        }
        isListening = false
    }

    fun launchSystemSpeechRecognizer() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Dictate your $subjectName question..."
                )
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            systemSpeechLauncher.launch(intent)
        } catch (_: Exception) {
            speechStatus = "Speech service unavailable. Please check system speech recognition settings."
            isListening = false
        }
    }

    fun startListeningWithRecognizer() {
        if (!isRecognizerSupported) {
            launchSystemSpeechRecognizer()
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    speechStatus = "Listening... Speak your academic question clearly."
                }

                override fun onBeginningOfSpeech() {
                    speechStatus = "Hearing your voice..."
                }

                override fun onRmsChanged(rmsdB: Float) {
                    soundLevel = (rmsdB / 10f).coerceIn(0f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    speechStatus = "Transcribing speech..."
                }

                override fun onError(error: Int) {
                    isListening = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak closer to microphone."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected before timeout."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check permissions."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
                        else -> "Speech recognition error ($error). You can retry or use system dialog."
                    }
                    speechStatus = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val rawSpoken = matches?.firstOrNull() ?: ""
                    if (rawSpoken.isNotEmpty()) {
                        val formatted = if (autoFormatSymbols) {
                            AcademicSpeechFormatter.formatAcademicSpeech(rawSpoken)
                        } else {
                            rawSpoken
                        }
                        recognizedText = formatted
                        speechStatus = "Question transcribed! Ready to insert or polish."
                    } else {
                        speechStatus = "No clear words detected. Please try again."
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = partialMatches?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        val formatted = if (autoFormatSymbols) {
                            AcademicSpeechFormatter.formatAcademicSpeech(partial)
                        } else {
                            partial
                        }
                        recognizedText = formatted
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
        } catch (_: Exception) {
            launchSystemSpeechRecognizer()
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            startListeningWithRecognizer()
        } else {
            speechStatus = "Microphone access is needed to dictate academic questions."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            stopListening()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("voice_dictation_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(IndigoPrimary, CyanSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Voice Dictation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dictate complex $subjectName questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        stopListening()
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.testTag("close_voice_dictation_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Microphone Interaction Centerpiece
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse ripple background ring when listening
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseAnim.value)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.25f))
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isListening) {
                                stopListening()
                            } else {
                                if (hasAudioPermission) {
                                    startListeningWithRecognizer()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("mic_toggle_button"),
                    shape = CircleShape,
                    color = if (isListening) MaterialTheme.colorScheme.error else IndigoPrimary,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop dictating" else "Start dictating",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Status message
            Text(
                text = speechStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isListening) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Audio visualizer bars simulation
            if (isListening) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) { index ->
                        val barHeight = (12 + (soundLevel * 28 * ((index % 3) + 1) / 3)).coerceIn(8f, 40f)
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyanSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dictated Text Preview
            OutlinedTextField(
                value = recognizedText,
                onValueChange = { recognizedText = it },
                label = { Text("Transcribed Question") },
                placeholder = { Text("Your spoken academic question will appear here in real-time...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("dictated_text_field"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (recognizedText.isNotEmpty()) {
                        IconButton(
                            onClick = { recognizedText = "" },
                            modifier = Modifier.testTag("clear_dictated_text")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear text")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Math & Science Symbol formatting toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Auto-format Math & Science symbols",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Converts 'x squared', 'integral', 'yields' to ², ∫, →",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = autoFormatSymbols,
                    onCheckedChange = { checked ->
                        autoFormatSymbols = checked
                        if (checked && recognizedText.isNotEmpty()) {
                            recognizedText = AcademicSpeechFormatter.formatAcademicSpeech(recognizedText)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("symbol_formatting_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Academic Symbol insertion strip
            Text(
                text = "Insert Symbols & Operators:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val symbols = listOf("²", "³", "√", "∫", "±", "π", "θ", "Δ", "∞", "≠", "≤", "≥", "→", "÷", "×")
                symbols.forEach { sym ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                recognizedText = "$recognizedText$sym"
                            }
                            .testTag("symbol_chip_$sym"),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = sym,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (hasAudioPermission) {
                            launchSystemSpeechRecognizer()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("system_speech_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("System Dialog")
                }

                Button(
                    onClick = {
                        stopListening()
                        if (recognizedText.isNotBlank()) {
                            val shouldAppend = existingPrompt.isNotBlank()
                            onApplyText(recognizedText, shouldAppend)
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    },
                    enabled = recognizedText.isNotBlank(),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("insert_transcription_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (existingPrompt.isBlank()) "Insert Question" else "Append to Prompt")
                }
            }

            // Replace option if prompt already has text
            if (existingPrompt.isNotBlank() && recognizedText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        stopListening()
                        onApplyText(recognizedText, false) // replace mode
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replace_transcription_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Replace Entire Prompt (${existingPrompt.length} chars)")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speech Guidance & Academic Examples
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = IndigoDark.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Speech dictation examples for $subjectName:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val examplePhrases = when (subjectName.lowercase()) {
                        "mathematics" -> listOf(
                            "\"Solve the quadratic equation 2x squared minus 8x plus 6 equals 0\"",
                            "\"Find the definite integral of cosine x from zero to pi\""
                        )
                        "physics" -> listOf(
                            "\"Calculate velocity if initial speed is 10 meters per second with constant acceleration\"",
                            "\"Explain quantum entanglement and Einstein-Podolsky-Rosen paradox\""
                        )
                        "chemistry" -> listOf(
                            "\"Balance iron plus oxygen gives iron 3 oxide with state symbols\"",
                            "\"What is the pH of a 0.05 molar solution of hydrochloric acid?\""
                        )
                        "computer science" -> listOf(
                            "\"Explain Dijkstra algorithm with time and space complexity\"",
                            "\"Write a recursive function for binary search tree depth\""
                        )
                        else -> listOf(
                            "\"Summarize the primary causes of the Industrial Revolution\"",
                            "\"Explain supply and demand elasticity with real-world examples\""
                        )
                    }
                    examplePhrases.forEach { phrase ->
                        Text(
                            text = "• $phrase",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val formatted = if (autoFormatSymbols) {
                                        AcademicSpeechFormatter.formatAcademicSpeech(phrase.replace("\"", ""))
                                    } else {
                                        phrase.replace("\"", "")
                                    }
                                    recognizedText = formatted
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
