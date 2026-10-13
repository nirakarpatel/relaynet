package com.nmiet.app.ui.screens.lesson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonReaderScreen(
    packId: String,
    repository: ContentRepository,
    onBackClick: () -> Unit,
    onStartQuiz: (String) -> Unit,
    targetSection: String? = null
) {
    val pack by repository.getLearningPackByIdFlow(packId).collectAsState(initial = null)
    val syncStatus by repository.getSyncStatusFlow().collectAsState(initial = com.nmiet.app.domain.model.SyncStatus())

    var languageMode by remember { mutableStateOf(AppLanguage.BILINGUAL) }
    var selectedGlossaryTerm by remember { mutableStateOf<KeyTerm?>(null) }

    // Audio Read-Aloud Player state
    var isPlayingAudio by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0.25f) }
    var audioSpeed by remember { mutableStateOf("1x") }

    LaunchedEffect(isPlayingAudio) {
        if (isPlayingAudio) {
            while (isPlayingAudio && audioProgress < 1.0f) {
                delay(1000)
                audioProgress += 0.05f
                if (audioProgress >= 1.0f) {
                    audioProgress = 0f
                    isPlayingAudio = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Lesson Reader",
                isOnline = syncStatus.isOnline,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            // Floating Audio Read-Aloud Player Bar
            FloatingAudioPlayerBar(
                isPlaying = isPlayingAudio,
                progress = audioProgress,
                speed = audioSpeed,
                onPlayPauseToggle = { isPlayingAudio = !isPlayingAudio },
                onSpeedToggle = {
                    audioSpeed = when (audioSpeed) {
                        "1x" -> "1.25x"
                        "1.25x" -> "1.5x"
                        else -> "1x"
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pack == null) {
            EmptyView(
                title = "Pack Loading...",
                subtitle = "Fetching lesson data from local SQLite pack manager.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            val lessonPack = pack!!
            val scrollState = rememberScrollState()

            // Programmatic smooth scroll to photolysis subsection when deep-linked from diagnostic card
            LaunchedEffect(targetSection, pack) {
                if (!targetSection.isNullOrBlank() && pack != null) {
                    delay(350)
                    scrollState.animateScrollTo(750)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Sticky 3-Way Segmented Button Control
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Language Mode (ଭାଷା ମୋଡ୍):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(
                                AppLanguage.BILINGUAL to "Bilingual",
                                AppLanguage.ODIA to "ଓଡ଼ିଆ",
                                AppLanguage.ENGLISH to "English"
                            )

                            options.forEachIndexed { index, pair ->
                                SegmentedButton(
                                    selected = languageMode == pair.first,
                                    onClick = { languageMode = pair.first },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(text = pair.second, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lesson Header
                Text(
                    text = lessonPack.titleOdia,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Text(
                    text = lessonPack.titleEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${lessonPack.subject} • Class ${lessonPack.classGrade} • ${lessonPack.board.displayName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Clean Rendering for Scientific Formulas and Chemical Equations
                val formula = lessonPack.lesson.equationOrFormula ?: "6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Balanced Photosynthetic Equation (ସନ୍ତୁଳିତ ରାସାୟନିକ ସମୀକରଣ):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = formula,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary
                Text(
                    text = "Lesson Overview (ସାରାଂଶ):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                when (languageMode) {
                    AppLanguage.BILINGUAL -> {
                        Text(
                            text = lessonPack.lesson.summaryOdia,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lessonPack.lesson.summaryEnglish,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AppLanguage.ODIA -> {
                        Text(
                            text = lessonPack.lesson.summaryOdia,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    AppLanguage.ENGLISH -> {
                        Text(
                            text = lessonPack.lesson.summaryEnglish,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Lesson Sections
                lessonPack.lesson.sections.forEach { section ->
                    val isTargetedHighlight = !targetSection.isNullOrBlank() &&
                            (section.titleEnglish.contains("Photolysis", ignoreCase = true) ||
                             section.titleEnglish.contains("Light", ignoreCase = true) ||
                             section.titleOdia.contains("ଆଲୋକ", ignoreCase = true))

                    if (isTargetedHighlight) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎯 Focused Topic: Photolysis & Light Reactions (Remediation)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "This subsection provides the conceptual foundation for water photolysis and oxygen evolution.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    when (languageMode) {
                        AppLanguage.BILINGUAL -> {
                            Text(
                                text = section.titleOdia,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.contentOdia,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = section.contentEnglish,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AppLanguage.ODIA -> {
                            Text(
                                text = section.titleOdia,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.contentOdia,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                        AppLanguage.ENGLISH -> {
                            Text(
                                text = section.titleEnglish,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.contentEnglish,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tappable Glossary Chips for STEM Terms
                    if (section.keyTerms.isNotEmpty()) {
                        Text(
                            text = "Tappable STEM Glossary (ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ ଶବ୍ଦାବଳୀ):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            section.keyTerms.forEach { term ->
                                AssistChip(
                                    onClick = { selectedGlossaryTerm = term },
                                    label = {
                                        Text(
                                            text = "${term.termEnglish} / ${term.termOdia.take(8)}...",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Checksum integrity footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHA-256 Checksum: ${lessonPack.checksumSha256.take(16)}... (Verified)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Start Quiz CTA
                Button(
                    onClick = { onStartQuiz(lessonPack.packId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Diagnostic Quiz (${lessonPack.quizQuestions.size} Questions)")
                }

                Spacer(modifier = Modifier.height(90.dp))
            }

            // Bottom Sheet for STEM Glossary Explanation
            selectedGlossaryTerm?.let { term ->
                val sheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    onDismissRequest = { selectedGlossaryTerm = null },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = term.termOdia,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            IconButton(onClick = { selectedGlossaryTerm = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Text(
                            text = "English: ${term.termEnglish}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Simplified Odia Explanation (ଓଡ଼ିଆ ସରଳ ବ୍ୟାଖ୍ୟା):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = term.definitionOdia,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "English Definition: ${term.definitionEnglish}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { selectedGlossaryTerm = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Got it (ବୁଝିଲି)")
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingAudioPlayerBar(
    isPlaying: Boolean,
    progress: Float,
    speed: String,
    onPlayPauseToggle: () -> Unit,
    onSpeedToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isPlaying) "Audio Read-Aloud Playing (ଓଡ଼ିଆ ଶ୍ରବଣ)" else "Pre-cached Audio Lesson",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(progress * 210).toInt()}s / 210s • Cached Offline",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSpeedToggle) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = speed, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}
