package com.example.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class ChatPersona(val displayName: String, val iconLabel: String, val systemInstruction: String) {
    GENERAL(
        "Study AI (ChatGPT)",
        "🤖",
        "You are an intelligent, articulate, versatile, and encouraging AI academic companion like ChatGPT. Provide direct, beautifully structured responses with bold highlights, bullet points, and code blocks where helpful."
    ),
    TUTOR(
        "Master Professor",
        "🎓",
        "You are a compassionate, world-class university tutor. Break down academic questions step-by-step, explain underlying principles, and provide memorable intuition."
    ),
    CODER(
        "Code Architect",
        "💻",
        "You are an expert senior software engineer and computer scientist. Provide clean, production-grade code, optimal time/space complexity analysis, and modern best practices."
    ),
    SOCRATIC(
        "Socratic Mentor",
        "🏛️",
        "You are a classical Socratic teacher. Ask probing, thoughtful questions to guide the student toward discovering the answer through their own reasoning."
    )
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val geminiService = GeminiStudyService()

    private var currentSessionId: Long? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudyRepository(db.studyDao(), geminiService)
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "Hello! I am your AI academic tutor and assistant. What would you like to explore or solve today?\n\n• Ask any complex math, physics, or coding question\n• Brainstorm essay arguments and outlines\n• Discuss books from our Digital Library\n• Request analogies, quizzes, or step-by-step derivations"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedPersona = MutableStateFlow(ChatPersona.GENERAL)
    val selectedPersona: StateFlow<ChatPersona> = _selectedPersona.asStateFlow()

    val promptSuggestions = listOf(
        "🧠 Explain Quantum Entanglement with an analogy",
        "💻 Write a Python Binary Search Tree implementation",
        "📝 Outline an argumentative essay on AI ethics",
        "🧪 Explain the mechanism of ATP synthesis",
        "🏛️ What is Plato's Allegory of the Cave?",
        "📐 Solve ∫ x * e^(2x) dx using Integration by Parts"
    )

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun onSelectPersona(persona: ChatPersona) {
        _selectedPersona.value = persona
    }

    fun sendMessage(userText: String? = null) {
        val query = (userText ?: _inputText.value).trim()
        if (query.isBlank() || _isGenerating.value) return

        _inputText.value = ""

        val userMessage = ChatMessage(sender = "USER", text = query)
        val currentList = _messages.value.toMutableList()
        currentList.add(userMessage)
        _messages.value = currentList
        _isGenerating.value = true

        viewModelScope.launch {
            // Persist to Room session
            if (currentSessionId == null) {
                val title = if (query.length > 30) query.take(28) + "..." else query
                currentSessionId = repository.createChatSession(
                    title = title,
                    subject = "Chat",
                    firstQuestion = query
                )
            }
            currentSessionId?.let { sId ->
                repository.addMessageToSession(sId, "USER", query)
            }

            // Build multi-turn context (last 10 turns)
            val history = currentList.takeLast(10).map { msg ->
                (if (msg.sender == "USER") "user" else "model") to msg.text
            }

            val result = geminiService.sendChatMessage(
                conversation = history,
                systemInstruction = _selectedPersona.value.systemInstruction
            )

            val aiText = result.getOrElse {
                "I encountered an issue generating a response: ${it.message}. Please try again."
            }

            val aiMessage = ChatMessage(sender = "AI", text = aiText)
            val updatedList = _messages.value.toMutableList()
            updatedList.add(aiMessage)
            _messages.value = updatedList
            _isGenerating.value = false

            currentSessionId?.let { sId ->
                repository.addMessageToSession(sId, "AI", aiText)
            }
        }
    }

    fun startNewChat() {
        currentSessionId = null
        _messages.value = listOf(
            ChatMessage(
                sender = "AI",
                text = "New chat session started with ${_selectedPersona.value.displayName}. How can I assist you now?"
            )
        )
        _inputText.value = ""
    }

    fun askAboutTopic(topic: String) {
        _inputText.value = "Explain $topic and provide its key principles, historical significance, and a study summary."
        sendMessage()
    }
}
