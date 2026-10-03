package com.example.ui.library

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.ReaderFontSize
import com.example.data.model.ReaderTheme

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel(),
    onDiscussWithStudyAiLive: (bookTitle: String, chapterTitle: String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {}
) {
    val isReaderOpen by viewModel.isReaderOpen.collectAsState()
    val activeBook by viewModel.activeBook.collectAsState()

    if (isReaderOpen && activeBook != null) {
        BackHandler { viewModel.closeReader() }
        BookReaderView(
            book = activeBook!!,
            viewModel = viewModel,
            onClose = { viewModel.closeReader() },
            onDiscussWithStudyAiLive = onDiscussWithStudyAiLive,
            modifier = modifier
        )
    } else {
        LibraryCatalogView(
            viewModel = viewModel,
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
    val displayedBooks by viewModel.displayedBooks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchChange(it) },
            placeholder = { Text("Search academic books, authors, topics...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("input_library_search"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Categories Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BookCategory.entries.toTypedArray()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { viewModel.onCategorySelect(cat) },
                    label = { Text("${cat.iconLabel} ${cat.label}", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Books List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curated Textbooks & Guides (${displayedBooks.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(displayedBooks, key = { it.id }) { book ->
                BookItemCard(
                    book = book,
                    onClick = { viewModel.openBook(book) }
                )
            }
        }
    }
}

@Composable
fun BookItemCard(
    book: Book,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover gradient badge
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 88.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(book.coverGradientStart, book.coverGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.category.iconLabel,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${book.rating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${book.chapters.size} Chapters • ${book.pageCount} Pages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    onDiscussWithStudyAiLive: (bookTitle: String, chapterTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val readerFontSize by viewModel.readerFontSize.collectAsState()
    val chapter = book.chapters.getOrNull(currentChapterIndex) ?: book.chapters.first()

    var showAppearanceControls by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(readerTheme.bgColor)
    ) {
        // Reader Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = readerTheme.cardColor,
            tonalElevation = 2.dp
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
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = readerTheme.textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = readerTheme.textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Chapter ${chapter.number}: ${chapter.title}",
                            fontSize = 12.sp,
                            color = readerTheme.textColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Discuss with Study AI Live button
                    IconButton(
                        onClick = { onDiscussWithStudyAiLive(book.title, chapter.title) },
                        modifier = Modifier.testTag("btn_discuss_live")
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Discuss with Study AI Live",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Appearance toggle
                    IconButton(onClick = { showAppearanceControls = !showAppearanceControls }) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = "Adjust Font",
                            tint = readerTheme.textColor
                        )
                    }
                }
            }
        }

        // Appearance controls tray
        if (showAppearanceControls) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = readerTheme.cardColor,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Reader Themes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = readerTheme.textColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReaderTheme.entries.forEach { th ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = th.bgColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (readerTheme == th) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setReaderTheme(th) }
                            ) {
                                Text(
                                    text = th.label,
                                    fontSize = 11.sp,
                                    color = th.textColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Font Size",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = readerTheme.textColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReaderFontSize.entries.forEach { fs ->
                            FilterChip(
                                selected = readerFontSize == fs,
                                onClick = { viewModel.setReaderFontSize(fs) },
                                label = { Text(fs.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Reader Content Scroll
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Live Tutor Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = readerTheme.cardColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDiscussWithStudyAiLive(book.title, chapter.title) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Talk live about this chapter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = readerTheme.textColor
                        )
                        Text(
                            text = "Study AI Live can explain key concepts aloud in real-time.",
                            fontSize = 11.sp,
                            color = readerTheme.textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Chapter ${chapter.number}: ${chapter.title}",
                fontWeight = FontWeight.Bold,
                fontSize = (readerFontSize.spSize + 4).sp,
                color = readerTheme.textColor
            )
            Text(
                text = "${chapter.readTimeMinutes} min read",
                fontSize = 12.sp,
                color = readerTheme.textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = chapter.content,
                fontSize = readerFontSize.spSize.sp,
                lineHeight = readerFontSize.lineHeightSp.sp,
                color = readerTheme.textColor
            )

            if (chapter.keyTakeaways.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = readerTheme.cardColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Key Academic Takeaways",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = readerTheme.textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        chapter.keyTakeaways.forEach { takeaway ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(takeaway, fontSize = 13.sp, color = readerTheme.textColor)
                            }
                        }
                    }
                }
            }
        }

        // Chapter navigation bottom bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = readerTheme.cardColor,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.previousChapter() },
                    enabled = currentChapterIndex > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev Chapter")
                }

                Text(
                    text = "${currentChapterIndex + 1} of ${book.chapters.size}",
                    fontSize = 12.sp,
                    color = readerTheme.textColor
                )

                Button(
                    onClick = { viewModel.nextChapter() },
                    enabled = currentChapterIndex < book.chapters.size - 1,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Next Chapter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
