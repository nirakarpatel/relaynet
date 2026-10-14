package com.nmiet.app.ui.screens.curriculum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentOrigin
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
    onShareViaBluetooth: () -> Unit,
    onGenerateNotes: (String) -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val selectedBucket by viewModel.selectedBucket.collectAsState()
    val downloadedCount by viewModel.downloadedPacksCount.collectAsState()
    val sharedCount by viewModel.sharedPacksCount.collectAsState()
    val packs by viewModel.filteredPacks.collectAsState()

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
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Color.White)
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

                Spacer(modifier = Modifier.height(14.dp))

                // MODULE 2: TWO-BUTTON CONTENT BUCKET FILTER ("DOWNLOADED" VS "SHARED")
                Text(
                    text = "Content Library Buckets (ପାଠ୍ୟକ୍ରମ ସଂଗ୍ରହ):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Downloaded Content
                    val isDownloadedSelected = selectedBucket == ContentBucketFilter.DOWNLOADED
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.onBucketSelected(ContentBucketFilter.DOWNLOADED) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDownloadedSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = if (isDownloadedSelected)
                            BorderStroke(2.dp, PrimaryBlue)
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDownloadedSelected) 2.dp else 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDownloadedSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = null,
                                        tint = if (isDownloadedSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDownloadedSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$downloadedCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDownloadedSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📥 Downloaded",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDownloadedSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cloud / Internet Packs",
                                fontSize = 10.sp,
                                color = if (isDownloadedSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Button 2: Shared Content (P2P Shared & Received)
                    val isSharedSelected = selectedBucket == ContentBucketFilter.SHARED
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.onBucketSelected(ContentBucketFilter.SHARED) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSharedSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = if (isSharedSelected)
                            BorderStroke(2.dp, PrimaryBlue)
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSharedSelected) 2.dp else 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSharedSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CellTower,
                                        contentDescription = null,
                                        tint = if (isSharedSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSharedSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$sharedCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSharedSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔄 P2P Shared",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSharedSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Bluetooth ShikshaMesh",
                                fontSize = 10.sp,
                                color = if (isSharedSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Board, Class, Subject Chips Row
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
                                    Board.ODISHA_STATE -> "Odisha BSE"
                                    Board.CBSE -> "CBSE"
                                    Board.CISCE_ICSE -> "ICSE"
                                }
                                Text(labelText, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        )
                    }

                    listOf(9, 10).forEach { grade ->
                        val isSelected = selectedGrade == grade
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onGradeSelected(grade) },
                            label = { Text("Class $grade", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    listOf("Science (ବିଜ୍ଞାନ)", "Mathematics").forEach { subject ->
                        val isSelected = selectedSubject == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSubjectSelected(subject) },
                            label = { Text(subject, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                // Section header reflecting current bucket
                val sectionHeader = if (selectedBucket == ContentBucketFilter.DOWNLOADED) {
                    "📥 Downloaded Content Packs ($downloadedCount Available)"
                } else {
                    "🔄 P2P Shared & Received Packs ($sharedCount Available)"
                }

                Text(
                    text = sectionHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Empty State Card when selected bucket has 0 packs
            if (packs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selectedBucket == ContentBucketFilter.SHARED) Icons.Default.CellTower else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedBucket == ContentBucketFilter.SHARED)
                                    "No P2P Shared Packs Yet"
                                else
                                    "No Downloaded Packs in this Filter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedBucket == ContentBucketFilter.SHARED)
                                    "Connect with a nearby phone via Bluetooth RFCOMM or add a demo shared pack to see offline mesh transfer in action."
                                else
                                    "Download packs from the server when connected to internet.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (selectedBucket == ContentBucketFilter.SHARED) {
                                    Button(onClick = onShareViaBluetooth) {
                                        Text("Open ShikshaMesh Radar", fontSize = 12.sp)
                                    }
                                    OutlinedButton(onClick = { viewModel.seedDemoSharedPack() }) {
                                        Text("Simulate Demo Pack", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Learning Packs List
            items(packs, key = { it.packId }) { pack ->
                LearningPackCard(
                    pack = pack,
                    onOpenLesson = { onOpenLesson(pack.packId) },
                    onStartQuiz = { onStartQuiz(pack.packId) },
                    onShareViaBluetooth = onShareViaBluetooth,
                    onGenerateNotes = { onGenerateNotes(pack.packId) }
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
    onShareViaBluetooth: () -> Unit,
    onGenerateNotes: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Header: Footprint, Origin, and Cache Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StoredOfflineBadge()
                    if (pack.origin == ContentOrigin.P2P_RECEIVED || pack.origin == ContentOrigin.P2P_SENT || pack.isReceivedViaMesh) {
                        val peer = pack.peerSenderName ?: "Node-02"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Received from $peer via Bluetooth • SHA-256 Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "📥 Downloaded (Cloud)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Storage footprint e.g. "1.8 MB"
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
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
                text = pack.lesson.summaryEnglish.ifBlank { pack.titleEnglish },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick action chips: AI Revision Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onGenerateNotes,
                    label = { Text("📝 AI Notes (ନୋଟ୍)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons: "Open Lesson", "Take Diagnostic Quiz", and "Share via Bluetooth"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenLesson,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Open Lesson", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onStartQuiz,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Take Quiz", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onShareViaBluetooth,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Share P2P", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}
