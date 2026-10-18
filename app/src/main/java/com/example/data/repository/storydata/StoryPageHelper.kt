package com.example.data.repository.storydata

import com.example.data.model.BookChapter
import com.example.data.model.StoryPage

object StoryPageHelper {
    /**
     * Builds a BookChapter with dedicated StoryPages for every page in [startPage..endPage].
     */
    fun createChapter(
        number: Int,
        title: String,
        readTimeMinutes: Int,
        startPage: Int,
        endPage: Int,
        pagePassages: List<String>,
        keyTakeaways: List<String> = emptyList(),
        studyQuestions: List<String> = emptyList()
    ): BookChapter {
        val totalPages = (endPage - startPage + 1).coerceAtLeast(1)
        val pages = mutableListOf<StoryPage>()

        for (i in 0 until totalPages) {
            val pageNum = startPage + i
            val content = if (i < pagePassages.size) {
                pagePassages[i].trimIndent()
            } else {
                val cycleIndex = i % pagePassages.size
                val base = pagePassages[cycleIndex].trimIndent()
                "Chapter $number: $title • Part ${i + 1}\n\n$base"
            }
            pages.add(
                StoryPage(
                    pageNumber = pageNum,
                    chapterNumber = number,
                    chapterTitle = title,
                    content = content
                )
            )
        }

        val fullContent = pages.joinToString("\n\n") { "Page ${it.pageNumber}\n\n${it.content}" }

        return BookChapter(
            number = number,
            title = title,
            readTimeMinutes = readTimeMinutes,
            startPage = startPage,
            endPage = endPage,
            content = fullContent,
            pages = pages,
            keyTakeaways = keyTakeaways,
            studyQuestions = studyQuestions
        )
    }
}
