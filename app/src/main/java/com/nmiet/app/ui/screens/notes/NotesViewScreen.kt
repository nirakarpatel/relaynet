package com.nmiet.app.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.NoteConceptPoint
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesViewScreen(
    viewModel: NotesGeneratorViewModel,
    topicId: String = "relaynet_class9_science_photosynthesis_v1",
    onBackClick: () -> Unit = {},
    onShareViaNearby: (GeneratedNote) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    LaunchedEffect(topicId) {
        if (uiState is NotesUiState.Idle) {
            viewModel.generateNotes(topicId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Revision Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (syncStatus.isOnline) "Cloud Synthesized • Gemini" else "Offline Room SQLite Grounded",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.generateNotes(topicId) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate Notes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is NotesUiState.Idle, is NotesUiState.Generating -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Text(
                                text = (state as? NotesUiState.Generating)?.progressMessage
                                    ?: "Synthesizing revision notes...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is NotesUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = state.message,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { viewModel.generateNotes(topicId) }) {
                                Text("Retry Generation")
                            }
                        }
                    }
                }

                is NotesUiState.Success -> {
                    val note = state.note

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))

                            // 1. Language Toggle: 3-Way Segmented Row (Odia, English, Bilingual)
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SegmentedButton(
                                    selected = languageMode == NotesLanguageMode.ODIA,
                                    onClick = { viewModel.setLanguageMode(NotesLanguageMode.ODIA) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                    icon = {}
                                ) {
                                    Text("ଓଡ଼ିଆ (Odia)", maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                SegmentedButton(
                                    selected = languageMode == NotesLanguageMode.ENGLISH,
                                    onClick = { viewModel.setLanguageMode(NotesLanguageMode.ENGLISH) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                    icon = {}
                                ) {
                                    Text("English", maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                SegmentedButton(
                                    selected = languageMode == NotesLanguageMode.BILINGUAL,
                                    onClick = { viewModel.setLanguageMode(NotesLanguageMode.BILINGUAL) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                    icon = {}
                                ) {
                                    Text("Bilingual", maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // 2. Note Header Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (note.isOfflineGenerated) SuccessGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (note.isOfflineGenerated) "💾 Room Offline Extracted" else "✨ Gemini Synthesized",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (note.isOfflineGenerated) SuccessGreen else PrimaryBlue
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.toggleBookmark(note) }) {
                                            Icon(
                                                imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark Note",
                                                tint = if (note.isBookmarked) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val mainTitle = when (languageMode) {
                                        NotesLanguageMode.ODIA -> note.titleOdia
                                        NotesLanguageMode.ENGLISH -> note.titleEnglish
                                        NotesLanguageMode.BILINGUAL -> "${note.titleEnglish}\n${note.titleOdia}"
                                    }

                                    Text(
                                        text = mainTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue,
                                        lineHeight = 24.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.topicName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 3. Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val summaryTitle = if (languageMode == NotesLanguageMode.ENGLISH) "Executive Summary" else "Executive Summary (ସାରାଂଶ)"
                                        Text(summaryTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    when (languageMode) {
                                        NotesLanguageMode.ODIA -> {
                                            Text(note.summaryOdia, fontSize = 13.sp, lineHeight = 20.sp)
                                        }
                                        NotesLanguageMode.ENGLISH -> {
                                            Text(note.summaryEnglish, fontSize = 13.sp, lineHeight = 20.sp)
                                        }
                                        NotesLanguageMode.BILINGUAL -> {
                                            Text(note.summaryOdia, fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(note.summaryEnglish, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Key Takeaways Card: Bullet points with highlight badges
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val takeawaysTitle = if (languageMode == NotesLanguageMode.ENGLISH) "Key Takeaways & Core Concepts" else "Key Takeaways & Core Concepts (ମୁଖ୍ୟ ତଥ୍ୟ)"
                                        Text(takeawaysTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    note.keyConcepts.forEachIndexed { index, concept ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                when (languageMode) {
                                                    NotesLanguageMode.ODIA -> {
                                                        Text(concept.pointOdia, fontSize = 13.sp, lineHeight = 19.sp)
                                                    }
                                                    NotesLanguageMode.ENGLISH -> {
                                                        Text(concept.pointEnglish, fontSize = 13.sp, lineHeight = 19.sp)
                                                    }
                                                    NotesLanguageMode.BILINGUAL -> {
                                                        Text(concept.pointOdia, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
                                                        Text(concept.pointEnglish, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Formula Callout Box: Monospace font with copy & share
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = PrimaryBlue)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val formulasTitle = if (languageMode == NotesLanguageMode.ENGLISH) "Formulas & Equations" else "Formulas & Equations (ରାସାୟନିକ ସମୀକରଣ)"
                                            Text(
                                                text = formulasTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    note.formulasEquations.forEach { equation ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.06f))
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = equation,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.copyFormulaToClipboard(context, equation) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy formula", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Exam Caution Alert: Amber card highlighting exam pitfalls
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val pitfallTitle = if (languageMode == NotesLanguageMode.ENGLISH) "Exam Pitfall & Caution Traps" else "Exam Pitfall & Caution Traps (ପରୀକ୍ଷା ସତର୍କତା)"
                                        Text(
                                            text = pitfallTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = WarningAmber
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    note.examTips.forEach { tip ->
                                        Text(
                                            text = tip,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 7. Action Bar: Save to Library, Export Plain Text, Share via Nearby Bluetooth
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.exportPlainText(context, note) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export Text", fontSize = 11.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = { onShareViaNearby(note) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Mesh", fontSize = 11.sp, maxLines = 1)
                                }
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
            }
        }
    }
}
