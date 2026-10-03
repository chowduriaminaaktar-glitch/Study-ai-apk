package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.ChatSessionEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.QuizRecordEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.SavedSolutionEntity
import com.example.data.local.StudyDao
import com.example.data.model.ExplanationStyle
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuestionSolution
import com.example.data.model.QuizQuestion
import com.example.data.model.SolutionStep
import com.example.data.remote.GeminiStudyService
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class StudyRepository(
    private val dao: StudyDao,
    private val geminiService: GeminiStudyService
) {
    val allSavedSolutions: Flow<List<SavedSolutionEntity>> = dao.getAllSavedSolutions()
    val allQuizRecords: Flow<List<QuizRecordEntity>> = dao.getAllQuizRecords()
    val allChatSessions: Flow<List<ChatSessionEntity>> = dao.getAllChatSessions()
    val allSavedQuizzes: Flow<List<SavedPracticeQuizEntity>> = dao.getAllSavedQuizzes()

    suspend fun solveQuestion(
        subject: String,
        question: String,
        style: ExplanationStyle,
        imageBitmap: Bitmap? = null
    ): Result<QuestionSolution> {
        return geminiService.solveQuestion(subject, question, style, imageBitmap)
    }

    suspend fun answerFollowUp(
        subject: String,
        originalQuestion: String,
        solutionSummary: String,
        followUpQuery: String
    ): Result<String> {
        return geminiService.answerFollowUp(subject, originalQuestion, solutionSummary, followUpQuery)
    }

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        count: Int = 5
    ): Result<PracticeQuiz> {
        return geminiService.generateQuiz(subject, topic, difficulty, count)
    }

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForSession(sessionId)
    }

    suspend fun getChatSessionById(sessionId: Long): ChatSessionEntity? {
        return dao.getChatSessionById(sessionId)
    }

    suspend fun createChatSession(title: String, subject: String, firstQuestion: String): Long {
        val session = ChatSessionEntity(
            title = title,
            subject = subject,
            previewText = firstQuestion.take(80),
            messageCount = 0
        )
        return dao.insertChatSession(session)
    }

    suspend fun addMessageToSession(
        sessionId: Long,
        sender: String,
        text: String,
        solution: QuestionSolution? = null
    ): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            sender = sender,
            text = text,
            solutionJson = solution?.let { serializeSolution(it) }
        )
        val msgId = dao.insertChatMessage(message)
        val messages = dao.getMessagesForSessionOnce(sessionId)
        dao.updateSessionMeta(
            id = sessionId,
            preview = text.take(80),
            count = messages.size,
            updatedAt = System.currentTimeMillis()
        )
        return msgId
    }

    suspend fun deleteChatSession(sessionId: Long) {
        dao.deleteMessagesForSession(sessionId)
        dao.deleteChatSession(sessionId)
    }

    suspend fun togglePinSession(sessionId: Long, isPinned: Boolean) {
        dao.updateSessionPinned(sessionId, isPinned)
    }

    suspend fun saveSolution(solution: QuestionSolution): Long {
        val entity = SavedSolutionEntity(
            subject = solution.subject,
            question = solution.question,
            directAnswer = solution.directAnswer,
            solutionJson = serializeSolution(solution),
            isFavorite = false,
            timestamp = solution.timestamp
        )
        return dao.insertSolution(entity)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteSolution(id: Long) {
        dao.deleteSolutionById(id)
    }

    suspend fun saveQuizRecord(record: QuizRecordEntity): Long {
        return dao.insertQuizRecord(record)
    }

    suspend fun deleteQuizRecord(id: Long) {
        dao.deleteQuizRecord(id)
    }

    suspend fun savePracticeQuiz(
        quiz: PracticeQuiz,
        answers: Map<Int, Int>? = null,
        score: Int? = null,
        percentage: Int? = null,
        isCompleted: Boolean = false
    ): Long {
        val entity = SavedPracticeQuizEntity(
            title = quiz.title,
            subject = quiz.subject,
            topic = quiz.topic,
            difficulty = quiz.difficulty,
            questionCount = quiz.questions.size,
            quizJson = serializeQuiz(quiz),
            userAnswersJson = answers?.let { serializeUserAnswers(it) },
            lastScore = score,
            lastPercentage = percentage,
            isCompleted = isCompleted
        )
        return dao.insertSavedQuiz(entity)
    }

    suspend fun updateSavedQuizAttempt(
        id: Long,
        answers: Map<Int, Int>,
        score: Int,
        percentage: Int
    ) {
        dao.updateSavedQuizResult(
            id = id,
            answersJson = serializeUserAnswers(answers),
            score = score,
            percentage = percentage,
            isCompleted = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun toggleSavedQuizFavorite(id: Long, isFavorite: Boolean) {
        dao.updateSavedQuizFavorite(id, isFavorite)
    }

    suspend fun deleteSavedQuiz(id: Long) {
        dao.deleteSavedQuiz(id)
    }

    fun serializeSolution(s: QuestionSolution): String {
        val root = JSONObject()
        root.put("id", s.id)
        root.put("subject", s.subject)
        root.put("question", s.question)
        root.put("directAnswer", s.directAnswer)
        root.put("timestamp", s.timestamp)
        root.put("followUpPractice", s.followUpPractice)

        val stepsArr = JSONArray()
        for (step in s.steps) {
            val stepObj = JSONObject()
            stepObj.put("stepNumber", step.stepNumber)
            stepObj.put("title", step.title)
            stepObj.put("explanation", step.explanation)
            stepObj.put("formulaOrDetail", step.formulaOrDetail ?: "")
            stepsArr.put(stepObj)
        }
        root.put("steps", stepsArr)

        val conceptsArr = JSONArray()
        s.coreConcepts.forEach { conceptsArr.put(it) }
        root.put("coreConcepts", conceptsArr)

        val tipsArr = JSONArray()
        s.tipsAndCommonMistakes.forEach { tipsArr.put(it) }
        root.put("tipsAndCommonMistakes", tipsArr)

        return root.toString()
    }

    fun deserializeSolution(jsonStr: String): QuestionSolution? {
        return try {
            val root = JSONObject(jsonStr)
            val stepsArr = root.optJSONArray("steps") ?: JSONArray()
            val stepsList = mutableListOf<SolutionStep>()
            for (i in 0 until stepsArr.length()) {
                val obj = stepsArr.getJSONObject(i)
                stepsList.add(
                    SolutionStep(
                        stepNumber = obj.optInt("stepNumber", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        explanation = obj.optString("explanation", ""),
                        formulaOrDetail = obj.optString("formulaOrDetail").takeIf { it.isNotBlank() }
                    )
                )
            }

            val conceptsArr = root.optJSONArray("coreConcepts") ?: JSONArray()
            val conceptsList = mutableListOf<String>()
            for (i in 0 until conceptsArr.length()) {
                conceptsList.add(conceptsArr.getString(i))
            }

            val tipsArr = root.optJSONArray("tipsAndCommonMistakes") ?: JSONArray()
            val tipsList = mutableListOf<String>()
            for (i in 0 until tipsArr.length()) {
                tipsList.add(tipsArr.getString(i))
            }

            QuestionSolution(
                id = root.optString("id", UUID.randomUUID().toString()),
                subject = root.optString("subject", "General"),
                question = root.optString("question", ""),
                directAnswer = root.optString("directAnswer", ""),
                steps = stepsList,
                coreConcepts = conceptsList,
                tipsAndCommonMistakes = tipsList,
                followUpPractice = root.optString("followUpPractice", ""),
                timestamp = root.optLong("timestamp", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    fun serializeQuiz(quiz: PracticeQuiz): String {
        val root = JSONObject()
        root.put("id", quiz.id)
        root.put("title", quiz.title)
        root.put("subject", quiz.subject)
        root.put("topic", quiz.topic)
        root.put("difficulty", quiz.difficulty)
        root.put("createdAt", quiz.createdAt)

        val qArr = JSONArray()
        for (q in quiz.questions) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("questionText", q.questionText)
            qObj.put("correctIndex", q.correctIndex)
            qObj.put("explanation", q.explanation)
            qObj.put("conceptTested", q.conceptTested)
            val optArr = JSONArray()
            q.options.forEach { optArr.put(it) }
            qObj.put("options", optArr)
            qArr.put(qObj)
        }
        root.put("questions", qArr)
        return root.toString()
    }

    fun deserializeQuiz(jsonStr: String): PracticeQuiz? {
        return try {
            val root = JSONObject(jsonStr)
            val qArr = root.optJSONArray("questions") ?: JSONArray()
            val qList = mutableListOf<QuizQuestion>()
            for (i in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(i)
                val optArr = qObj.optJSONArray("options") ?: JSONArray()
                val optList = mutableListOf<String>()
                for (j in 0 until optArr.length()) {
                    optList.add(optArr.getString(j))
                }
                qList.add(
                    QuizQuestion(
                        id = qObj.optInt("id", i + 1),
                        questionText = qObj.optString("questionText", ""),
                        options = optList,
                        correctIndex = qObj.optInt("correctIndex", 0),
                        explanation = qObj.optString("explanation", ""),
                        conceptTested = qObj.optString("conceptTested", "")
                    )
                )
            }
            PracticeQuiz(
                id = root.optString("id", UUID.randomUUID().toString()),
                title = root.optString("title", "Practice Quiz"),
                subject = root.optString("subject", "General"),
                topic = root.optString("topic", "General"),
                difficulty = root.optString("difficulty", "Intermediate"),
                questions = qList,
                createdAt = root.optLong("createdAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    fun serializeUserAnswers(answers: Map<Int, Int>): String {
        val root = JSONObject()
        for ((k, v) in answers) {
            root.put(k.toString(), v)
        }
        return root.toString()
    }

    fun deserializeUserAnswers(jsonStr: String?): Map<Int, Int> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            val root = JSONObject(jsonStr)
            val map = mutableMapOf<Int, Int>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k.toInt()] = root.getInt(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
