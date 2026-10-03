package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class BookCategory(val label: String, val iconLabel: String) {
    ALL("All Books", "📚"),
    STEM("Science & Math", "🔬"),
    COMPUTER_SCIENCE("CS & Coding", "💻"),
    PHILOSOPHY("Philosophy", "🏛️"),
    LITERATURE("Literature", "📖"),
    HISTORY("World History", "⏳")
}

enum class ReaderTheme(
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    val cardColor: Color
) {
    LIGHT("Paper", Color(0xFFFAFAFA), Color(0xFF1E293B), Color(0xFFF1F5F9)),
    SEPIA("Sepia", Color(0xFFFBF0D9), Color(0xFF433422), Color(0xFFF7E6C0)),
    SLATE("Dark Slate", Color(0xFF0F172A), Color(0xFFE2E8F0), Color(0xFF1E293B)),
    OLED("OLED Black", Color(0xFF000000), Color(0xFFF1F5F9), Color(0xFF121212))
}

enum class ReaderFontSize(val label: String, val spSize: Int, val lineHeightSp: Int) {
    SMALL("Small", 14, 22),
    MEDIUM("Default", 16, 26),
    LARGE("Large", 19, 30),
    EXTRA_LARGE("XL", 22, 34)
}

data class BookChapter(
    val number: Int,
    val title: String,
    val readTimeMinutes: Int,
    val content: String,
    val keyTakeaways: List<String> = emptyList(),
    val studyQuestions: List<String> = emptyList()
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: BookCategory,
    val year: String,
    val pageCount: Int,
    val rating: Float,
    val description: String,
    val chapters: List<BookChapter>,
    val coverGradientStart: Color = Color(0xFF4F46E5),
    val coverGradientEnd: Color = Color(0xFF06B6D4)
)
