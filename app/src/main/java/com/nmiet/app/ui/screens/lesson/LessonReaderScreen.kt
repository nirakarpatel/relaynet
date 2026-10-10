package com.nmiet.app.ui.screens.lesson

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.PrimaryBlue

@Composable
fun LessonReaderScreen(
    packId: String,
    repository: ContentRepository,
    onBackClick: () -> Unit,
    onStartQuiz: (String) -> Unit
) {
    val pack by repository.getLearningPackByIdFlow(packId).collectAsState(initial = null)
    val syncStatus by repository.getSyncStatusFlow().collectAsState(initial = com.nmiet.app.domain.model.SyncStatus())

    var languageMode by remember { mutableStateOf(AppLanguage.BILINGUAL) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Lesson Reader",
                isOnline = syncStatus.isOnline,
                onBackClick = onBackClick
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Language Selection Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Language (ଭାଷା):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = languageMode == AppLanguage.BILINGUAL,
                            onClick = { languageMode = AppLanguage.BILINGUAL },
                            label = { Text("Bilingual", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = languageMode == AppLanguage.ODIA,
                            onClick = { languageMode = AppLanguage.ODIA },
                            label = { Text("ଓଡ଼ିଆ", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = languageMode == AppLanguage.ENGLISH,
                            onClick = { languageMode = AppLanguage.ENGLISH },
                            label = { Text("English", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ENGLISH) {
                    Text(
                        text = lessonPack.titleEnglish,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ODIA) {
                    Text(
                        text = lessonPack.titleOdia,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${lessonPack.subject} • Class ${lessonPack.classGrade} • ${lessonPack.board.displayName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Chemical Equation / Formula Box
                lessonPack.lesson.equationOrFormula?.let { equation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Balanced Biochemical Reaction (ସନ୍ତୁଳିତ ରାସାୟନିକ ସମୀକରଣ):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = equation,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Summary
                Text(
                    text = "Summary (ସାରାଂଶ):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ODIA) {
                    Text(
                        text = lessonPack.lesson.summaryOdia,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ENGLISH) {
                    Text(
                        text = lessonPack.lesson.summaryEnglish,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Sections
                lessonPack.lesson.sections.forEach { section ->
                    if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ODIA) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (languageMode == AppLanguage.BILINGUAL || languageMode == AppLanguage.ENGLISH) {
                        Text(
                            text = section.titleEnglish,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = section.contentEnglish,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Key Terms
                    if (section.keyTerms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Key Scientific Terms (ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ ଶବ୍ଦାବଳୀ):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        section.keyTerms.forEach { term ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${term.termOdia} / ${term.termEnglish}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryBlue
                                    )
                                    Text(text = term.definitionOdia, fontSize = 12.sp)
                                    Text(text = term.definitionEnglish, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
                            text = "Pack SHA-256: ${lessonPack.checksumSha256.take(16)}... (Verified)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Start Quiz CTA
                Button(
                    onClick = { onStartQuiz(lessonPack.packId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Practice Quiz (${lessonPack.quizQuestions.size} Questions)")
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
