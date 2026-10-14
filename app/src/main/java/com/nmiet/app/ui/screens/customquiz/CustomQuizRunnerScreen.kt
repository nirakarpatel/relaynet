package com.nmiet.app.ui.screens.customquiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.CustomQuizQuestion
import com.nmiet.app.domain.model.QuizDifficulty
import com.nmiet.app.domain.model.QuizSourceType
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomQuizRunnerScreen(
    viewModel: CustomQuizViewModel,
    onBackClick: () -> Unit = {},
    onNavigateToNotes: (String) -> Unit = {},
    onNavigateToMastery: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isConfiguring) "Custom Quiz Generator"
                            else if (uiState.isCompleted) "Assessment Analytics"
                            else "Diagnostic Quiz (${uiState.currentIndex + 1}/${uiState.totalCount})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (syncStatus.isOnline) "Cloud AI Concept Mapped" else "Offline Room SQLite Grounded",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!uiState.isConfiguring && !uiState.isCompleted) {
                            viewModel.resetToConfiguration()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isGenerating -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Text(
                                text = "Synthesizing customized concept-mapped quiz...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                uiState.isConfiguring -> {
                    QuizConfiguratorContent(
                        uiState = uiState,
                        onSourceChange = { viewModel.setSourceType(it) },
                        onCountChange = { viewModel.setQuestionCount(it) },
                        onDifficultyChange = { viewModel.setDifficulty(it) },
                        onStartQuiz = { viewModel.startQuizGeneration() }
                    )
                }

                uiState.isCompleted -> {
                    QuizAnalyticsContent(
                        uiState = uiState,
                        onRetakeFailed = { viewModel.retakeFailedQuestions() },
                        onGenerateNotes = { onNavigateToNotes("relaynet_class9_science_photosynthesis_v1") },
                        onBackToMastery = onNavigateToMastery,
                        onNewQuiz = { viewModel.resetToConfiguration() }
                    )
                }

                else -> {
                    uiState.currentQuestion?.let { question ->
                        QuizRunnerContent(
                            question = question,
                            uiState = uiState,
                            onOptionSelected = { viewModel.selectOption(it) },
                            onNextClick = { viewModel.nextQuestion() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizConfiguratorContent(
    uiState: CustomQuizUiState,
    onSourceChange: (QuizSourceType) -> Unit,
    onCountChange: (Int) -> Unit,
    onDifficultyChange: (QuizDifficulty) -> Unit,
    onStartQuiz: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "On-Demand Diagnostic Quiz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Synthesize targeted quizzes from chapter concepts, weak topics, or textbook diagrams.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section 1: Question Source
        item {
            Text(text = "1. Quiz Source (ପ୍ରଶ୍ନ ଉତ୍ସ):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuizSourceType.entries.forEach { source ->
                    val isSelected = uiState.config.sourceType == source
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSourceChange(source) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(source.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(source.odiaName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Question Count
        item {
            Text(text = "2. Question Count (ପ୍ରଶ୍ନ ସଂଖ୍ୟା):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(3, 5, 10).forEach { count ->
                    val isSelected = uiState.config.questionCount == count
                    OutlinedButton(
                        onClick = { onCountChange(count) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) PrimaryBlue else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "$count Qs",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 3: Difficulty
        item {
            Text(text = "3. Difficulty Level (କଠିନତା ସ୍ତର):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                QuizDifficulty.entries.forEachIndexed { index, diff ->
                    SegmentedButton(
                        selected = uiState.config.difficulty == diff,
                        onClick = { onDifficultyChange(diff) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = QuizDifficulty.entries.size),
                        icon = {}
                    ) {
                        Text(diff.displayName, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Launch Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Assessment (${uiState.config.questionCount} Questions)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuizRunnerContent(
    question: CustomQuizQuestion,
    uiState: CustomQuizUiState,
    onOptionSelected: (Int) -> Unit,
    onNextClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Progress Bar
        item {
            val progress = (uiState.currentIndex + 1).toFloat() / uiState.totalCount.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Question Statement Card
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Concept: ${question.conceptTag}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Text(
                            text = "Q ${uiState.currentIndex + 1} of ${uiState.totalCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = question.questionOdia,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 23.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = question.questionEnglish,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Option Cards
        items(question.optionsEnglish.size) { index ->
            val optEng = question.optionsEnglish[index]
            val optOdia = question.optionsOdia.getOrElse(index) { optEng }
            val isSelected = uiState.selectedOptionIndex == index
            val isRevealed = uiState.isAnswerRevealed
            val isCorrect = index == question.correctIndex

            val containerColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> SuccessGreen.copy(alpha = 0.18f)
                    isRevealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                    isSelected -> PrimaryBlue.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "opt_bg"
            )

            val borderColor = when {
                isRevealed && isCorrect -> SuccessGreen
                isRevealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                isSelected -> PrimaryBlue
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isRevealed) { onOptionSelected(index) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(if (isSelected || (isRevealed && isCorrect)) 2.dp else 1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val optionLabel = ('A'.code + index).toChar().toString()
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isRevealed && isCorrect -> SuccessGreen
                                    isRevealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                    isSelected -> PrimaryBlue
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected || (isRevealed && isCorrect)) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = optOdia,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = optEng,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isRevealed) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else if (isSelected) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isCorrect) SuccessGreen else if (isSelected) MaterialTheme.colorScheme.error else Color.Transparent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Misconception Diagnostic Card (Triggered immediately on wrong choice)
        if (uiState.isAnswerRevealed) {
            item {
                val selectedIdx = uiState.selectedOptionIndex
                val isWrong = selectedIdx != null && selectedIdx != question.correctIndex
                val distractorExplanation = question.distractorExplanations[selectedIdx]

                if (isWrong && !distractorExplanation.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, WarningAmber)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Diagnostic Misconception Breakdown:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarningAmber)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = distractorExplanation, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Official Scientific Explanation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Curriculum Grounded Solution:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = question.explanationOdia, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = question.explanationEnglish, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }
            }

            // Next / Finish Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (uiState.currentIndex + 1 < uiState.totalCount) "Next Question" else "View Assessment Results",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun QuizAnalyticsContent(
    uiState: CustomQuizUiState,
    onRetakeFailed: () -> Unit,
    onGenerateNotes: () -> Unit,
    onBackToMastery: () -> Unit,
    onNewQuiz: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Dial Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${uiState.scorePercent}%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.scorePercent >= 70) SuccessGreen else WarningAmber
                    )
                    Text(
                        text = "${uiState.correctCount} of ${uiState.totalCount} Questions Correct",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.scorePercent >= 85) "🌟 Strong Mastery Achieved"
                        else if (uiState.scorePercent >= 60) "📈 Developing Competency - Review Recommended"
                        else "⚠️ Foundational Concept Gaps Detected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Concept Breakdown Ratings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Concept Tag Diagnostics Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.masteredConcepts.isNotEmpty()) {
                        Text("✅ Demonstrated Mastery:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.masteredConcepts.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (uiState.weakConcepts.isNotEmpty()) {
                        Text("⚠️ Needs Revision / Misconceptions:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.weakConcepts.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(WarningAmber.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button 1: Generate Revision Notes for Missed Topics
                Button(
                    onClick = onGenerateNotes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Revision Notes for Missed Topics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Retake Failed Questions
                if (uiState.correctCount < uiState.totalCount) {
                    OutlinedButton(
                        onClick = onRetakeFailed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retake Failed Questions (${uiState.totalCount - uiState.correctCount} Qs)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Button 3: Back to Mastery Dashboard
                OutlinedButton(
                    onClick = onBackToMastery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Mastery Dashboard", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
