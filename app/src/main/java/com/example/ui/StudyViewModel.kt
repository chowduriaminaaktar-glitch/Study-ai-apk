package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.SavedSolutionEntity
import com.example.data.model.ExplanationStyle
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuestionSolution
import com.example.data.model.QuizDifficulty
import com.example.data.model.StudySubject
import com.example.data.model.SubjectCatalog
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.UserAccount
import com.example.data.repository.AuthRepository
import com.example.ui.theme.ThemeManager

sealed class Screen(val title: String) {
    data object Home : Screen("StudyAI")
    data object Chat : Screen("AI Chat (ChatGPT)")
    data object Library : Screen("Digital Library")
    data object SolveQuestion : Screen("Ask StudyAI")
    data object PracticeQuizzes : Screen("Quizzes")
    data object SavedAndHistory : Screen("Saved & History")
    data object SubjectExplore : Screen("Explore")
    data object Auth : Screen("Account")
}

sealed class SolverUiState {
    data object Idle : SolverUiState()
    data object Loading : SolverUiState()
    data class Success(val solution: QuestionSolution, val isSaved: Boolean = false) : SolverUiState()
    data class Error(val message: String) : SolverUiState()
}

sealed class QuizFlowState {
    data object Setup : QuizFlowState()
    data object Generating : QuizFlowState()
    data class Active(
        val quiz: PracticeQuiz,
        val currentQuestionIndex: Int = 0,
        val userAnswers: Map<Int, Int> = emptyMap(), // questionId -> optionIndex
        val isAnswerChecked: Boolean = false
    ) : QuizFlowState()
    data class Completed(
        val quiz: PracticeQuiz,
        val userAnswers: Map<Int, Int>,
        val score: Int,
        val total: Int,
        val percentage: Int
    ) : QuizFlowState()
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val authRepository = AuthRepository(application)
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    init {
        ThemeManager.initialize(application)
        val db = AppDatabase.getInstance(application)
        val geminiService = GeminiStudyService()
        repository = StudyRepository(db.studyDao(), geminiService)
    }

