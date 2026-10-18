package com.example.ui.library

import androidx.lifecycle.ViewModel
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.ReaderFontSize
import com.example.data.model.ReaderTheme
import com.example.data.model.StoryPage
import com.example.data.repository.LibraryCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(BookCategory.ALL)
    val selectedCategory: StateFlow<BookCategory> = _selectedCategory.asStateFlow()

    private val _displayedBooks = MutableStateFlow(LibraryCatalog.books)
    val displayedBooks: StateFlow<List<Book>> = _displayedBooks.asStateFlow()

    private val _activeBook = MutableStateFlow<Book?>(null)
    val activeBook: StateFlow<Book?> = _activeBook.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    private val _currentPageNumber = MutableStateFlow(1)
    val currentPageNumber: StateFlow<Int> = _currentPageNumber.asStateFlow()

    private val _isFullChapterMode = MutableStateFlow(false)
    val isFullChapterMode: StateFlow<Boolean> = _isFullChapterMode.asStateFlow()

    private val _readerTheme = MutableStateFlow(ReaderTheme.SEPIA)
    val readerTheme: StateFlow<ReaderTheme> = _readerTheme.asStateFlow()

    private val _readerFontSize = MutableStateFlow(ReaderFontSize.MEDIUM)
    val readerFontSize: StateFlow<ReaderFontSize> = _readerFontSize.asStateFlow()

    private val _isReaderOpen = MutableStateFlow(false)
    val isReaderOpen: StateFlow<Boolean> = _isReaderOpen.asStateFlow()

    fun onSearchChange(query: String) {
        _searchQuery.value = query
        updateFilter()
    }

    fun onCategorySelect(category: BookCategory) {
        _selectedCategory.value = category
        updateFilter()
    }

    private fun updateFilter() {
        val q = _searchQuery.value.trim().lowercase()
        val cat = _selectedCategory.value
        _displayedBooks.value = LibraryCatalog.books.filter { book ->
            val matchCat = cat == BookCategory.ALL || book.category == cat
            val matchSearch = q.isEmpty() ||
                    book.title.lowercase().contains(q) ||
                    book.author.lowercase().contains(q) ||
                    book.description.lowercase().contains(q) ||
                    book.category.label.lowercase().contains(q)
            matchCat && matchSearch
        }
    }

    fun openBook(book: Book, chapterIndex: Int = 0, pageNumber: Int = 1) {
        _activeBook.value = book
        val validChapterIndex = chapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
        _currentChapterIndex.value = validChapterIndex
        val targetPage = if (pageNumber > 1) {
            pageNumber.coerceIn(1, book.pageCount)
        } else {
            book.chapters.getOrNull(validChapterIndex)?.startPage ?: 1
        }
        _currentPageNumber.value = targetPage
        _isReaderOpen.value = true
    }

    fun closeReader() {
        _isReaderOpen.value = false
        _activeBook.value = null
    }

    fun selectChapter(index: Int) {
        val book = _activeBook.value ?: return
        val validIndex = index.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
        _currentChapterIndex.value = validIndex
        val ch = book.chapters.getOrNull(validIndex)
        if (ch != null) {
            _currentPageNumber.value = ch.startPage
        }
    }

    fun nextChapter() {
        val book = _activeBook.value ?: return
        if (_currentChapterIndex.value < book.chapters.size - 1) {
            selectChapter(_currentChapterIndex.value + 1)
        }
    }

    fun previousChapter() {
        if (_currentChapterIndex.value > 0) {
            selectChapter(_currentChapterIndex.value - 1)
        }
    }

    fun nextPage() {
        val book = _activeBook.value ?: return
        if (_currentPageNumber.value < book.pageCount) {
            val nextPageNum = _currentPageNumber.value + 1
            _currentPageNumber.value = nextPageNum
            syncChapterWithPage(book, nextPageNum)
        }
    }

    fun previousPage() {
        val book = _activeBook.value ?: return
        if (_currentPageNumber.value > 1) {
            val prevPageNum = _currentPageNumber.value - 1
            _currentPageNumber.value = prevPageNum
            syncChapterWithPage(book, prevPageNum)
        }
    }

    fun goToPage(page: Int) {
        val book = _activeBook.value ?: return
        val clamped = page.coerceIn(1, book.pageCount)
        _currentPageNumber.value = clamped
        syncChapterWithPage(book, clamped)
    }

    private fun syncChapterWithPage(book: Book, pageNum: Int) {
        val chIndex = book.chapters.indexOfFirst { pageNum in it.startPage..it.endPage }
        if (chIndex >= 0 && chIndex != _currentChapterIndex.value) {
            _currentChapterIndex.value = chIndex
        }
    }

    fun toggleReadingMode() {
        _isFullChapterMode.value = !_isFullChapterMode.value
    }

    fun getCurrentStoryPage(): StoryPage? {
        val book = _activeBook.value ?: return null
        val pageNum = _currentPageNumber.value
        return book.allPages.find { it.pageNumber == pageNum }
            ?: book.allPages.firstOrNull()
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    fun setReaderFontSize(size: ReaderFontSize) {
        _readerFontSize.value = size
    }
}
