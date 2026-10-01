package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.SavedPracticeQuizEntity
import com.example.data.local.StudyDao
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuestionSolution
import com.example.data.model.QuizQuestion
import com.example.data.model.SolutionStep
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StudyDao
    private lateinit var repository: StudyRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.studyDao()
        repository = StudyRepository(dao, GeminiStudyService())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testStoreAndRetrieveChatSessionWithMessages() = runBlocking {
        // 1. Create a study chat session in Room
        val sessionId = repository.createChatSession(
            title = "Solve for x in 2x^2 + 5x - 3 = 0",
            subject = "Mathematics",
            firstQuestion = "Solve for x in 2x^2 + 5x - 3 = 0 by factoring"
        )
        assertTrue(sessionId > 0)

        // 2. Add user question message
        val userMsgId = repository.addMessageToSession(
            sessionId = sessionId,
            sender = "USER",
            text = "Solve for x in 2x^2 + 5x - 3 = 0 by factoring"
        )
        assertTrue(userMsgId > 0)

        // 3. Add AI solution answer
        val sampleSolution = QuestionSolution(
            id = "math-sol-1",
            subject = "Mathematics",
            question = "Solve for x in 2x^2 + 5x - 3 = 0 by factoring",
            directAnswer = "x = 1/2 or x = -3",
            steps = listOf(
                SolutionStep(
                    stepNumber = 1,
                    title = "Factor the quadratic equation",
                    explanation = "Find two numbers whose product is 2*(-3) = -6 and sum is 5: 6 and -1.",
                    formulaOrDetail = "(2x - 1)(x + 3) = 0"
                ),
                SolutionStep(
                    stepNumber = 2,
                    title = "Solve each factor",
                    explanation = "Set each factor equal to zero: 2x - 1 = 0 => x = 1/2; x + 3 = 0 => x = -3.",
                    formulaOrDetail = "x_1 = 1/2, x_2 = -3"
                )
            ),
            coreConcepts = listOf("Quadratic Equations", "Factoring Polynomials"),
            tipsAndCommonMistakes = listOf("Always check signs when factoring"),
            followUpPractice = "Try solving 3x^2 + 7x - 6 = 0"
        )

        val aiMsgId = repository.addMessageToSession(
            sessionId = sessionId,
            sender = "AI",
            text = sampleSolution.directAnswer,
            solution = sampleSolution
        )
        assertTrue(aiMsgId > 0)

        // 4. Add follow-up question
        repository.addMessageToSession(
            sessionId = sessionId,
            sender = "USER",
            text = "Can you show how to verify the roots using the quadratic formula?"
        )

        // 5. Query session and verify messages in Room
        val session = repository.getChatSessionById(sessionId)
        assertNotNull(session)
        assertEquals("Solve for x in 2x^2 + 5x - 3 = 0", session?.title)
        assertEquals("Mathematics", session?.subject)

        val messages = repository.getMessagesForSession(sessionId).first()
        assertEquals(3, messages.size)
        assertEquals("USER", messages[0].sender)
        assertEquals("AI", messages[1].sender)
        assertEquals("USER", messages[2].sender)

        // Verify deserialization of stored solution in message
        assertNotNull(messages[1].solutionJson)
        val deserialized = repository.deserializeSolution(messages[1].solutionJson!!)
        assertNotNull(deserialized)
        assertEquals("x = 1/2 or x = -3", deserialized?.directAnswer)
        assertEquals(2, deserialized?.steps?.size)
    }

    @Test
    fun testStoreAndRetrieveSavedPracticeQuiz() = runBlocking {
        val quiz = PracticeQuiz(
            id = "quiz-physics-1",
            title = "Physics: Newton's Laws Practice",
            subject = "Physics",
            topic = "Newton's Laws",
            difficulty = "Medium",
            questions = listOf(
                QuizQuestion(
                    id = 1,
                    questionText = "What is the net force on an object moving at constant velocity?",
                    options = listOf("0 N", "Greater than zero", "Equal to its weight", "Depends on friction"),
                    correctIndex = 0,
                    explanation = "By Newton's First Law, constant velocity means zero acceleration, hence zero net force.",
                    conceptTested = "Newton's First Law"
                ),
                QuizQuestion(
                    id = 2,
                    questionText = "Which formula represents Newton's Second Law?",
                    options = listOf("F = ma", "E = mc^2", "p = mv", "W = Fd"),
                    correctIndex = 0,
                    explanation = "Newton's Second Law relates force, mass, and acceleration as F = ma.",
                    conceptTested = "Newton's Second Law"
                )
            ),
            createdAt = System.currentTimeMillis()
        )

        // Save practice quiz with attempt answers into Room
        val answers = mapOf(1 to 0, 2 to 0) // Both correct
        val savedId = repository.savePracticeQuiz(
            quiz = quiz,
            answers = answers,
            score = 2,
            percentage = 100,
            isCompleted = true
        )
        assertTrue(savedId > 0)

        // Retrieve from Room
        val allQuizzes = repository.allSavedQuizzes.first()
        assertTrue(allQuizzes.isNotEmpty())
        val savedRecord = allQuizzes.first { it.id == savedId }
        assertEquals("Physics: Newton's Laws Practice", savedRecord.title)
        assertEquals("Physics", savedRecord.subject)
        assertEquals(2, savedRecord.lastScore)
        assertEquals(100, savedRecord.lastPercentage)
        assertTrue(savedRecord.isCompleted)

        // Deserialize quiz and user answers
        val deserializedQuiz = repository.deserializeQuiz(savedRecord.quizJson)
        assertNotNull(deserializedQuiz)
        assertEquals(2, deserializedQuiz?.questions?.size)

        val deserializedAnswers = repository.deserializeUserAnswers(savedRecord.userAnswersJson)
        assertEquals(0, deserializedAnswers[1])
        assertEquals(0, deserializedAnswers[2])

        // Update attempt with new score
        val newAnswers = mapOf(1 to 0, 2 to 1) // 1 correct, 1 wrong
        repository.updateSavedQuizAttempt(
            id = savedId,
            answers = newAnswers,
            score = 1,
            percentage = 50
        )

        val updated = repository.allSavedQuizzes.first().first { it.id == savedId }
        assertEquals(1, updated.lastScore)
        assertEquals(50, updated.lastPercentage)
    }

    @Test
    fun testDeleteChatSessionAndCascadingMessages() = runBlocking {
        val sessionId = repository.createChatSession(
            title = "Chemistry Equilibrium",
            subject = "Chemistry",
            firstQuestion = "Explain Le Chatelier's principle"
        )
        repository.addMessageToSession(sessionId, "USER", "Explain Le Chatelier's principle")
        repository.addMessageToSession(sessionId, "AI", "If a stress is applied to a dynamic equilibrium...")

        val initialSessions = repository.allChatSessions.first()
        assertTrue(initialSessions.any { it.id == sessionId })

        // Delete session
        repository.deleteChatSession(sessionId)

        val remainingSessions = repository.allChatSessions.first()
        assertTrue(remainingSessions.none { it.id == sessionId })

        val remainingMessages = repository.getMessagesForSession(sessionId).first()
        assertTrue(remainingMessages.isEmpty())
    }
}
