package com.example.ui

sealed class Screen(val title: String) {
    data object Home : Screen("Study AI")
    data object Chat : Screen("AI Chat")
    data object StudyAiLive : Screen("Study AI Live")
    data object SolveQuestion : Screen("Ask StudyAI")
    data object PracticeQuizzes : Screen("Quizzes")
    data object Library : Screen("Digital Library")
    data object SavedAndHistory : Screen("Saved & History")
    data object SubjectExplore : Screen("Explore")
    data object Auth : Screen("Account")
}

sealed interface SolverUiState {
    data object Idle : SolverUiState
    data class Loading(val question: String) : SolverUiState
    data class Success(val solution: com.example.data.model.QuestionSolution, val isSaved: Boolean = false) : SolverUiState
    data class Error(val message: String) : SolverUiState
}

sealed interface QuizFlowState {
    data object Setup : QuizFlowState
    data class Generating(val topic: String) : QuizFlowState
    data class Active(
        val quiz: com.example.data.model.PracticeQuiz,
        val currentQuestionIndex: Int = 0,
        val userAnswers: Map<Int, Int> = emptyMap(),
        val isAnswerChecked: Boolean = false
    ) : QuizFlowState
    data class Completed(
        val quiz: com.example.data.model.PracticeQuiz,
        val userAnswers: Map<Int, Int>,
        val score: Int,
        val total: Int,
        val percentage: Int
    ) : QuizFlowState
    data class Error(val message: String) : QuizFlowState
}
