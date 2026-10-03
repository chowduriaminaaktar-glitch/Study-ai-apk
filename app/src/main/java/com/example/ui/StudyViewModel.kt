package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatSessionEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.SavedSolutionEntity
import com.example.data.model.ExplanationStyle
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuestionSolution
import com.example.data.model.QuizDifficulty
import com.example.data.model.StudySubject
import com.example.data.model.SubjectCatalog
import com.example.data.model.UserAccount
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.AuthRepository
import com.example.data.repository.StudyRepository
import com.example.ui.theme.ThemeManager
import com.studyai.app.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    val repository: StudyRepository
    val geminiService: GeminiStudyService

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedSubject = MutableStateFlow(SubjectCatalog.subjects.first())
    val selectedSubject: StateFlow<StudySubject> = _selectedSubject.asStateFlow()

    private val _questionInput = MutableStateFlow("")
    val questionInput: StateFlow<String> = _questionInput.asStateFlow()

    private val _explanationStyle = MutableStateFlow(ExplanationStyle.STEP_BY_STEP)
    val explanationStyle: StateFlow<ExplanationStyle> = _explanationStyle.asStateFlow()

    private val _solverUiState = MutableStateFlow<SolverUiState>(SolverUiState.Idle)
    val solverUiState: StateFlow<SolverUiState> = _solverUiState.asStateFlow()

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

    // Quiz flow
    private val _quizSubject = MutableStateFlow(SubjectCatalog.subjects.first())
    val quizSubject: StateFlow<StudySubject> = _quizSubject.asStateFlow()

    private val _quizTopic = MutableStateFlow("Calculus")
    val quizTopic: StateFlow<String> = _quizTopic.asStateFlow()

    private val _quizDifficulty = MutableStateFlow(QuizDifficulty.MEDIUM)
    val quizDifficulty: StateFlow<QuizDifficulty> = _quizDifficulty.asStateFlow()

    private val _quizQuestionCount = MutableStateFlow(5)
    val quizQuestionCount: StateFlow<Int> = _quizQuestionCount.asStateFlow()

    private val _quizFlowState = MutableStateFlow<QuizFlowState>(QuizFlowState.Setup)
    val quizFlowState: StateFlow<QuizFlowState> = _quizFlowState.asStateFlow()

    val savedSolutions: StateFlow<List<SavedSolutionEntity>>
    val quizRecords: StateFlow<List<QuizRecordEntity>>
    val chatSessions: StateFlow<List<ChatSessionEntity>>
    val savedQuizzes: StateFlow<List<SavedPracticeQuizEntity>>

    init {
        ThemeManager.initialize(application)
        val db = AppDatabase.getInstance(application)
        geminiService = GeminiStudyService()
        repository = StudyRepository(db.studyDao(), geminiService)

        savedSolutions = repository.allSavedSolutions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        quizRecords = repository.allQuizRecords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        chatSessions = repository.allChatSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        savedQuizzes = repository.allSavedQuizzes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    val isApiKeyConfigured: Boolean
        get() = try {
            BuildConfig.GEMINI_API_KEY.isNotBlank()
        } catch (e: Throwable) {
            false
        }

    fun signOut() {
        authRepository.signOut()
    }

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
            _questionInput.value = current.trimEnd() + " " + spokenText.trim()
        }
    }

    fun replaceSpokenText(spokenText: String) {
        _questionInput.value = spokenText.trim()
    }

    fun setExplanationStyle(style: ExplanationStyle) {
        _explanationStyle.value = style
    }

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
            repository.getMessagesForSession(sessionId).collect { msgs ->
                _currentChatMessages.value = msgs
                val lastSol = msgs.lastOrNull { it.solutionJson != null }
                if (lastSol != null && _solverUiState.value !is SolverUiState.Loading) {
                    val parsed = repository.deserializeSolution(lastSol.solutionJson ?: "")
                    if (parsed != null) {
                        _solverUiState.value = SolverUiState.Success(parsed, false)
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
            repository.togglePinSession(sessionId, isPinned)
        }
    }

    fun solveQuestion() {
        val query = _questionInput.value.trim()
        if (query.isBlank()) return

        _solverUiState.value = SolverUiState.Loading(query)

        viewModelScope.launch {
            val result = repository.solveQuestion(
                subject = _selectedSubject.value.name,
                question = query,
                style = _explanationStyle.value
            )

            result.onSuccess { solution ->
                _solverUiState.value = SolverUiState.Success(solution, false)
                val currentSid = _currentChatSessionId.value
                val sid = if (currentSid == null) {
                    val newSid = repository.createChatSession(
                        title = query.take(50),
                        subject = _selectedSubject.value.name,
                        firstQuestion = query
                    )
                    _currentChatSessionId.value = newSid
                    observeSessionMessages(newSid)
                    newSid
                } else currentSid

                repository.addMessageToSession(sid, "user", query)
                repository.addMessageToSession(sid, "assistant", solution.directAnswer, solution)
            }.onFailure { err ->
                _solverUiState.value = SolverUiState.Error(err.message ?: "Failed to generate solution")
            }
        }
    }

    fun sendFollowUpMessage() {
        val query = _followUpInput.value.trim()
        val sid = _currentChatSessionId.value ?: return
        if (query.isBlank()) return

        _followUpInput.value = ""
        _isSendingFollowUp.value = true

        viewModelScope.launch {
            repository.addMessageToSession(sid, "user", query)
            val currentState = _solverUiState.value
            val currentSol = (currentState as? SolverUiState.Success)?.solution
            val result = repository.answerFollowUp(
                subject = _selectedSubject.value.name,
                originalQuestion = currentSol?.question ?: _questionInput.value,
                solutionSummary = currentSol?.directAnswer ?: "",
                followUpQuery = query
            )
            result.onSuccess { reply ->
                repository.addMessageToSession(sid, "assistant", reply)
            }.onFailure { err ->
                repository.addMessageToSession(sid, "assistant", "Error: ${err.message}")
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
            val curr = _solverUiState.value
            if (curr is SolverUiState.Success) {
                _solverUiState.value = curr.copy(isSaved = true)
            }
        }
    }

    fun saveCurrentSolution() {
        val curr = (_solverUiState.value as? SolverUiState.Success)?.solution ?: return
        saveCurrentSolution(curr)
    }

    fun startQuizFromCurrentQuestion() {
        val curr = (_solverUiState.value as? SolverUiState.Success)?.solution ?: return
        quizFromSolution(curr)
    }

    fun openSavedSolution(entity: SavedSolutionEntity) {
        viewSavedSolution(entity)
    }

    fun selectQuizSubject(subject: StudySubject) {
        setQuizSubject(subject)
    }

    fun generateQuiz() {
        startQuizGeneration()
    }

    fun checkAnswer() {
        val state = _quizFlowState.value as? QuizFlowState.Active ?: return
        _quizFlowState.value = state.copy(isAnswerChecked = true)
    }

    fun nextQuestion() {
        nextQuizQuestion()
    }

    fun previousQuestion() {
        previousQuizQuestion()
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
        val solution = repository.deserializeSolution(entity.solutionJson) ?: return
        val matchedSub = SubjectCatalog.subjects.find { it.name.equals(entity.subject, ignoreCase = true) }
        if (matchedSub != null) {
            _selectedSubject.value = matchedSub
        }
        _questionInput.value = entity.question
        _solverUiState.value = SolverUiState.Success(solution, true)
        _currentScreen.value = Screen.SolveQuestion
    }

    // Quiz flow methods
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
        val topic = _quizTopic.value
        _quizFlowState.value = QuizFlowState.Generating(topic)
        viewModelScope.launch {
            val result = repository.generateQuiz(
                subject = _quizSubject.value.name,
                topic = topic,
                difficulty = _quizDifficulty.value.label,
                count = _quizQuestionCount.value
            )
            result.onSuccess { quiz ->
                _quizFlowState.value = QuizFlowState.Active(quiz = quiz)
            }.onFailure { err ->
                _quizFlowState.value = QuizFlowState.Error(err.message ?: "Failed to generate quiz")
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
        val matched = SubjectCatalog.subjects.find { it.name.equals(solution.subject, ignoreCase = true) }
            ?: SubjectCatalog.subjects.first()
        _quizSubject.value = matched
        _quizTopic.value = solution.coreConcepts.firstOrNull() ?: solution.subject
        _currentScreen.value = Screen.PracticeQuizzes
        startQuizGeneration()
    }

    fun selectQuizAnswer(questionId: Int, optionIndex: Int) {
        val state = _quizFlowState.value as? QuizFlowState.Active ?: return
        val updated = state.userAnswers.toMutableMap()
        updated[questionId] = optionIndex
        _quizFlowState.value = state.copy(userAnswers = updated, isAnswerChecked = true)
    }

    fun nextQuizQuestion() {
        val state = _quizFlowState.value as? QuizFlowState.Active ?: return
        val nextIdx = state.currentQuestionIndex + 1
        if (nextIdx < state.quiz.questions.size) {
            _quizFlowState.value = state.copy(
                currentQuestionIndex = nextIdx,
                isAnswerChecked = state.userAnswers.containsKey(state.quiz.questions[nextIdx].id)
            )
        } else {
            finishQuiz(state)
        }
    }

    fun previousQuizQuestion() {
        val state = _quizFlowState.value as? QuizFlowState.Active ?: return
        if (state.currentQuestionIndex > 0) {
            val prevIdx = state.currentQuestionIndex - 1
            _quizFlowState.value = state.copy(
                currentQuestionIndex = prevIdx,
                isAnswerChecked = state.userAnswers.containsKey(state.quiz.questions[prevIdx].id)
            )
        }
    }

    private fun finishQuiz(activeState: QuizFlowState.Active) {
        var correct = 0
        val total = activeState.quiz.questions.size
        for (q in activeState.quiz.questions) {
            if (activeState.userAnswers[q.id] == q.correctIndex) {
                correct++
            }
        }
        val pct = if (total > 0) (correct * 100) / total else 0
        _quizFlowState.value = QuizFlowState.Completed(
            quiz = activeState.quiz,
            userAnswers = activeState.userAnswers,
            score = correct,
            total = total,
            percentage = pct
        )

        viewModelScope.launch {
            repository.saveQuizRecord(
                QuizRecordEntity(
                    title = activeState.quiz.title,
                    subject = activeState.quiz.subject,
                    topic = activeState.quiz.topic,
                    difficulty = activeState.quiz.difficulty,
                    totalQuestions = total,
                    correctCount = correct,
                    percentage = pct
                )
            )
            repository.savePracticeQuiz(
                quiz = activeState.quiz,
                answers = activeState.userAnswers,
                score = correct,
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

    fun openSavedQuizForReview(savedQuiz: SavedPracticeQuizEntity) {
        val quiz = repository.deserializeQuiz(savedQuiz.quizJson) ?: return
        val answers = repository.deserializeUserAnswers(savedQuiz.userAnswersJson)
        val score = savedQuiz.lastScore ?: 0
        val total = savedQuiz.questionCount
        val pct = savedQuiz.lastPercentage ?: if (total > 0) (score * 100) / total else 0

        _quizFlowState.value = QuizFlowState.Completed(
            quiz = quiz,
            userAnswers = answers,
            score = score,
            total = total,
            percentage = pct
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

    fun toggleSavedQuizFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleSavedQuizFavorite(id, !isFavorite)
        }
    }
}
