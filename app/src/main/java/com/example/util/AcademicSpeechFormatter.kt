package com.example.util

object AcademicSpeechFormatter {

    /**
     * Formats spoken phrases commonly used when dictating academic formulas and terminology
     * into clean mathematical, chemical, and scientific symbols.
     */
    fun formatAcademicSpeech(spokenText: String): String {
        var text = spokenText

        // Math power expressions: "x squared" or "x to the power of 2" -> "x²"
        text = text.replace(Regex("(?i)\\s*\\b(?:to the power of 2|squared)\\b"), "²")
            .replace(Regex("(?i)\\s*\\b(?:to the power of 3|cubed)\\b"), "³")
            .replace(Regex("(?i)\\s*\\bto the power of ([0-9]+)\\b"), "^$1")
            .replace(Regex("(?i)\\s*\\bto the ([0-9]+)(?:st|nd|rd|th)? power\\b"), "^$1")

        // Roots and radicals
        text = text.replace(Regex("(?i)\\b(?:square root of|square root)\\b\\s*"), "√")
            .replace(Regex("(?i)\\b(?:cube root of|cube root)\\b\\s*"), "∛")

        // Calculus
        text = text.replace(Regex("(?i)\\b(?:definite integral of|integral of)\\b\\s*"), "∫ ")
            .replace(Regex("(?i)\\bderivative of\\b\\s*"), "d/dx(")
            .replace(Regex("(?i)\\bpartial derivative of\\b\\s*"), "∂/∂x(")

        // Operators
        text = text.replace(Regex("(?i)\\bplus or minus\\b"), "±")
            .replace(Regex("(?i)\\bdivided by\\b"), " / ")
            .replace(Regex("(?i)\\bmultiplied by\\b"), " × ")
            .replace(Regex("(?i)\\btimes\\b"), " × ")
            .replace(Regex("(?i)\\bgreater than or equal to\\b"), " ≥ ")
            .replace(Regex("(?i)\\bless than or equal to\\b"), " ≤ ")
            .replace(Regex("(?i)\\bnot equal to\\b"), " ≠ ")
            .replace(Regex("(?i)\\bequals\\b"), " = ")
            .replace(Regex("(?i)\\bequal to\\b"), " = ")
            .replace(Regex("(?i)\\bapproaches\\b"), " → ")

        // Constants and symbols
        text = text.replace(Regex("(?i)\\bpi\\b"), "π")
            .replace(Regex("(?i)\\btheta\\b"), "θ")
            .replace(Regex("(?i)\\balpha\\b"), "α")
            .replace(Regex("(?i)\\bbeta\\b"), "β")
            .replace(Regex("(?i)\\bdelta\\b"), "Δ")
            .replace(Regex("(?i)\\blambda\\b"), "λ")
            .replace(Regex("(?i)\\binfinity\\b"), "∞")

        // Chemistry reactions
        text = text.replace(Regex("(?i)\\breacts with\\b"), " + ")
            .replace(Regex("(?i)\\byields\\b"), " → ")
            .replace(Regex("(?i)\\bgives\\b"), " → ")
            .replace(Regex("(?i)\\bproduces\\b"), " → ")

        // Spacing cleanup
        text = text.replace(Regex("\\s+"), " ").trim()

        return text
    }
}
