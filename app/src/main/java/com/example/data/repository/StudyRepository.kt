package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
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

class StudyRepository(
    private val dao: StudyDao,
    private val geminiService: GeminiStudyService
) {

    val allSavedSolutions: Flow<List<SavedSolutionEntity>> = dao.getAllSavedSolutions()
    val allQuizRecords: Flow<List<QuizRecordEntity>> = dao.getAllQuizRecords()
    val allChatSessions: Flow<List<ChatSessionEntity>> = dao.getAllChatSessions()
    val allSavedQuizzes: Flow<List<SavedPracticeQuizEntity>> = dao.getAllSavedQuizzes()

    fun isApiKeyConfigured(): Boolean = geminiService.isApiKeyConfigured()

    suspend fun solveQuestion(
        subject: String,
        question: String,
        style: ExplanationStyle
    ): Result<QuestionSolution> {
        return geminiService.solveQuestion(subject, question, style)
    }

    suspend fun answerFollowUp(
        subject: String,
        originalQuestion: String,
        conversationHistory: List<Pair<String, String>>,
        followUpQuery: String
    ): Result<String> {
        return geminiService.answerFollowUp(subject, originalQuestion, conversationHistory, followUpQuery)
    }

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        count: Int
    ): Result<PracticeQuiz> {
        return geminiService.generateQuiz(subject, topic, difficulty, count)
    }

    // --- Chat Sessions Management ---
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForSession(sessionId)
    }

    suspend fun getChatSessionById(sessionId: Long): ChatSessionEntity? {
        return dao.getChatSessionById(sessionId)
    }

    suspend fun createChatSession(
        title: String,
        subject: String,
        firstQuestion: String
    ): Long {
        val snippet = if (firstQuestion.length > 80) firstQuestion.take(77) + "..." else firstQuestion
        val session = ChatSessionEntity(
            title = title,
            subject = subject,
            previewText = snippet,
            messageCount = 0,
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertChatSession(session)
    }

    suspend fun addMessageToSession(
        sessionId: Long,
        sender: String,
        text: String,
        solution: QuestionSolution? = null
    ): Long {
        val solutionJson = solution?.let { serializeSolution(it) }
        val msg = ChatMessageEntity(
            sessionId = sessionId,
            sender = sender,
            text = text,
            solutionJson = solutionJson,
            timestamp = System.currentTimeMillis()
        )
        val msgId = dao.insertChatMessage(msg)

        val snippet = if (text.length > 80) text.take(77) + "..." else text
        val currentCount = dao.getMessagesForSessionOnce(sessionId).size
        dao.updateSessionMeta(
            id = sessionId,
            preview = snippet,
            count = currentCount,
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

    // --- Saved Solutions Management ---
    suspend fun saveSolution(solution: QuestionSolution): Long {
        val json = serializeSolution(solution)
        val entity = SavedSolutionEntity(
            subject = solution.subject,
            question = solution.question,
            directAnswer = solution.directAnswer,
            solutionJson = json,
            isFavorite = false,
            timestamp = System.currentTimeMillis()
        )
        return dao.insertSolution(entity)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteSolution(id: Long) {
        dao.deleteSolutionById(id)
    }

    // --- Quiz Records & Saved Practice Quizzes Management ---
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
        val quizJson = serializeQuiz(quiz)
        val answersJson = answers?.let { serializeUserAnswers(it) }
        val entity = SavedPracticeQuizEntity(
            title = quiz.title,
            subject = quiz.subject,
            topic = quiz.topic,
            difficulty = quiz.difficulty,
            questionCount = quiz.questions.size,
            quizJson = quizJson,
            userAnswersJson = answersJson,
            lastScore = score,
            lastPercentage = percentage,
            isCompleted = isCompleted,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertSavedQuiz(entity)
    }

    suspend fun updateSavedQuizAttempt(
        id: Long,
        answers: Map<Int, Int>,
        score: Int,
        percentage: Int
    ) {
        val answersJson = serializeUserAnswers(answers)
        dao.updateSavedQuizResult(
            id = id,
            answersJson = answersJson,
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

    // --- Serialization Helpers ---
    fun serializeSolution(s: QuestionSolution): String {
        val root = JSONObject().apply {
            put("id", s.id)
            put("subject", s.subject)
            put("question", s.question)
            put("directAnswer", s.directAnswer)
            put("followUpPractice", s.followUpPractice)

            val stepsArray = JSONArray().apply {
                s.steps.forEach { step ->
                    val stepObj = JSONObject().apply {
                        put("stepNumber", step.stepNumber)
                        put("title", step.title)
                        put("explanation", step.explanation)
                        step.formulaOrDetail?.let { put("formulaOrDetail", it) }
                    }
                    put(stepObj)
                }
            }
            put("steps", stepsArray)

            val conceptsArray = JSONArray().apply {
                s.coreConcepts.forEach { put(it) }
            }
            put("coreConcepts", conceptsArray)

            val tipsArray = JSONArray().apply {
                s.tipsAndCommonMistakes.forEach { put(it) }
            }
            put("tipsAndCommonMistakes", tipsArray)
        }
        return root.toString()
    }

    fun deserializeSolution(jsonStr: String): QuestionSolution? {
        return try {
            val obj = JSONObject(jsonStr)
            val stepsList = mutableListOf<SolutionStep>()
            val stepsArr = obj.optJSONArray("steps")
            if (stepsArr != null) {
                for (i in 0 until stepsArr.length()) {
                    val s = stepsArr.getJSONObject(i)
                    stepsList.add(
                        SolutionStep(
                            stepNumber = s.optInt("stepNumber", i + 1),
                            title = s.optString("title", "Step ${i + 1}"),
                            explanation = s.optString("explanation", ""),
                            formulaOrDetail = s.optString("formulaOrDetail", "").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            val conceptsList = mutableListOf<String>()
            val conceptsArr = obj.optJSONArray("coreConcepts")
            if (conceptsArr != null) {
                for (i in 0 until conceptsArr.length()) {
                    conceptsList.add(conceptsArr.getString(i))
                }
            }

            val tipsList = mutableListOf<String>()
            val tipsArr = obj.optJSONArray("tipsAndCommonMistakes")
            if (tipsArr != null) {
                for (i in 0 until tipsArr.length()) {
                    tipsList.add(tipsArr.getString(i))
                }
            }

            QuestionSolution(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                subject = obj.optString("subject", "General"),
                question = obj.optString("question", ""),
                directAnswer = obj.optString("directAnswer", ""),
                steps = stepsList,
                coreConcepts = conceptsList,
                tipsAndCommonMistakes = tipsList,
                followUpPractice = obj.optString("followUpPractice", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun serializeQuiz(quiz: PracticeQuiz): String {
        val root = JSONObject().apply {
            put("id", quiz.id)
            put("title", quiz.title)
            put("subject", quiz.subject)
            put("topic", quiz.topic)
            put("difficulty", quiz.difficulty)
            put("createdAt", quiz.createdAt)

            val questionsArr = JSONArray().apply {
                quiz.questions.forEach { q ->
                    val qObj = JSONObject().apply {
                        put("id", q.id)
                        put("questionText", q.questionText)
                        put("correctIndex", q.correctIndex)
                        put("explanation", q.explanation)
                        put("conceptTested", q.conceptTested)
                        val optArr = JSONArray().apply {
                            q.options.forEach { put(it) }
                        }
                        put("options", optArr)
                    }
                    put(qObj)
                }
            }
            put("questions", questionsArr)
        }
        return root.toString()
    }

    fun deserializeQuiz(jsonStr: String): PracticeQuiz? {
        return try {
            val root = JSONObject(jsonStr)
            val qList = mutableListOf<QuizQuestion>()
            val qArr = root.optJSONArray("questions")
            if (qArr != null) {
                for (i in 0 until qArr.length()) {
                    val qObj = qArr.getJSONObject(i)
                    val optList = mutableListOf<String>()
                    val optArr = qObj.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) {
                            optList.add(optArr.getString(j))
                        }
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
            }

            PracticeQuiz(
                id = root.optString("id", java.util.UUID.randomUUID().toString()),
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
        answers.forEach { (qid, optIdx) ->
            root.put(qid.toString(), optIdx)
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
