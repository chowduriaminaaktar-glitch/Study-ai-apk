package com.example.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.ReaderFontSize
import com.example.data.model.ReaderTheme
import com.example.ui.theme.AmberTertiary

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel = viewModel(),
    onAskAiAboutBook: ((String, String) -> Unit)? = null,
    onNavigateBack: () -> Unit = {}
) {
    val isReaderOpen by libraryViewModel.isReaderOpen.collectAsState()
    val activeBook by libraryViewModel.activeBook.collectAsState()

    BackHandler {
        if (isReaderOpen) {
            libraryViewModel.closeReader()
        } else {
            onNavigateBack()
        }
    }

    if (isReaderOpen && activeBook != null) {
        BookReaderView(
            book = activeBook!!,
            viewModel = libraryViewModel,
            onClose = { libraryViewModel.closeReader() },
            onAskAi = onAskAiAboutBook,
            modifier = modifier
        )
    } else {
        LibraryCatalogView(
            viewModel = libraryViewModel,
            modifier = modifier
        )
    }
}

@Composable
fun LibraryCatalogView(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val books by viewModel.displayedBooks.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_catalog_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Universal Digital Library",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Explore complete books, classic treatises, and STEM texts with an in-app reader.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                placeholder = { Text("Search by book title, author, or keyword...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BookCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelect(cat) },
                        label = { Text("${cat.iconLabel} ${cat.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("library_category_${cat.name}")
                    )
                }
            }
        }

        // Book count
        item {
            Text(
                text = "${books.size} Books Available",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Books List
        items(books) { book ->
            BookItemCard(
                book = book,
                onReadClick = { viewModel.openBook(book) }
            )
        }
    }
}

@Composable
fun BookItemCard(
    book: Book,
    onReadClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReadClick)
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Book Spine / Cover preview
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(book.coverGradientStart, book.coverGradientEnd)
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.title,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = book.category.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "by ${book.author} (${book.year})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${book.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(text = "•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${book.pageCount} pages", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${book.chapters.size} ch.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = book.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onReadClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("read_button_${book.id}")
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Read Book", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun BookReaderView(
    book: Book,
    viewModel: LibraryViewModel,
    onClose: () -> Unit,
    onAskAi: ((String, String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val currentChapterIdx by viewModel.currentChapterIndex.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val readerFontSize by viewModel.readerFontSize.collectAsState()

    val currentChapter = book.chapters.getOrNull(currentChapterIdx) ?: book.chapters.first()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showChapterMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(readerTheme.bgColor)
            .testTag("book_reader_view")
    ) {
        // Reader Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = readerTheme.cardColor,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Reader",
                            tint = readerTheme.textColor
                        )
                    }

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = readerTheme.textColor
                        )
                        Text(
                            text = "Chapter ${currentChapter.number}: ${currentChapter.title}",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = readerTheme.textColor.copy(alpha = 0.75f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chapter selector
                    Box {
                        IconButton(onClick = { showChapterMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Chapters", tint = readerTheme.textColor)
                        }
                        DropdownMenu(
                            expanded = showChapterMenu,
                            onDismissRequest = { showChapterMenu = false }
                        ) {
                            book.chapters.forEachIndexed { idx, ch ->
                                DropdownMenuItem(
                                    text = { Text("Ch. ${ch.number}: ${ch.title}") },
                                    onClick = {
                                        viewModel.selectChapter(idx)
                                        showChapterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Font size selector
                    Box {
                        IconButton(onClick = { showFontSizeMenu = true }) {
                            Icon(Icons.Default.FormatSize, contentDescription = "Font Size", tint = readerTheme.textColor)
                        }
                        DropdownMenu(
                            expanded = showFontSizeMenu,
                            onDismissRequest = { showFontSizeMenu = false }
                        ) {
                            ReaderFontSize.entries.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text(size.label) },
                                    onClick = {
                                        viewModel.setReaderFontSize(size)
                                        showFontSizeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Theme selector
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Theme", tint = readerTheme.textColor)
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            ReaderTheme.entries.forEach { th ->
                                DropdownMenuItem(
                                    text = { Text(th.label) },
                                    onClick = {
                                        viewModel.setReaderTheme(th)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reading Content Body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Chapter Title Header
            item {
                Column {
                    Text(
                        text = "CHAPTER ${currentChapter.number}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = readerTheme.textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentChapter.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = readerTheme.textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimated read time: ${currentChapter.readTimeMinutes} mins",
                        fontSize = 12.sp,
                        color = readerTheme.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Chapter Text Content
            item {
                SelectionContainer {
                    Text(
                        text = currentChapter.content,
                        fontSize = readerFontSize.spSize.sp,
                        lineHeight = readerFontSize.lineHeightSp.sp,
                        color = readerTheme.textColor,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            // Key Takeaways Card
            if (currentChapter.keyTakeaways.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = readerTheme.cardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Key Takeaways & Core Concepts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = readerTheme.textColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            currentChapter.keyTakeaways.forEach { takeaway ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("• ", color = readerTheme.textColor, fontWeight = FontWeight.Bold)
                                    Text(takeaway, color = readerTheme.textColor.copy(alpha = 0.9f), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Ask AI Button
            if (onAskAi != null) {
                item {
                    Button(
                        onClick = {
                            onAskAi(book.title, currentChapter.title)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Discuss Chapter with AI Tutor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Chapter Navigation
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = readerTheme.cardColor,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentChapterIdx > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousChapter() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = "Chapter ${currentChapterIdx + 1} of ${book.chapters.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = readerTheme.textColor.copy(alpha = 0.8f)
                )

                if (currentChapterIdx + 1 < book.chapters.size) {
                    Button(
                        onClick = { viewModel.nextChapter() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }
}
