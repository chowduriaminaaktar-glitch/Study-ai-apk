package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Saved individual question solutions ---
    @Query("SELECT * FROM saved_solutions ORDER BY timestamp DESC")
    fun getAllSavedSolutions(): Flow<List<SavedSolutionEntity>>

    @Query("SELECT * FROM saved_solutions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteSolutions(): Flow<List<SavedSolutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolution(solution: SavedSolutionEntity): Long

    @Query("UPDATE saved_solutions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM saved_solutions WHERE id = :id")
    suspend fun deleteSolutionById(id: Long)

    @Query("DELETE FROM saved_solutions")
    suspend fun deleteAllSolutions()

    // --- Quiz completion records ---
    @Query("SELECT * FROM quiz_records ORDER BY timestamp DESC")
    fun getAllQuizRecords(): Flow<List<QuizRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizRecord(record: QuizRecordEntity): Long

    @Query("DELETE FROM quiz_records WHERE id = :id")
    suspend fun deleteQuizRecord(id: Long)

    @Query("DELETE FROM quiz_records")
    suspend fun deleteAllQuizRecords()

    // --- Chat Sessions ---
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getChatSessionById(sessionId: Long): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSessionEntity): Long

    @Query("UPDATE chat_sessions SET previewText = :preview, messageCount = :count, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSessionMeta(id: Long, preview: String, count: Int, updatedAt: Long)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :id")
    suspend fun updateSessionPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteChatSession(id: Long)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllChatSessions()

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesForSessionOnce(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteChatMessage(id: Long)

    // --- Saved Practice Quizzes ---
    @Query("SELECT * FROM saved_practice_quizzes ORDER BY updatedAt DESC")
    fun getAllSavedQuizzes(): Flow<List<SavedPracticeQuizEntity>>

    @Query("SELECT * FROM saved_practice_quizzes WHERE id = :id LIMIT 1")
    suspend fun getSavedQuizById(id: Long): SavedPracticeQuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedQuiz(quiz: SavedPracticeQuizEntity): Long

    @Query("UPDATE saved_practice_quizzes SET userAnswersJson = :answersJson, lastScore = :score, lastPercentage = :percentage, isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSavedQuizResult(
        id: Long,
        answersJson: String,
        score: Int,
        percentage: Int,
        isCompleted: Boolean,
        updatedAt: Long
    )

    @Query("UPDATE saved_practice_quizzes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateSavedQuizFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM saved_practice_quizzes WHERE id = :id")
    suspend fun deleteSavedQuiz(id: Long)

    @Query("DELETE FROM saved_practice_quizzes")
    suspend fun deleteAllSavedQuizzes()
}
