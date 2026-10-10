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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Diagnostic Quiz",
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
            // Quiz Results Summary Screen
            val total = pack!!.quizQuestions.size
            val scorePercent = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (scorePercent >= 70) SuccessGreen else WarningAmber
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quiz Completed! (କୁଇଜ୍ ସମ୍ପୂର୍ଣ୍ଣ)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Score: $correctCount / $total ($scorePercent%)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                val tierText = when {
                    scorePercent >= 85 -> "Strong Mastery (ଉତ୍କୃଷ୍ଟ ଦକ୍ଷତା)"
                    scorePercent >= 70 -> "Good Progress (ଉତ୍ତମ ଅଗ୍ରଗତି)"
                    scorePercent >= 40 -> "Developing (ବିକାଶଶୀଳ)"
                    else -> "Needs Foundation Practice (ମୂଳଧାରଣା ଅଭ୍ୟାସ ଆବଶ୍ୟକ)"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = tierText, fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 13.sp)
                }

                if (weakConcepts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Concepts to Review (ପୁନର୍ବାର ପଢ଼ିବାକୁ ଥିବା ଧାରଣା):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = WarningAmber
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            weakConcepts.forEach { concept ->
                                Text(text = "• ${concept.replace('_', ' ').capitalize()}", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onNavigateToMastery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Progress & Mastery Dashboard")
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
                    // Header progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = language == AppLanguage.ODIA,
                                onClick = { viewModel.setLanguage(AppLanguage.ODIA) },
                                label = { Text("ଓଡ଼ିଆ", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = language == AppLanguage.ENGLISH,
                                onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) currentQuestion.questionOdia else currentQuestion.questionEnglish,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Options
                    val options = if (language == AppLanguage.ODIA) currentQuestion.optionsOdia else currentQuestion.optionsEnglish
                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOption == index
                        val isCorrect = index == currentQuestion.correctIndex

                        val (bgColor, borderColor) = when {
                            isSubmitted && isCorrect -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                            isSubmitted && isSelected && !isCorrect -> Pair(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), MaterialTheme.colorScheme.error)
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

                    // Explanation & Misconception Box if submitted
                    AnimatedVisibility(visible = isSubmitted) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Concept Explanation:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) currentQuestion.explanationOdia else currentQuestion.explanationEnglish,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )

                                    if (selectedOption != currentQuestion.correctIndex) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(WarningAmber.copy(alpha = 0.15f))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.ODIA) currentQuestion.misconceptionHintOdia else currentQuestion.misconceptionHintEnglish,
                                                fontSize = 11.sp,
                                                color = WarningAmber,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit or Next Button
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
