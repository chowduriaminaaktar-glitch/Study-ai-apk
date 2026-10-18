package com.example.ui.library

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.ReaderFontSize
import com.example.data.model.ReaderTheme
import java.util.Locale

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
            placeholder = { Text("Search 10 story books, genres, authors...") },
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
                        text = "Curated Story Books (${displayedBooks.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "10 Distinct Genres",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
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
                    .size(width = 72.dp, height = 100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(book.coverGradientStart, book.coverGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(
                        text = book.category.iconLabel,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${book.pageCount}p",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = book.category.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "• ${book.year}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderView(
    book: Book,
    viewModel: LibraryViewModel,
    onClose: () -> Unit,
    onDiscussWithStudyAiLive: (bookTitle: String, chapterTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val currentPageNumber by viewModel.currentPageNumber.collectAsState()
    val isFullChapterMode by viewModel.isFullChapterMode.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val readerFontSize by viewModel.readerFontSize.collectAsState()

    val chapter = book.chapters.getOrNull(currentChapterIndex) ?: book.chapters.first()
    val currentStoryPage = viewModel.getCurrentStoryPage()

    var showAppearanceControls by remember { mutableStateOf(false) }
    var showTableOfContents by remember { mutableStateOf(false) }
    var showJumpPageDialog by remember { mutableStateOf(false) }

    // Text to Speech
    val context = LocalContext.current
    var isTtsSpeaking by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize TTS engine
            }
        }
        tts.language = Locale.US
        ttsInstance = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val speakCurrentContent: () -> Unit = {
        val tts = ttsInstance
        if (tts != null) {
            if (isTtsSpeaking) {
                tts.stop()
                isTtsSpeaking = false
            } else {
                val textToSpeak = if (isFullChapterMode) {
                    "${chapter.title}. ${chapter.content}"
                } else {
                    currentStoryPage?.content ?: chapter.content
                }
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "story_reader_tts")
                isTtsSpeaking = true
            }
        }
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = readerTheme.textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = readerTheme.textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isFullChapterMode) {
                                "Ch. ${chapter.number}: ${chapter.title}"
                            } else {
                                "Ch. ${chapter.number} • Page $currentPageNumber of ${book.pageCount}"
                            },
                            fontSize = 11.sp,
                            color = readerTheme.textColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // TTS Listen Aloud button
                    IconButton(
                        onClick = speakCurrentContent,
                        modifier = Modifier.testTag("btn_read_aloud")
                    ) {
                        Icon(
                            imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isTtsSpeaking) "Stop reading" else "Read aloud",
                            tint = if (isTtsSpeaking) MaterialTheme.colorScheme.error else readerTheme.textColor
                        )
                    }

                    // Table of Contents
                    IconButton(
                        onClick = { showTableOfContents = true },
                        modifier = Modifier.testTag("btn_table_of_contents")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Table of Contents",
                            tint = readerTheme.textColor
                        )
                    }

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
                            contentDescription = "Adjust Font & Appearance",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reading Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = readerTheme.textColor
                        )
                        OutlinedButton(
                            onClick = { viewModel.toggleReadingMode() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isFullChapterMode) "Switch to Page Flip Mode" else "Switch to Continuous Scroll",
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                                border = BorderStroke(
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
                                    textAlign = TextAlign.Center,
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
                            text = "Discuss this chapter with Study AI Live",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = readerTheme.textColor
                        )
                        Text(
                            text = "Ask questions, analyze characters, or have Study AI Live narrate aloud.",
                            fontSize = 11.sp,
                            color = readerTheme.textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Page or Chapter Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chapter ${chapter.number}: ${chapter.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = (readerFontSize.spSize + 4).sp,
                        color = readerTheme.textColor
                    )
                    Text(
                        text = "Pages ${chapter.startPage} - ${chapter.endPage} • ${chapter.readTimeMinutes} min read",
                        fontSize = 12.sp,
                        color = readerTheme.textColor.copy(alpha = 0.6f)
                    )
                }

                if (!isFullChapterMode) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showJumpPageDialog = true }
                    ) {
                        Text(
                            text = "Page $currentPageNumber / ${book.pageCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Text Content
            val displayText = if (isFullChapterMode) {
                chapter.content
            } else {
                currentStoryPage?.content ?: chapter.content
            }

            Text(
                text = displayText,
                fontSize = readerFontSize.spSize.sp,
                lineHeight = readerFontSize.lineHeightSp.sp,
                color = readerTheme.textColor
            )

            // Chapter takeaways & questions if at end of chapter or in full mode
            if (chapter.keyTakeaways.isNotEmpty() && (isFullChapterMode || currentPageNumber == chapter.endPage)) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = readerTheme.cardColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Key Story Takeaways & Moral Themes",
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

                        if (chapter.studyQuestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Story Reflection Questions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = readerTheme.textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            chapter.studyQuestions.forEach { question ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("? ", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                    Text(question, fontSize = 12.sp, color = readerTheme.textColor)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Navigation Bar
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
                if (!isFullChapterMode) {
                    // Page by Page Controls
                    Button(
                        onClick = { viewModel.previousPage() },
                        enabled = currentPageNumber > 1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Page")
                    }

                    TextButton(onClick = { showJumpPageDialog = true }) {
                        Text(
                            text = "Page $currentPageNumber of ${book.pageCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = readerTheme.textColor
                        )
                    }

                    Button(
                        onClick = { viewModel.nextPage() },
                        enabled = currentPageNumber < book.pageCount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Next Page")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Full Chapter Controls
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

    // Table of Contents Modal Bottom Sheet
    if (showTableOfContents) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showTableOfContents = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Table of Contents",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${book.title} • ${book.pageCount} Pages Total",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(book.chapters) { index, ch ->
                        val isCurrent = index == currentChapterIndex
                        Card(
                            onClick = {
                                viewModel.selectChapter(index)
                                showTableOfContents = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Chapter ${ch.number}: ${ch.title}",
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Pages ${ch.startPage} - ${ch.endPage} • ${ch.readTimeMinutes} min",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCurrent) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpPageDialog) {
        var sliderValue by remember { mutableFloatStateOf(currentPageNumber.toFloat()) }
        AlertDialog(
            onDismissRequest = { showJumpPageDialog = false },
            title = { Text("Jump to Page") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Page ${sliderValue.toInt()} of ${book.pageCount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 1f..book.pageCount.toFloat(),
                        steps = (book.pageCount - 2).coerceAtLeast(0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val selectedChapter = book.chapters.find { sliderValue.toInt() in it.startPage..it.endPage }
                    if (selectedChapter != null) {
                        Text(
                            text = "Chapter ${selectedChapter.number}: ${selectedChapter.title}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.goToPage(sliderValue.toInt())
                        showJumpPageDialog = false
                    }
                ) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpPageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