    fun signOut() {
        authRepository.signOut()
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Solver state
    private val _selectedSubject = MutableStateFlow<StudySubject>(SubjectCatalog.subjects.first())
    val selectedSubject: StateFlow<StudySubject> = _selectedSubject.asStateFlow()

    private val _questionInput = MutableStateFlow("")
    val questionInput: StateFlow<String> = _questionInput.asStateFlow()

    private val _explanationStyle = MutableStateFlow(ExplanationStyle.STEP_BY_STEP)
    val explanationStyle: StateFlow<ExplanationStyle> = _explanationStyle.asStateFlow()

    private val _solverUiState = MutableStateFlow<SolverUiState>(SolverUiState.Idle)
    val solverUiState: StateFlow<SolverUiState> = _solverUiState.asStateFlow()

    // Active Chat Session state
    private val _currentChatSessionId = MutableStateFlow<Long?>(null)
    val currentChatSessionId: StateFlow<Long?> = _currentChatSessionId.asStateFlow()

    private val _currentChatSession = MutableStateFlow<ChatSessionEntity?>(null)
    val currentChatSession: StateFlow<ChatSessionEntity?> = _currentChatSession.asStateFlow()

    private val _currentChatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentChatMessages: StateFlow<List<ChatMessageEntity>> = _currentChatMessages.asStateFlow()

    private val _followUpInput = MutableStateFlow("")
    val followUpInput: StateFlow<String> = _followUpInput.asStateFlow()

    private val _isSendingFollowUp = MutableStateFlow(false)
    val isSendingFollowUp: StateFlow<Boolean> = _isSendingFollowUp.asStateFlow()

    private var messagesCollectJob: Job? = null

    // Quiz generator state
    private val _quizSubject = MutableStateFlow<StudySubject>(SubjectCatalog.subjects.first())
    val quizSubject: StateFlow<StudySubject> = _quizSubject.asStateFlow()

    private val _quizTopic = MutableStateFlow("Calculus")
    val quizTopic: StateFlow<String> = _quizTopic.asStateFlow()

    private val _quizDifficulty = MutableStateFlow(QuizDifficulty.MEDIUM)
    val quizDifficulty: StateFlow<QuizDifficulty> = _quizDifficulty.asStateFlow()

    private val _quizQuestionCount = MutableStateFlow(5)
    val quizQuestionCount: StateFlow<Int> = _quizQuestionCount.asStateFlow()

    private val _quizFlowState = MutableStateFlow<QuizFlowState>(QuizFlowState.Setup)
    val quizFlowState: StateFlow<QuizFlowState> = _quizFlowState.asStateFlow()

    // Database flow streams
    val savedSolutions: StateFlow<List<SavedSolutionEntity>> = repository.allSavedSolutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizRecords: StateFlow<List<QuizRecordEntity>> = repository.allQuizRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatSessions: StateFlow<List<ChatSessionEntity>> = repository.allChatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedQuizzes: StateFlow<List<SavedPracticeQuizEntity>> = repository.allSavedQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isApiKeyConfigured: Boolean
        get() = repository.isApiKeyConfigured()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectSubject(subject: StudySubject) {
        _selectedSubject.value = subject
        _quizSubject.value = subject
        if (subject.popularTopics.isNotEmpty()) {
            _quizTopic.value = subject.popularTopics.first()
        }
    }

    fun setQuestionInput(text: String) {
        _questionInput.value = text
    }

    fun setFollowUpInput(text: String) {
        _followUpInput.value = text
    }

    fun appendSpokenText(spokenText: String) {
        if (spokenText.isBlank()) return
        val current = _questionInput.value
        if (current.isBlank()) {
            _questionInput.value = spokenText.trim()
        } else {
            _questionInput.value = "${current.trimEnd()} ${spokenText.trim()}"
        }
    }

    fun replaceSpokenText(spokenText: String) {
        _questionInput.value = spokenText.trim()
    }

    fun setExplanationStyle(style: ExplanationStyle) {
        _explanationStyle.value = style
    }

    // --- Chat Session Actions ---

    fun startNewChatSession() {
        messagesCollectJob?.cancel()
        messagesCollectJob = null
        _currentChatSessionId.value = null
        _currentChatSession.value = null
        _currentChatMessages.value = emptyList()
        _solverUiState.value = SolverUiState.Idle
        _questionInput.value = ""
        _followUpInput.value = ""
    }

    fun loadChatSession(session: ChatSessionEntity) {
        _currentChatSessionId.value = session.id
        _currentChatSession.value = session

        val matchedSub = SubjectCatalog.subjects.find { it.name.equals(session.subject, ignoreCase = true) }
        if (matchedSub != null) {
            _selectedSubject.value = matchedSub
        }

        _questionInput.value = session.title

        observeSessionMessages(session.id)
        _currentScreen.value = Screen.SolveQuestion
    }

    private fun observeSessionMessages(sessionId: Long) {
        messagesCollectJob?.cancel()
        messagesCollectJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { messages ->
                _currentChatMessages.value = messages

                // If the latest AI message has a structured solution, set it in solver state
                val latestSolutionMsg = messages.findLast { it.solutionJson != null }
                if (latestSolutionMsg != null) {
                    val solution = repository.deserializeSolution(latestSolutionMsg.solutionJson!!)
                    if (solution != null) {
                        _solverUiState.value = SolverUiState.Success(solution = solution, isSaved = false)
                    }
                } else if (messages.isNotEmpty()) {
                    val firstUserMsg = messages.firstOrNull { it.sender == "USER" }
                    val firstAiMsg = messages.firstOrNull { it.sender == "AI" }
                    if (firstUserMsg != null && firstAiMsg != null) {
                        _solverUiState.value = SolverUiState.Success(
                            solution = QuestionSolution(
                                subject = _currentChatSession.value?.subject ?: "Academic",
                                question = firstUserMsg.text,
                                directAnswer = firstAiMsg.text,
                                steps = emptyList(),
                                coreConcepts = listOf(_currentChatSession.value?.subject ?: "Academic"),
                                tipsAndCommonMistakes = emptyList(),
                                followUpPractice = ""
                            ),
                            isSaved = false
                        )
                    }
                }
            }
        }
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteChatSession(sessionId)
            if (_currentChatSessionId.value == sessionId) {
                startNewChatSession()
            }
        }
    }

    fun togglePinSession(sessionId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId, !isPinned)
            val curr = _currentChatSession.value
            if (curr?.id == sessionId) {
                _currentChatSession.value = curr.copy(isPinned = !isPinned)
            }
        }
    }

    fun solveQuestion() {
        val query = _questionInput.value.trim()
        if (query.isBlank()) return

        _solverUiState.value = SolverUiState.Loading

        viewModelScope.launch {
            // Ensure we have an active chat session in Room
            var sessionId = _currentChatSessionId.value
            if (sessionId == null) {
                val title = if (query.length > 42) query.take(40) + "..." else query
                sessionId = repository.createChatSession(
                    title = title,
                    subject = _selectedSubject.value.name,
                    firstQuestion = query
                )
                _currentChatSessionId.value = sessionId
                _currentChatSession.value = repository.getChatSessionById(sessionId)
                observeSessionMessages(sessionId)
            }

            // Save user message to Room
            repository.addMessageToSession(
                sessionId = sessionId,
                sender = "USER",
                text = query
            )

            // Solve question via Gemini / Fallback
            val result = repository.solveQuestion(
                subject = _selectedSubject.value.name,
                question = query,
                style = _explanationStyle.value
            )

            result.onSuccess { solution ->
                _solverUiState.value = SolverUiState.Success(solution = solution, isSaved = false)

                // Save AI solution message to Room
                repository.addMessageToSession(
                    sessionId = sessionId,
                    sender = "AI",
                    text = solution.directAnswer,
                    solution = solution
                )
            }.onFailure { err ->
                val errorMsg = err.message ?: "Failed to generate solution"
                _solverUiState.value = SolverUiState.Error(errorMsg)
                repository.addMessageToSession(
                    sessionId = sessionId,
                    sender = "AI",
                    text = "I encountered an error solving this problem: $errorMsg. Please check the equation or try again."
                )
            }
        }
    }

    fun sendFollowUpMessage() {
        val query = _followUpInput.value.trim()
        val sessionId = _currentChatSessionId.value ?: return
        if (query.isBlank()) return

        _followUpInput.value = ""
        _isSendingFollowUp.value = true

        viewModelScope.launch {
            // Save user follow-up message to Room
            repository.addMessageToSession(
                sessionId = sessionId,
                sender = "USER",
                text = query
            )

            val messages = _currentChatMessages.value
            val history = messages.map { Pair(it.sender, it.text) }
            val firstQuestion = messages.firstOrNull { it.sender == "USER" }?.text ?: query

            val result = repository.answerFollowUp(
                subject = _selectedSubject.value.name,
                originalQuestion = firstQuestion,
                conversationHistory = history,
                followUpQuery = query
            )

            result.onSuccess { aiAnswer ->
                repository.addMessageToSession(
                    sessionId = sessionId,
                    sender = "AI",
                    text = aiAnswer
                )
            }.onFailure { err ->
                repository.addMessageToSession(
                    sessionId = sessionId,
                    sender = "AI",
                    text = "Could not generate follow-up answer: ${err.message}. Please try rephrasing."
                )
            }

            _isSendingFollowUp.value = false
        }
    }

    fun pickSampleQuestion(q: String) {
        _questionInput.value = q
        solveQuestion()
    }

    fun saveCurrentSolution(solution: QuestionSolution) {
        viewModelScope.launch {
            repository.saveSolution(solution)
            val current = _solverUiState.value
            if (current is SolverUiState.Success) {
                _solverUiState.value = current.copy(isSaved = true)
            }
        }
    }

    fun toggleFavorite(id: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, !currentFav)
        }
    }

    fun deleteSavedSolution(id: Long) {
        viewModelScope.launch {
            repository.deleteSolution(id)
        }
    }

    fun viewSavedSolution(entity: SavedSolutionEntity) {
        val solution = repository.deserializeSolution(entity.solutionJson)
        if (solution != null) {
            val matchedSub = SubjectCatalog.subjects.find { it.name.equals(entity.subject, ignoreCase = true) }
            if (matchedSub != null) {
                _selectedSubject.value = matchedSub
            }
            _questionInput.value = entity.question
            _solverUiState.value = SolverUiState.Success(solution, isSaved = true)
            _currentScreen.value = Screen.SolveQuestion
        }
    }

    // --- Quiz generator actions ---
    fun setQuizSubject(subject: StudySubject) {
        _quizSubject.value = subject
        if (subject.popularTopics.isNotEmpty()) {
            _quizTopic.value = subject.popularTopics.first()
        }
    }

    fun setQuizTopic(topic: String) {
        _quizTopic.value = topic
    }

    fun setQuizDifficulty(diff: QuizDifficulty) {
        _quizDifficulty.value = diff
    }

    fun setQuizQuestionCount(count: Int) {
        _quizQuestionCount.value = count
    }

    fun startQuizGeneration() {
        _quizFlowState.value = QuizFlowState.Generating
        viewModelScope.launch {
            val result = repository.generateQuiz(
                subject = _quizSubject.value.name,
                topic = _quizTopic.value,
                difficulty = _quizDifficulty.value.label,
                count = _quizQuestionCount.value
            )
            result.onSuccess { quiz ->
                _quizFlowState.value = QuizFlowState.Active(quiz = quiz)

                // Save generated quiz template to Room DB
                repository.savePracticeQuiz(quiz = quiz)
            }.onFailure {
                // Return to setup
                _quizFlowState.value = QuizFlowState.Setup
            }
        }
    }

    fun quickQuizForSubject(subject: StudySubject, topic: String) {
        _quizSubject.value = subject
        _quizTopic.value = topic
        _currentScreen.value = Screen.PracticeQuizzes
        startQuizGeneration()
    }

    fun quizFromSolution(solution: QuestionSolution) {
        val subject = SubjectCatalog.subjects.find { it.name.equals(solution.subject, ignoreCase = true) }
            ?: SubjectCatalog.subjects.first()
        _quizSubject.value = subject
        _quizTopic.value = solution.coreConcepts.firstOrNull() ?: solution.subject
        _currentScreen.value = Screen.PracticeQuizzes
        startQuizGeneration()
    }

    fun selectQuizAnswer(questionId: Int, optionIndex: Int) {
        val state = _quizFlowState.value
        if (state is QuizFlowState.Active) {
            val updatedMap = state.userAnswers.toMutableMap()
            updatedMap[questionId] = optionIndex
            _quizFlowState.value = state.copy(
                userAnswers = updatedMap,
                isAnswerChecked = true
            )
        }
    }

    fun nextQuizQuestion() {
        val state = _quizFlowState.value
        if (state is QuizFlowState.Active) {
            val nextIndex = state.currentQuestionIndex + 1
            if (nextIndex < state.quiz.questions.size) {
                _quizFlowState.value = state.copy(
                    currentQuestionIndex = nextIndex,
                    isAnswerChecked = false
                )
            } else {
                finishQuiz(state)
            }
        }
    }

    fun previousQuizQuestion() {
        val state = _quizFlowState.value
        if (state is QuizFlowState.Active && state.currentQuestionIndex > 0) {
            _quizFlowState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex - 1,
                isAnswerChecked = state.userAnswers.containsKey(state.quiz.questions[state.currentQuestionIndex - 1].id)
            )
        }
    }

    private fun finishQuiz(activeState: QuizFlowState.Active) {
        var correctCount = 0
        val total = activeState.quiz.questions.size
        activeState.quiz.questions.forEach { q ->
            val userPick = activeState.userAnswers[q.id]
            if (userPick == q.correctIndex) {
                correctCount++
            }
        }
        val pct = if (total > 0) ((correctCount.toDouble() / total) * 100).toInt() else 0

        _quizFlowState.value = QuizFlowState.Completed(
            quiz = activeState.quiz,
            userAnswers = activeState.userAnswers,
            score = correctCount,
            total = total,
            percentage = pct
        )

        // Save record and full quiz attempt to Room DB
        viewModelScope.launch {
            repository.saveQuizRecord(
                QuizRecordEntity(
                    title = activeState.quiz.title,
                    subject = activeState.quiz.subject,
                    topic = activeState.quiz.topic,
                    difficulty = activeState.quiz.difficulty,
                    totalQuestions = total,
                    correctCount = correctCount,
                    percentage = pct,
                    timestamp = System.currentTimeMillis()
                )
            )
            repository.savePracticeQuiz(
                quiz = activeState.quiz,
                answers = activeState.userAnswers,
                score = correctCount,
                percentage = pct,
                isCompleted = true
            )
        }
    }

    fun resetQuizSetup() {
        _quizFlowState.value = QuizFlowState.Setup
    }

    fun retakeCurrentQuiz(quiz: PracticeQuiz) {
        _quizFlowState.value = QuizFlowState.Active(quiz = quiz)
    }

    fun deleteQuizRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteQuizRecord(id)
        }
    }

    // --- Saved Practice Quiz Actions ---

    fun openSavedQuizForReview(savedQuiz: SavedPracticeQuizEntity) {
        val quiz = repository.deserializeQuiz(savedQuiz.quizJson) ?: return
        val userAnswers = repository.deserializeUserAnswers(savedQuiz.userAnswersJson)
        _quizFlowState.value = QuizFlowState.Completed(
            quiz = quiz,
            userAnswers = userAnswers,
            score = savedQuiz.lastScore ?: 0,
            total = quiz.questions.size,
            percentage = savedQuiz.lastPercentage ?: 0
        )
        _currentScreen.value = Screen.PracticeQuizzes
    }

    fun retakeSavedQuiz(savedQuiz: SavedPracticeQuizEntity) {
        val quiz = repository.deserializeQuiz(savedQuiz.quizJson) ?: return
        _quizFlowState.value = QuizFlowState.Active(quiz = quiz)
        _currentScreen.value = Screen.PracticeQuizzes
    }

    fun deleteSavedQuiz(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedQuiz(id)
        }
    }

    fun toggleSavedQuizFavorite(id: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleSavedQuizFavorite(id, !currentFav)
        }
    }
}
