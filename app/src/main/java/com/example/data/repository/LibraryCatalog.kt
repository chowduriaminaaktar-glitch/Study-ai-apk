package com.example.data.repository

import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.repository.storydata.StoryBooksPart1
import com.example.data.repository.storydata.StoryBooksPart2
import com.example.data.repository.storydata.StoryBooksPart3

object LibraryCatalog {
    /**
     * Curated 10 types of story books featuring genuine, readable narrative prose,
     * comprehensive page ranges (50-60, 70-80, 100-120 pages), and distinct literary genres.
     */
    val books: List<Book> = listOf(
        StoryBooksPart1.whisperingWoods,       // 1. Fairy Tale & Fable (55 Pages)
        StoryBooksPart1.professorPumpernickel, // 2. Comedy & Humor (60 Pages)
        StoryBooksPart1.weaverOfLanterns,      // 3. Inspiring & Morals (50 Pages)
        StoryBooksPart2.ravenwoodManor,        // 4. Spooky & Horror (65 Pages)
        StoryBooksPart2.velocityZero,          // 5. Thriller & Suspense (70 Pages)
        StoryBooksPart2.sunIsleTemple,         // 6. Adventure & Quest (75 Pages)
        StoryBooksPart2.emeraldDragonfly,      // 7. Mystery & Detective (80 Pages)
        StoryBooksPart3.spartanVanguard,       // 8. Historical Fiction (95 Pages)
        StoryBooksPart3.keplerOdyssey,         // 9. Sci-Fi & Space (110 Pages)
        StoryBooksPart3.dragonCrown            // 10. Fantasy & Magic (120 Pages)
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
                book.description.contains(query, ignoreCase = true) ||
                book.category.label.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
