package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_records")
data class QuizRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)
