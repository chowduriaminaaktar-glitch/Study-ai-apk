package com.example

import com.example.util.AcademicSpeechFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun formatAcademicSpeech_mathPowers_areConverted() {
        val input = "solve for x in 2x squared plus 5x minus 3 equals 0"
        val formatted = AcademicSpeechFormatter.formatAcademicSpeech(input)
        assertTrue(formatted.contains("2x²"))
        assertTrue(formatted.contains("="))
    }

    @Test
    fun formatAcademicSpeech_integralsAndDerivatives_areFormatted() {
        val input = "find the integral of x cubed dx"
        val formatted = AcademicSpeechFormatter.formatAcademicSpeech(input)
        assertTrue(formatted.contains("∫"))
        assertTrue(formatted.contains("x³"))
    }

    @Test
    fun formatAcademicSpeech_chemistryReaction_isFormatted() {
        val input = "hydrogen reacts with oxygen yields water"
        val formatted = AcademicSpeechFormatter.formatAcademicSpeech(input)
        assertTrue(formatted.contains("+"))
        assertTrue(formatted.contains("→"))
    }
}
