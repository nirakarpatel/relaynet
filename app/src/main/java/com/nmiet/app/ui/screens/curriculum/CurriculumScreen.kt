package com.nmiet.app.ui.screens.curriculum

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.ReceivedViaNearbyBadge
import com.nmiet.app.ui.components.StoredOfflineBadge
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun CurriculumScreen(
    viewModel: CurriculumViewModel,
    onOpenLesson: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onShareViaBluetooth: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val packs by viewModel.learningPacks.collectAsState()

    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val curriculumNodes by viewModel.allCurriculumNodes.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Curriculum Explorer",
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

                // Learner Profile Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${profile.name} • Class $selectedGrade",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${selectedBoard.displayName} • ${profile.preferredLanguage.displayName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Top Filter Chips (Board, Class, Subject)
                Text(
                    text = "Filters (ବୋର୍ଡ, ଶ୍ରେଣୀ ଓ ବିଷୟ):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Board Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Board.entries.forEach { board ->
                        val isSelected = selectedBoard == board
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onBoardSelected(board) },
                            label = {
                                val labelText = when (board) {
                                    Board.ODISHA_STATE -> "Odisha State Board (BSE)"
                                    Board.CBSE -> "CBSE"
                                    Board.CISCE_ICSE -> "ICSE"
                                }
                                Text(labelText, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Class Grade & Subject Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Class chips
                    listOf(9, 10).forEach { grade ->
                        val isSelected = selectedGrade == grade
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onGradeSelected(grade) },
                            label = { Text("Class $grade", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Subject chips
                    listOf("Science (ବିଜ୍ଞାନ)", "Mathematics").forEach { subject ->
                        val isSelected = selectedSubject == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSubjectSelected(subject) },
                            label = { Text(subject, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Offline Notice Strip if Offline
                if (!syncStatus.isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudOff, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Mode: Serving curriculum from local Room SQLite cache.",
                                fontSize = 12.sp,
                                color = WarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Downloaded Learning Packs (ଅଫଲାଇନ୍ ପାଠ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Learning Packs List
            items(packs, key = { it.packId }) { pack ->
                LearningPackCard(
                    pack = pack,
                    onOpenLesson = { onOpenLesson(pack.packId) },
                    onStartQuiz = { onStartQuiz(pack.packId) },
                    onShareViaBluetooth = onShareViaBluetooth
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Additional Board Curriculum Outline Catalog
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Curriculum Catalogue (Secondary Scope)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Multi-board BSE, CBSE & ICSE mapped curriculum nodes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(curriculumNodes, key = { it.id }) { node ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${node.board.code} • Class ${node.classGrade} • ${node.subject}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            if (node.isAvailableOffline) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Pack Active", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Cloud Available", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = node.chapterTitleEnglish, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = node.chapterTitleOdia, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun LearningPackCard(
    pack: LearningPack,
    onOpenLesson: () -> Unit,
    onStartQuiz: () -> Unit,
    onShareViaBluetooth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Header: Footprint and Cache Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StoredOfflineBadge()
                    if (pack.isReceivedViaMesh) {
                        ReceivedViaNearbyBadge(peerName = pack.peerSenderName)
                    }
                }

                // Metadata: Storage footprint e.g. "1.8 MB"
                val footprintText = if (pack.sizeBytes >= 1_000_000) {
                    "%.1f MB".format(pack.sizeBytes / 1_000_000.0)
                } else {
                    "${pack.sizeBytes / 1024} KB"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Footprint: $footprintText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chapter Title: "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)"
            Text(
                text = pack.titleOdia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pack.lesson.summaryEnglish,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: "Open Lesson", "Take Diagnostic Quiz", and "Share via Bluetooth"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenLesson,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Lesson", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onStartQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Take Quiz", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShareViaBluetooth,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share P2P", fontSize = 12.sp)
                }
            }
        }
    }
}
