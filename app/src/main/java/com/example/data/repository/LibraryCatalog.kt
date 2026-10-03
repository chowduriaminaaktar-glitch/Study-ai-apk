package com.example.data.repository

import androidx.compose.ui.graphics.Color
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.BookChapter

object LibraryCatalog {
    val books: List<Book> = listOf(
        Book(
            id = "principia",
            title = "Philosophiæ Naturalis Principia Mathematica",
            author = "Sir Isaac Newton",
            category = BookCategory.STEM,
            year = "1687",
            pageCount = 510,
            rating = 4.9f,
            description = "The foundational bedrock of classical mechanics, setting forth Newton's Laws of Motion and Universal Gravitation.",
            coverGradientStart = Color(0xFF1E3A8A),
            coverGradientEnd = Color(0xFF3B82F6),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Axioms, or the Laws of Motion",
                    readTimeMinutes = 9,
                    content = """
                        Law I: Every body continues in its state of rest, or of uniform motion in a right line, unless it is compelled to change that state by forces impressed upon it.
                        
                        Law II: The alteration of motion is ever proportional to the motive force impressed; and is made in the direction of the right line in which that force is impressed (F = dp/dt = ma).
                        
                        Law III: To every action there is always opposed an equal reaction: or the mutual actions of two bodies upon each other are always equal, and directed to contrary parts.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Inertia preserves velocity unless unbalanced forces act.",
                        "Force is the time rate of momentum change.",
                        "All physical interactions consist of mutual, equal, and opposite forces."
                    ),
                    studyQuestions = listOf(
                        "How does Newton's First Law relate to Galileo's concept of inertia?",
                        "What happens to acceleration when force is doubled and mass is tripled?"
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "Universal Gravitation and Orbital Mechanics",
                    readTimeMinutes = 11,
                    content = """
                        Gravity is an inverse-square force that binds planets in elliptical orbits around the sun.
                        F = G * (m1 * m2) / r^2
                        where G is the gravitational constant (6.674 × 10^-11 N m²/kg²).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Gravitational attraction falls off with the square of the distance.",
                        "Kepler's Laws of Planetary Motion are mathematical consequences of Newton's inverse-square gravity."
                    ),
                    studyQuestions = listOf(
                        "Derive Kepler's Third Law (T² ∝ r³) using Newton's law of gravity and centripetal force."
                    )
                )
            )
        ),
        Book(
            id = "frankenstein",
            title = "Frankenstein; or, The Modern Prometheus",
            author = "Mary Shelley",
            category = BookCategory.LITERATURE,
            year = "1818",
            pageCount = 280,
            rating = 4.8f,
            description = "The pioneering gothic science-fiction classic exploring human hubris, the limits of science, alienation, creation, and moral responsibility.",
            coverGradientStart = Color(0xFF064E3B),
            coverGradientEnd = Color(0xFF10B981),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "The Spark of Creation",
                    readTimeMinutes = 8,
                    content = """
                        It was on a dreary night of November that I beheld the accomplishment of my toils. With an anxiety that almost amounted to agony, I collected the instruments of life around me, that I might infuse a spark of being into the lifeless thing that lay at my feet...
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Victor Frankenstein's obsessive pursuit of scientific glory blinds him to the ethical consequences of his creation.",
                        "Immediate abandonment of the Creature highlights the tragic failure of parental and creator responsibility."
                    ),
                    studyQuestions = listOf(
                        "Why does Mary Shelley subtitle the novel 'The Modern Prometheus'?",
                        "How does Victor's reaction immediately upon the Creature awakening set the stage for tragedy?"
                    )
                )
            )
        ),
        Book(
            id = "short_history_world",
            title = "A Short History of the World",
            author = "H.G. Wells",
            category = BookCategory.HISTORY,
            year = "1922",
            pageCount = 410,
            rating = 4.7f,
            description = "A grand sweep of human history, from early river valley civilizations to modern societies.",
            coverGradientStart = Color(0xFF78350F),
            coverGradientEnd = Color(0xFFF59E0B),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "The First River Civilizations (Nile & Mesopotamia)",
                    readTimeMinutes = 8,
                    content = """
                        Civilization grew slowly along the alluvial river valleys where agriculture, water irrigation, and seasonal floods demanded coordinated human organization...
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Agricultural surplus is the prerequisite for division of labor and urbanization.",
                        "Writing originated primarily as a bureaucratic and accounting innovation before turning to literature."
                    ),
                    studyQuestions = listOf(
                        "Why were river floodplains uniquely suited for early human urbanization?"
                    )
                )
            )
        )
    )

    fun getById(id: String): Book? {
        return books.find { it.id == id }
    }

    fun filter(query: String, category: BookCategory): List<Book> {
        return books.filter { book ->
            val matchesCategory = category == BookCategory.ALL || book.category == category
            val matchesQuery = query.isBlank() ||
                book.title.contains(query, ignoreCase = true) ||
                book.author.contains(query, ignoreCase = true) ||
                book.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
