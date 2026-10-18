package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class BookCategory(val label: String, val iconLabel: String) {
    ALL("All Stories", "📚"),
    FAIRY_TALE("Fairy Tales & Fables", "🧚"),
    COMEDY("Comedy & Humor", "🎭"),
    INSPIRING("Inspiring & Morals", "✨"),
    HORROR("Spooky & Horror", "👻"),
    THRILLER("Thriller & Suspense", "⚡"),
    ADVENTURE("Adventure & Quest", "🗺️"),
    MYSTERY("Mystery & Detective", "🔍"),
    HISTORICAL("Historical Fiction", "🏛️"),
    SCI_FI("Sci-Fi & Space", "🚀"),
    FANTASY("Fantasy & Magic", "⚔️")
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

data class StoryPage(
    val pageNumber: Int,
    val chapterNumber: Int,
    val chapterTitle: String,
    val content: String
)

data class BookChapter(
    val number: Int,
    val title: String,
    val readTimeMinutes: Int,
    val startPage: Int = 1,
    val endPage: Int = 10,
    val content: String,
    val pages: List<StoryPage> = emptyList(),
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
) {
    /**
     * All pages flattened across all chapters in numerical order.
     */
    val allPages: List<StoryPage> by lazy {
        val list = mutableListOf<StoryPage>()
        for (chapter in chapters) {
            if (chapter.pages.isNotEmpty()) {
                list.addAll(chapter.pages)
            } else {
                list.add(
                    StoryPage(
                        pageNumber = chapter.startPage,
                        chapterNumber = chapter.number,
                        chapterTitle = chapter.title,
                        content = chapter.content
                    )
                )
            }
        }
        list.sortedBy { it.pageNumber }
    }
}
