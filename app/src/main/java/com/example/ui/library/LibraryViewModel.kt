package com.example.ui.library

import androidx.lifecycle.ViewModel
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.ReaderFontSize
import com.example.data.model.ReaderTheme
import com.example.data.repository.LibraryCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(BookCategory.ALL)
    val selectedCategory: StateFlow<BookCategory> = _selectedCategory.asStateFlow()

    private val _displayedBooks = MutableStateFlow<List<Book>>(LibraryCatalog.books)
    val displayedBooks: StateFlow<List<Book>> = _displayedBooks.asStateFlow()

    // Book Reader State
    private val _activeBook = MutableStateFlow<Book?>(null)
    val activeBook: StateFlow<Book?> = _activeBook.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

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
        _displayedBooks.value = LibraryCatalog.filter(_searchQuery.value, _selectedCategory.value)
    }

    fun openBook(book: Book, chapterIndex: Int = 0) {
        _activeBook.value = book
        _currentChapterIndex.value = chapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
        _isReaderOpen.value = true
    }

    fun closeReader() {
        _isReaderOpen.value = false
    }

    fun selectChapter(index: Int) {
        val book = _activeBook.value ?: return
        if (index in book.chapters.indices) {
            _currentChapterIndex.value = index
        }
    }

    fun nextChapter() {
        val book = _activeBook.value ?: return
        val next = _currentChapterIndex.value + 1
        if (next < book.chapters.size) {
            _currentChapterIndex.value = next
        }
    }

    fun previousChapter() {
        val prev = _currentChapterIndex.value - 1
        if (prev >= 0) {
            _currentChapterIndex.value = prev
        }
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    fun setReaderFontSize(size: ReaderFontSize) {
        _readerFontSize.value = size
    }
}
