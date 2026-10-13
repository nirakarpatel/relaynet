package com.nmiet.app.ui.screens.mastery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.formatRelativeTime
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun MasteryScreen(
    viewModel: MasteryViewModel,
    onReadTopic: (packId: String, sectionTag: String) -> Unit = { _, _ -> },
    onRetakeTopicQuiz: (packId: String, topicTag: String) -> Unit = { _, _ -> },
    onPracticeTopic: (String) -> Unit = {}
) {
    val masteryList by viewModel.topicMasteryList.collectAsState()
    val attempts by viewModel.quizAttempts.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    val overallScore = if (masteryList.isNotEmpty()) {
        masteryList.map { it.masteryScore }.average().toInt()
    } else 61

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Mastery & Diagnostics",
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Chapter Mastery Dashboard Card featuring Circular Progress Ring
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chapter 4: Photosynthesis Mastery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Circular Progress Ring showing 0-100% chapter mastery score
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularMasteryRing(scorePercent = overallScore)

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text("Categorization Tier:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))

                                // Mandatory Categorization Badges
                                CategorizationTierBadge(scorePercent = overallScore)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Brackets:\n" +
                                            "• < 50%: Foundation Needed\n" +
                                            "• 50–70%: Developing\n" +
                                            "• 70–85%: Proficient\n" +
                                            "• > 85%: Strong Mastery",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Targeted Practice Deck
                Text(
                    text = "Targeted Practice Deck (ସମ୍ଭାବ୍ୟ ଅଭ୍ୟାସ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dynamic practice cards generated from diagnostic historical errors & misconception gaps.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic targeted practice cards
                TargetedPracticeDeckSection(
                    attempts = attempts,
                    onReadTopic = onReadTopic,
                    onRetakeTopicQuiz = onRetakeTopicQuiz
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Topic-Wise Mastery Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(masteryList, key = { it.topicId }) { mastery ->
                TopicMasteryCard(
                    mastery = mastery,
                    onPracticeClick = { onPracticeTopic(mastery.topicId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Recent Diagnostic Quiz History
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Diagnostic Quiz Attempts Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Synced Local", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (attempts.isEmpty()) {
                item {
                    Text(
                        text = "No diagnostic quizzes attempted yet. Take the Chapter 4 Photosynthesis quiz to generate practice cards.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(attempts, key = { it.id }) { attempt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = attempt.topicName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    text = "${formatRelativeTime(attempt.timestamp)} • Score: ${attempt.correctCount}/${attempt.totalCount}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (attempt.weakConcepts.isNotEmpty()) {
                                    Text(
                                        text = "Misconceptions Identified: ${attempt.weakConcepts.joinToString()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                }
                            }
                            Text(
                                text = "${attempt.scorePercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (attempt.scorePercent >= 70) SuccessGreen else WarningAmber
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun CircularMasteryRing(scorePercent: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = scorePercent / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "ringProgress"
    )

    val strokeColor = when {
        scorePercent >= 85 -> SuccessGreen
        scorePercent >= 70 -> PrimaryBlue
        scorePercent >= 50 -> WarningAmber
        else -> MaterialTheme.colorScheme.error
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val strokeWidth = 10.dp.toPx()
            // Track arc
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            // Progress arc
            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$scorePercent%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = strokeColor
            )
            Text(
                text = "Mastery",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategorizationTierBadge(scorePercent: Int) {
    val (text, color, bg) = when {
        scorePercent >= 85 -> Triple("> 85%: Strong Mastery", SuccessGreen, SuccessGreen.copy(alpha = 0.15f))
        scorePercent >= 70 -> Triple("70–85%: Proficient", PrimaryBlue, PrimaryBlue.copy(alpha = 0.15f))
        scorePercent >= 50 -> Triple("50–70%: Developing", WarningAmber, WarningAmber.copy(alpha = 0.18f))
        else -> Triple("< 50%: Foundation Needed", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TargetedPracticeDeckSection(
    attempts: List<com.nmiet.app.domain.model.QuizAttempt>,
    onReadTopic: (packId: String, sectionTag: String) -> Unit,
    onRetakeTopicQuiz: (packId: String, topicTag: String) -> Unit
) {
    val packId = "relaynet_class9_science_photosynthesis_v1"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Targeted Practice: Photolysis & Light Reactions - 3 Questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Focused diagnostic practice loop targeting water photolysis, oxygen evolution, and stomatal gas regulation.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: 📖 Read Topic (ପ୍ରସଙ୍ଗ ପଢ଼ନ୍ତୁ)
                OutlinedButton(
                    onClick = { onReadTopic(packId, "photolysis") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "📖 Read Topic\n(ପ୍ରସଙ୍ଗ ପଢ଼ନ୍ତୁ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }

                // Button 2: 🎯 Retake Topic Quiz (ଅଭ୍ୟାସ କୁଇଜ୍ ଦିଅନ୍ତୁ)
                Button(
                    onClick = { onRetakeTopicQuiz(packId, "PHOTOLYSIS_WATER") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🎯 Retake Quiz\n(ଅଭ୍ୟାସ କୁଇଜ୍ ଦିଅନ୍ତୁ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TopicMasteryCard(
    mastery: TopicMastery,
    onPracticeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = mastery.topicTitleEnglish, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = mastery.topicTitleOdia, fontSize = 12.sp, color = PrimaryBlue)
                }
                Text(
                    text = "${mastery.masteryScore}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            CategorizationTierBadge(scorePercent = mastery.masteryScore)

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarningAmber.copy(alpha = 0.12f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recommendation: ${mastery.recommendedNextAction}",
                        fontSize = 11.sp,
                        color = WarningAmber,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onPracticeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Practice Topic (ପୁନର୍ବାର ଅଭ୍ୟାସ କରନ୍ତୁ)", fontSize = 12.sp)
            }
        }
    }
}
