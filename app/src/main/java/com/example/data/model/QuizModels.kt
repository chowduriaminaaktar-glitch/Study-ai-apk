package com.example.data.model

import java.util.UUID

enum class QuizDifficulty(val label: String) {
    EASY("Foundational"),
    MEDIUM("Intermediate"),
    HARD("Advanced"),
    EXAM("Exam Level")
}

data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val conceptTested: String
)

data class PracticeQuiz(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val questions: List<QuizQuestion>,
    val createdAt: Long = System.currentTimeMillis()
)

data class QuizResultSummary(
    val id: Long = 0L,
    val quizTitle: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)
