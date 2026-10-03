package com.example.ui.quiz

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.QuizRecordEntity
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizQuestion
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * UI State for the Gemini AI Quiz Generator
 */
sealed interface QuizUiState {
    data object Setup : QuizUiState

    data class Generating(
        val topic: String,
        val statusMessage: String = "Consulting Gemini AI..."
    ) : QuizUiState

    data class Active(
        val quiz: PracticeQuiz,
        val currentQuestionIndex: Int = 0,
        val userAnswers: Map<Int, Int> = emptyMap(), // questionId -> selectedOptionIndex
        val isAnswerChecked: Boolean = false,
        val elapsedSeconds: Int = 0
    ) : QuizUiState

    data class Completed(
        val quiz: PracticeQuiz,
        val userAnswers: Map<Int, Int>,
        val score: Int,
        val totalQuestions: Int,
        val percentage: Int,
        val elapsedSeconds: Int
    ) : QuizUiState

    data class Error(
        val message: String,
        val failedTopic: String,
        val canRetry: Boolean = true
    ) : QuizUiState
}

/**
 * ViewModel managing quiz generation using Google Gemini AI,
 * user study topic inputs, difficulty, question progression, and scoring.
 */
class QuizGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val geminiService = GeminiStudyService()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val geminiModel = "gemini-3.5-flash"

    // Timer coroutine job
    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudyRepository(db.studyDao(), geminiService)
    }

    // Input state
    private val _topicInput = MutableStateFlow("Newton's Laws of Motion")
    val topicInput: StateFlow<String> = _topicInput.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Physics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow(QuizDifficulty.MEDIUM)
    val selectedDifficulty: StateFlow<QuizDifficulty> = _selectedDifficulty.asStateFlow()

    private val _questionCount = MutableStateFlow(5)
    val questionCount: StateFlow<Int> = _questionCount.asStateFlow()

    // Main UI State
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Setup)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Popular suggested study topics
    val suggestedTopics = listOf(
        "Calculus Derivatives" to "Mathematics",
        "Newton's Laws" to "Physics",
        "Cellular Respiration" to "Biology",
        "Periodic Trends" to "Chemistry",
        "Binary Search Trees" to "Computer Science",
        "World War II" to "History",
        "Macroeconomics Inflation" to "Economics",
        "Shakespearean Tragedies" to "Literature"
    )

    fun onTopicChange(newTopic: String) {
        _topicInput.value = newTopic
    }

    fun onSubjectSelect(subject: String) {
        _selectedSubject.value = subject
    }

    fun onDifficultySelect(difficulty: QuizDifficulty) {
        _selectedDifficulty.value = difficulty
    }

    fun onQuestionCountSelect(count: Int) {
        _questionCount.value = count
    }

    fun selectSuggestedTopic(topic: String, subject: String) {
        _topicInput.value = topic
        _selectedSubject.value = subject
    }

    /**
     * Sends user topic to Gemini AI to generate structured multiple-choice quiz questions.
     */
    fun generateQuiz() {
        val topic = _topicInput.value.trim()
        if (topic.isBlank()) {
            _uiState.value = QuizUiState.Error(
                message = "Please enter a study topic to generate a quiz.",
                failedTopic = topic,
                canRetry = false
            )
            return
        }

        val subject = _selectedSubject.value
        val difficulty = _selectedDifficulty.value.label
        val count = _questionCount.value

        _uiState.value = QuizUiState.Generating(
            topic = topic,
            statusMessage = "Crafting $count exam-style questions with Gemini AI..."
        )

        viewModelScope.launch {
            try {
                val quiz = callGeminiForQuiz(
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    count = count
                )

                // Start timer
                startTimer()

                _uiState.value = QuizUiState.Active(
                    quiz = quiz,
                    currentQuestionIndex = 0,
                    userAnswers = emptyMap(),
                    isAnswerChecked = false,
                    elapsedSeconds = 0
                )

                // Cache quiz to local Room database
                repository.savePracticeQuiz(quiz)
            } catch (e: Exception) {
                Log.e("QuizGeneratorVM", "Quiz generation failed", e)
                // Use intelligent academic fallback if offline or key issue
                val fallbackQuiz = geminiService.createFallbackQuiz(
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    count = count
                )

                startTimer()
                _uiState.value = QuizUiState.Active(
                    quiz = fallbackQuiz,
                    currentQuestionIndex = 0,
                    userAnswers = emptyMap(),
                    isAnswerChecked = false,
                    elapsedSeconds = 0
                )
            }
        }
    }

    private suspend fun callGeminiForQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        count: Int
    ): PracticeQuiz = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext geminiService.createFallbackQuiz(subject, topic, difficulty, count)
        }

        val prompt = """
You are a university professor and academic test creator in $subject.
Generate an engaging, pedagogically rigorous multiple-choice practice quiz for a student studying:
Topic: "$topic"
Academic Subject: $subject
Difficulty Level: $difficulty
Number of Questions: exactly $count

Rules:
1. Every question must be clear, well-phrased, and directly test understanding of "$topic".
2. Provide exactly 4 distinct options per question.
3. Only ONE option must be correct.
4. "correctIndex" MUST be the 0-based integer index of the correct answer (0 for 1st option, 1 for 2nd, etc.).
5. Provide a rich "explanation" explaining why that option is correct and clarifying common misconceptions.
6. Provide "conceptTested" naming the specific principle.

You MUST respond strictly in VALID, RAW JSON matching this exact structure:
{
  "title": "$topic Quiz",
  "questions": [
    {
      "id": 1,
      "questionText": "Question statement here?",
      "options": ["Option A text", "Option B text", "Option C text", "Option D text"],
      "correctIndex": 0,
      "explanation": "Clear pedagogical explanation.",
      "conceptTested": "Core Concept"
    }
  ]
}
Do NOT include markdown backticks or extra commentary. Output pure raw JSON.
""".trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            }
            put("generationConfig", genConfig)
        }

        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            throw IllegalStateException("Gemini API error ${response.code}: $errorBody")
        }

        val responseBody = response.body?.string() ?: throw IllegalStateException("Empty Gemini response")
        val jsonRoot = JSONObject(responseBody)
        val candidates = jsonRoot.getJSONArray("candidates")
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val rawJsonText = parts.getJSONObject(0).getString("text")

        parseQuizJson(rawJsonText, subject, topic, difficulty)
    }

    private fun parseQuizJson(
        rawJson: String,
        subject: String,
        topic: String,
        difficulty: String
    ): PracticeQuiz {
        val cleaned = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val obj = JSONObject(cleaned)
        val title = obj.optString("title", "$topic Practice Quiz")
        val qArray = obj.getJSONArray("questions")
        val questionsList = mutableListOf<QuizQuestion>()

        for (i in 0 until qArray.length()) {
            val qObj = qArray.getJSONObject(i)
            val optsArray = qObj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optsArray.length()) {
                options.add(optsArray.getString(j))
            }

            questionsList.add(
                QuizQuestion(
                    id = qObj.optInt("id", i + 1),
                    questionText = qObj.optString("questionText", "Question ${i + 1}"),
                    options = options,
                    correctIndex = qObj.optInt("correctIndex", 0).coerceIn(0, (options.size - 1).coerceAtLeast(0)),
                    explanation = qObj.optString("explanation", "Correct according to key principles."),
                    conceptTested = qObj.optString("conceptTested", topic)
                )
            )
        }

        return PracticeQuiz(
            title = title,
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            questions = questionsList
        )
    }

    // --- Interactive Quiz Actions ---

    fun onSelectOption(questionId: Int, optionIndex: Int) {
        val current = _uiState.value
        if (current is QuizUiState.Active) {
            val updatedAnswers = current.userAnswers.toMutableMap()
            updatedAnswers[questionId] = optionIndex
            _uiState.value = current.copy(
                userAnswers = updatedAnswers,
                isAnswerChecked = true
            )
        }
    }

    fun onNextQuestion() {
        val current = _uiState.value
        if (current is QuizUiState.Active) {
            val nextIndex = current.currentQuestionIndex + 1
            if (nextIndex < current.quiz.questions.size) {
                _uiState.value = current.copy(
                    currentQuestionIndex = nextIndex,
                    isAnswerChecked = current.userAnswers.containsKey(current.quiz.questions[nextIndex].id)
                )
            } else {
                finishQuiz(current)
            }
        }
    }

    fun onPreviousQuestion() {
        val current = _uiState.value
        if (current is QuizUiState.Active && current.currentQuestionIndex > 0) {
            val prevIndex = current.currentQuestionIndex - 1
            _uiState.value = current.copy(
                currentQuestionIndex = prevIndex,
                isAnswerChecked = current.userAnswers.containsKey(current.quiz.questions[prevIndex].id)
            )
        }
    }

    private fun finishQuiz(state: QuizUiState.Active) {
        stopTimer()

        val total = state.quiz.questions.size
        var correct = 0
        state.quiz.questions.forEach { q ->
            if (state.userAnswers[q.id] == q.correctIndex) {
                correct++
            }
        }

        val pct = if (total > 0) ((correct.toDouble() / total) * 100).toInt() else 0

        _uiState.value = QuizUiState.Completed(
            quiz = state.quiz,
            userAnswers = state.userAnswers,
            score = correct,
            totalQuestions = total,
            percentage = pct,
            elapsedSeconds = state.elapsedSeconds
        )

        // Save record to local database
        viewModelScope.launch {
            repository.saveQuizRecord(
                QuizRecordEntity(
                    title = state.quiz.title,
                    subject = state.quiz.subject,
                    topic = state.quiz.topic,
                    difficulty = state.quiz.difficulty,
                    totalQuestions = total,
                    correctCount = correct,
                    percentage = pct,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun onRetakeQuiz() {
        val current = _uiState.value
        val quiz = when (current) {
            is QuizUiState.Completed -> current.quiz
            is QuizUiState.Active -> current.quiz
            else -> return
        }

        startTimer()
        _uiState.value = QuizUiState.Active(
            quiz = quiz,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            isAnswerChecked = false,
            elapsedSeconds = 0
        )
    }

    fun resetToSetup() {
        stopTimer()
        _uiState.value = QuizUiState.Setup
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1000)
                seconds++
                val cur = _uiState.value
                if (cur is QuizUiState.Active) {
                    _uiState.value = cur.copy(elapsedSeconds = seconds)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
