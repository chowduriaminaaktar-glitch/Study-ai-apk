package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_solutions")
data class SavedSolutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val subject: String,
    val question: String,
    val directAnswer: String,
    val solutionJson: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
