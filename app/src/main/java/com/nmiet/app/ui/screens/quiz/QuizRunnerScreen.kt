package com.nmiet.app.ui.screens.quiz

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun QuizRunnerScreen(
    viewModel: QuizViewModel,
    onBackClick: () -> Unit,
    onNavigateToMastery: () -> Unit
) {
    val pack by viewModel.learningPack.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOption by viewModel.selectedOptionIndex.collectAsState()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val language by viewModel.language.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val weakConcepts by viewModel.weakConcepts.collectAsState()
    val isFinished by viewModel.isQuizFinished.collectAsState()
    val diagnosticBanner by viewModel.diagnosticBanner.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Diagnostic Quiz Runner",
                isOnline = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (pack == null) {
            EmptyView(
                title = "Loading Quiz...",
                subtitle = "Preparing diagnostic questions from local learning pack.",
                modifier = Modifier.padding(innerPadding)
            )
        } else if (isFinished) {
            // Completion Summary Sheet
            val total = pack!!.quizQuestions.size
            val scorePercent = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0
            val allConceptTags = pack!!.quizQuestions.map { it.conceptTag }.distinct()
            val masteredConcepts = allConceptTags.filter { !weakConcepts.contains(it) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (scorePercent >= 70) SuccessGreen else WarningAmber
                )
                Spacer(modifier = Modifier.height(12.dp))
                val completedTitle = when (language) {
                    AppLanguage.ENGLISH_ONLY -> "Quiz Completed!"
                    AppLanguage.ODIA_ONLY -> "କୁଇଜ୍ ସମ୍ପୂର୍ଣ୍ଣ!"
                    AppLanguage.HINDI_ONLY -> "प्रश्नोत्तरी संपन्न!"
                    AppLanguage.BILINGUAL_EN_HI -> "Quiz Completed! (प्रश्नोत्तरी पूर्ण)"
                    AppLanguage.BILINGUAL_EN_OR -> "Quiz Completed! (କୁଇଜ୍ ସମ୍ପୂର୍ଣ୍ଣ)"
                }
                Text(
                    text = completedTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Overall Accuracy: $correctCount / $total ($scorePercent%)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                val tierText = when {
                    scorePercent >= 85 -> "Strong Mastery (>85%)"
                    scorePercent >= 70 -> "Proficient (70–85%)"
                    scorePercent >= 50 -> "Developing (50–70%)"
                    else -> "Foundation Needed (<50%)"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = tierText, fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Breakdown: Mastered Concepts vs Needing Review
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Concept Mastery Summary:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Mastered concepts
                        Text(text = "✓ Mastered Concepts (${masteredConcepts.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (masteredConcepts.isEmpty()) {
                            Text(text = "• None yet. Review lesson materials to build foundation.", fontSize = 11.sp)
                        } else {
                            masteredConcepts.forEach { tag ->
                                Text(text = "• ${tag.replace('_', ' ').capitalize()}", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Concepts needing review
                        Text(text = "⚠️ Concepts Needing Review (${weakConcepts.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (weakConcepts.isEmpty()) {
                            Text(text = "• All concept tags mastered in this attempt!", fontSize = 11.sp, color = SuccessGreen)
                        } else {
                            weakConcepts.forEach { tag ->
                                Text(text = "• ${tag.replace('_', ' ').capitalize()}", fontSize = 11.sp, color = WarningAmber)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mandatory Action Button: "Add Weak Concepts to Practice Queue"
                Button(
                    onClick = onNavigateToMastery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Weak Concepts to Practice Queue & View Dashboard", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Lessons")
                }
            }
        } else {
            // Interactive Question Runner
            val questions = pack!!.quizQuestions
            val currentQuestion = questions.getOrNull(currentIndex)

            if (currentQuestion != null) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Header progress indicator: "Question X of 5" with Score Tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Question ${currentIndex + 1} of ${questions.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Score Tracker: $correctCount Correct",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = language == AppLanguage.ODIA_ONLY || language == AppLanguage.BILINGUAL_EN_OR,
                                onClick = { viewModel.setLanguage(AppLanguage.ODIA_ONLY) },
                                label = { Text("ଓଡ଼ିଆ", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = language == AppLanguage.HINDI_ONLY || language == AppLanguage.BILINGUAL_EN_HI,
                                onClick = { viewModel.setLanguage(AppLanguage.HINDI_ONLY) },
                                label = { Text("हिंदी", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = language == AppLanguage.ENGLISH_ONLY,
                                onClick = { viewModel.setLanguage(AppLanguage.ENGLISH_ONLY) },
                                label = { Text("English", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1) / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Concept Tag: [${currentQuestion.conceptTag}]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.getQuestion(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Multiple-Choice Options
                    val options = currentQuestion.getOptions(language)
                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOption == index
                        val isCorrect = index == currentQuestion.correctIndex

                        val (bgColor, borderColor) = when {
                            isSubmitted && isCorrect -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                            isSubmitted && isSelected && !isCorrect -> Pair(WarningAmber.copy(alpha = 0.2f), WarningAmber)
                            isSelected -> Pair(PrimaryBlue.copy(alpha = 0.12f), PrimaryBlue)
                            else -> Pair(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable(enabled = !isSubmitted) { viewModel.selectOption(index) },
                            colors = CardDefaults.cardColors(containerColor = bgColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = optionText,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || (isSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSubmitted && isCorrect) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                } else if (isSubmitted && isSelected && !isCorrect) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Mandatory Misconception Diagnostic Card rendering immediately on submit
                    AnimatedVisibility(visible = isSubmitted) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Targeted Diagnostic Interceptor Banner (Prompt Requirement)
                            if (diagnosticBanner != null) {
                                val banner = diagnosticBanner!!
                                val localizedMessage = when (language) {
                                    AppLanguage.ENGLISH_ONLY -> banner.messageEnglish
                                    AppLanguage.ODIA_ONLY -> banner.messageOdia
                                    AppLanguage.HINDI_ONLY -> banner.messageHindi
                                    AppLanguage.BILINGUAL_EN_HI -> "${banner.messageEnglish}\n\n${banner.messageHindi}"
                                    AppLanguage.BILINGUAL_EN_OR -> "${banner.messageEnglish}\n\n${banner.messageOdia}"
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = banner.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = localizedMessage,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            if (selectedOption != currentQuestion.correctIndex) {
                                // Amber Misconception Diagnostic Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.18f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Misconception Diagnostic Card",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningAmber
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = currentQuestion.getMisconceptionHint(language),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 18.sp,
                                            color = WarningAmber
                                        )
                                    }
                                }
                            } else {
                                // Correct answer card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("✓ Correct Concept Application!", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 13.sp)
                                            Text(
                                                text = currentQuestion.getExplanation(language),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit / Next CTA Button
                    if (!isSubmitted) {
                        Button(
                            onClick = { viewModel.submitAnswer() },
                            enabled = selectedOption != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Check Answer")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (currentIndex < questions.size - 1) "Next Question →" else "Finish Quiz & View Results")
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
