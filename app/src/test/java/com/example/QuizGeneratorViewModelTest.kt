package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.QuizDifficulty
import com.example.ui.quiz.QuizGeneratorViewModel
import com.example.ui.quiz.QuizUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuizGeneratorViewModelTest {

    private lateinit var viewModel: QuizGeneratorViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = QuizGeneratorViewModel(app)
    }

    @Test
    fun `initial ui state is Setup`() {
        assertEquals(QuizUiState.Setup, viewModel.uiState.value)
    }

    @Test
    fun `update topic updates state`() {
        viewModel.onTopicChange("Thermodynamics")
        assertEquals("Thermodynamics", viewModel.topicInput.value)
    }

    @Test
    fun `select suggested topic updates topic and subject`() {
        viewModel.selectSuggestedTopic("Cellular Respiration", "Biology")
        assertEquals("Cellular Respiration", viewModel.topicInput.value)
        assertEquals("Biology", viewModel.selectedSubject.value)
    }

    @Test
    fun `update difficulty and question count`() {
        viewModel.onDifficultySelect(QuizDifficulty.HARD)
        assertEquals(QuizDifficulty.HARD, viewModel.selectedDifficulty.value)

        viewModel.onQuestionCountSelect(10)
        assertEquals(10, viewModel.questionCount.value)
    }

    @Test
    fun `generate quiz with empty topic results in Error state`() {
        viewModel.onTopicChange("   ")
        viewModel.generateQuiz()
        val state = viewModel.uiState.value
        assertTrue(state is QuizUiState.Error)
    }
}
