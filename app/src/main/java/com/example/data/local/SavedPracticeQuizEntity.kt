package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_practice_quizzes")
data class SavedPracticeQuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val questionCount: Int,
    val quizJson: String,
    val userAnswersJson: String? = null,
    val lastScore: Int? = null,
    val lastPercentage: Int? = null,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
