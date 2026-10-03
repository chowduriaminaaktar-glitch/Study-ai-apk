package com.example.data.model

import java.util.UUID

enum class ExplanationStyle(val label: String, val promptDescription: String) {
    STEP_BY_STEP(
        "Step-by-Step",
        "Provide an exhaustive, crystal-clear step-by-step mathematical or logical derivation"
    ),
    INTUITIVE_ELI5(
        "Intuitive / Analogy",
        "Explain using vivid real-world intuition, analogies, and simplified mental models"
    ),
    DEEP_CONCEPTUAL(
        "Deep Conceptual",
        "Explain underlying theory, edge cases, proofs, and academic significance"
    ),
    QUICK_EXAM_TIPS(
        "Exam Speed & Shortcuts",
        "Focus on rapid solution tricks, exam shortcuts, and common trap detection"
    )
}

data class SolutionStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val formulaOrDetail: String? = null
)

data class QuestionSolution(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val question: String,
    val directAnswer: String,
    val steps: List<SolutionStep>,
    val coreConcepts: List<String>,
    val tipsAndCommonMistakes: List<String>,
    val followUpPractice: String,
    val timestamp: Long = System.currentTimeMillis()
)
