package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.SubjectBiology
import com.example.ui.theme.SubjectCS
import com.example.ui.theme.SubjectChemistry
import com.example.ui.theme.SubjectEconomics
import com.example.ui.theme.SubjectHistory
import com.example.ui.theme.SubjectLiterature
import com.example.ui.theme.SubjectMath
import com.example.ui.theme.SubjectPhysics

data class StudySubject(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val sampleQuestions: List<String>,
    val popularTopics: List<String>
)

object SubjectCatalog {
    val subjects = listOf(
        StudySubject(
            id = "math",
            name = "Mathematics",
            description = "Calculus, Algebra, Geometry, Statistics, Trigonometry",
            icon = Icons.Default.Calculate,
            color = SubjectMath,
            sampleQuestions = listOf(
                "Find the derivative of f(x) = x^3 * e^(2x)",
                "How do you evaluate the integral ∫ x * sin(x) dx by parts?",
                "Solve the quadratic system: 2x^2 + 3x - 5 = 0"
            ),
            popularTopics = listOf("Calculus", "Linear Algebra", "Probability & Stats", "Trigonometry", "Differential Equations")
        ),
        StudySubject(
            id = "physics",
            name = "Physics",
            description = "Mechanics, Thermodynamics, Electromagnetism, Quantum",
            icon = Icons.Default.Functions,
            color = SubjectPhysics,
            sampleQuestions = listOf(
                "A 5kg block slides down a 30° frictionless incline. What is its acceleration?",
                "Explain Lenz's Law and how induced current opposes flux change",
                "Calculate the escape velocity from Earth's surface"
            ),
            popularTopics = listOf("Newtonian Mechanics", "Electromagnetism", "Thermodynamics", "Wave Optics", "Special Relativity")
        ),
        StudySubject(
            id = "chemistry",
            name = "Chemistry",
            description = "Organic, Inorganic, Physical, Stoichiometry, Equilibrium",
            icon = Icons.Default.Science,
            color = SubjectChemistry,
            sampleQuestions = listOf(
                "Balance the redox reaction: MnO4⁻ + Fe²⁺ → Mn²⁺ + Fe³⁺ in acidic solution",
                "Explain the SN1 vs SN2 reaction mechanisms with stereochemistry",
                "Calculate the pH of a 0.15 M acetic acid solution (Ka = 1.8 × 10⁻⁵)"
            ),
            popularTopics = listOf("Organic Synthesis", "Chemical Equilibrium", "Thermodynamics", "Periodic Trends", "Electrochemistry")
        ),
        StudySubject(
            id = "biology",
            name = "Biology",
            description = "Genetics, Cell Biology, Physiology, Ecology, Evolution",
            icon = Icons.Default.Biotech,
            color = SubjectBiology,
            sampleQuestions = listOf(
                "How does CRISPR-Cas9 target and edit specific DNA sequences?",
                "Trace the steps of cellular respiration: Glycolysis to Oxidative Phosphorylation",
                "Predict phenotypic ratio for a dihybrid cross of heterozygous parents"
            ),
            popularTopics = listOf("Molecular Genetics", "Cellular Respiration", "Immune System", "Ecology & Ecosystems", "Neurobiology")
        ),
        StudySubject(
            id = "cs",
            name = "Computer Science",
            description = "Algorithms, Data Structures, OOP, Systems, AI",
            icon = Icons.Default.Code,
            color = SubjectCS,
            sampleQuestions = listOf(
                "Explain Dijkstra's shortest path algorithm with time complexity",
                "How does QuickSort work and why is its worst case O(n^2)?",
                "What is the difference between concurrency and parallelism?"
            ),
            popularTopics = listOf("Data Structures", "Dynamic Programming", "System Architecture", "Databases & SQL", "Operating Systems")
        ),
        StudySubject(
            id = "history",
            name = "History",
            description = "World History, Civilizations, Revolutions, Global Conflicts",
            icon = Icons.Default.HistoryEdu,
            color = SubjectHistory,
            sampleQuestions = listOf(
                "What were the primary socio-economic causes of the French Revolution?",
                "Analyze the impact of the Silk Road on cultural and technological exchange",
                "How did the Peace of Westphalia reshape national sovereignty?"
            ),
            popularTopics = listOf("Ancient Civilizations", "Industrial Revolution", "World War I & II", "Cold War Era", "Decolonization")
        ),
        StudySubject(
            id = "literature",
            name = "Literature",
            description = "Literary Analysis, Poetry, Themes, Rhetoric, World Classics",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            color = SubjectLiterature,
            sampleQuestions = listOf(
                "Analyze the symbolism of the green light in F. Scott Fitzgerald's The Great Gatsby",
                "How does Shakespeare use dramatic irony in Macbeth?",
                "Compare stream of consciousness in Woolf and Joyce"
            ),
            popularTopics = listOf("Literary Devices", "Character Analysis", "Poetic Meter", "Modernist Fiction", "Rhetorical Strategies")
        ),
        StudySubject(
            id = "economics",
            name = "Economics",
            description = "Microeconomics, Macroeconomics, Market Structures, Fiscal Policy",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = SubjectEconomics,
            sampleQuestions = listOf(
                "How does the central bank use open market operations to control inflation?",
                "Explain price elasticity of demand and its effect on total revenue",
                "What is comparative advantage and how does it drive international trade?"
            ),
            popularTopics = listOf("Supply & Demand", "Monetary Policy", "Game Theory", "Fiscal Multiplier", "Market Externalities")
        ),
        StudySubject(
            id = "general",
            name = "Any Subject / Custom",
            description = "Philosophy, Psychology, Law, Languages, Engineering & more",
            icon = Icons.Default.Psychology,
            color = Color(0xFF6366F1),
            sampleQuestions = listOf(
                "Explain Kant's Categorical Imperative versus Utilitarianism",
                "How do cognitive biases like Confirmation Bias affect critical thinking?",
                "What is the difference between civil law and common law systems?"
            ),
            popularTopics = listOf("Ethics & Logic", "Cognitive Psychology", "Philosophy of Mind", "Legal Systems", "Linguistics")
        )
    )

    fun getById(id: String): StudySubject {
        return subjects.find { it.id.equals(id, ignoreCase = true) } ?: subjects.last()
    }
}
