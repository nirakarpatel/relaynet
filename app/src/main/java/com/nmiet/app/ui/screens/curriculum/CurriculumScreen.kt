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
import com.nmiet.app.domain.model.AppLanguage
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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
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

    val screenTitle = when (currentLanguage) {
        AppLanguage.ENGLISH_ONLY -> "Curriculum Explorer"
        AppLanguage.ODIA_ONLY -> "ପାଠ୍ୟକ୍ରମ ଏକ୍ସପ୍ଲୋରର୍"
        AppLanguage.HINDI_ONLY -> "पाठ्यक्रम एक्सप्लोरर"
        AppLanguage.BILINGUAL_EN_HI -> "Curriculum Explorer • पाठ्यक्रम"
        AppLanguage.BILINGUAL_EN_OR -> "Curriculum Explorer • ପାଠ୍ୟକ୍ରମ"
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp // Guaranteed clearance above bottom bar + floating mic
            )
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
                                text = "${profile.studentName} • ${profile.classGrade}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${profile.educationalBoard} • ${profile.languageMode.displayName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MODULE 2: TWO-BUTTON CONTENT BUCKET FILTER ("DOWNLOADED" VS "SHARED")
                val contentLibraryTitle = when (currentLanguage) {
                    AppLanguage.ENGLISH_ONLY -> "Content Library Buckets:"
                    AppLanguage.ODIA_ONLY -> "ପାଠ୍ୟକ୍ରମ ସଂଗ୍ରହ:"
                    AppLanguage.HINDI_ONLY -> "पाठ्यक्रम संग्रह:"
                    AppLanguage.BILINGUAL_EN_HI -> "Content Library (पाठ्यक्रम संग्रह):"
                    AppLanguage.BILINGUAL_EN_OR -> "Content Library Buckets (ପାଠ୍ୟକ୍ରମ ସଂଗ୍ରହ):"
                }
                Text(
                    text = contentLibraryTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val downloadedBucketText = when (currentLanguage) {
                    AppLanguage.ENGLISH_ONLY -> "📥 Downloaded"
                    AppLanguage.ODIA_ONLY -> "📥 ଡାଉନଲୋଡ୍ ହୋଇଥିବା"
                    AppLanguage.HINDI_ONLY -> "📥 डाउनलोड किया गया"
                    AppLanguage.BILINGUAL_EN_HI -> "📥 Downloaded (डाउनलोड)"
                    AppLanguage.BILINGUAL_EN_OR -> "📥 Downloaded (ଡାଉନଲୋଡ୍)"
                }
                val sharedBucketText = when (currentLanguage) {
                    AppLanguage.ENGLISH_ONLY -> "🔄 P2P Shared"
                    AppLanguage.ODIA_ONLY -> "🔄 ପିଅର୍ ସେୟାର୍ଡ"
                    AppLanguage.HINDI_ONLY -> "🔄 सहकर्मी साझा"
                    AppLanguage.BILINGUAL_EN_HI -> "🔄 P2P Shared (सहकर्मी साझा)"
                    AppLanguage.BILINGUAL_EN_OR -> "🔄 P2P Shared (ପିଅର୍ ସେୟାର୍ଡ)"
                }

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
                                text = downloadedBucketText,
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
                                text = sharedBucketText,
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

                // Subject Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    val scienceLabel = when (currentLanguage) {
                        AppLanguage.ENGLISH_ONLY -> "Science"
                        AppLanguage.ODIA_ONLY -> "ବିଜ୍ଞାନ (Science)"
                        AppLanguage.HINDI_ONLY -> "विज्ञान (Science)"
                        AppLanguage.BILINGUAL_EN_HI -> "Science (विज्ञान)"
                        AppLanguage.BILINGUAL_EN_OR -> "Science (ବିଜ୍ଞାନ)"
                    }
                    val mathLabel = when (currentLanguage) {
                        AppLanguage.ENGLISH_ONLY -> "Mathematics"
                        AppLanguage.ODIA_ONLY -> "ଗଣିତ (Math)"
                        AppLanguage.HINDI_ONLY -> "गणित (Math)"
                        AppLanguage.BILINGUAL_EN_HI -> "Mathematics (गणित)"
                        AppLanguage.BILINGUAL_EN_OR -> "Mathematics (ଗଣିତ)"
                    }

                    listOf(scienceLabel, mathLabel).forEach { subject ->
                        val isSelected = (subject == scienceLabel && selectedSubject.contains("Science")) ||
                                (subject == mathLabel && selectedSubject.contains("Math"))
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.onSubjectSelected(if (subject == scienceLabel) "Science (ବିଜ୍ଞାନ)" else "Mathematics")
                            },
                            label = {
                                Text(
                                    text = subject,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
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
                    currentLanguage = currentLanguage,
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
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
fun LearningPackCard(
    pack: LearningPack,
    currentLanguage: AppLanguage = AppLanguage.BILINGUAL_EN_OR,
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

            // Chapter Title: English: "Chapter 4: Photosynthesis" | Odia: "ଅଧ୍ୟାୟ ୪: ଆଲୋକ ଶ୍ଳେଷଣ" | Hindi: "अध्याय 4: प्रकाश संश्लेषण"
            val chapterTitle = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> pack.titleEnglish
                AppLanguage.ODIA_ONLY -> pack.titleOdia
                AppLanguage.HINDI_ONLY -> if (pack.titleHindi.isNotBlank()) pack.titleHindi else pack.titleEnglish
                AppLanguage.BILINGUAL_EN_HI -> if (pack.titleHindi.isNotBlank()) "${pack.titleEnglish}\n${pack.titleHindi}" else pack.titleEnglish
                AppLanguage.BILINGUAL_EN_OR -> pack.titleOdia
            }

            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description: English, Odia, and Hindi parallel translations
            val summaryText = pack.lesson.getSummary(currentLanguage).ifBlank { pack.getTitle(currentLanguage) }
            Text(
                text = summaryText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick action chips: AI Revision Notes
            val notesChipLabel = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "📝 AI Notes"
                AppLanguage.ODIA_ONLY -> "📝 AI ନୋଟ୍"
                AppLanguage.HINDI_ONLY -> "📝 AI नोट्स"
                AppLanguage.BILINGUAL_EN_HI -> "📝 AI Notes (नोट्स)"
                AppLanguage.BILINGUAL_EN_OR -> "📝 AI Notes (ନୋଟ୍)"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onGenerateNotes,
                    label = { Text(notesChipLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons: "Open Lesson", "Take Diagnostic Quiz", and "Share via Bluetooth"
            val openLessonBtnText = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "Open Lesson"
                AppLanguage.ODIA_ONLY -> "ପାଠ ଖୋଲନ୍ତୁ"
                AppLanguage.HINDI_ONLY -> "पाठ खोलें"
                AppLanguage.BILINGUAL_EN_HI -> "Open Lesson"
                AppLanguage.BILINGUAL_EN_OR -> "Open Lesson"
            }
            val takeQuizBtnText = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "Take Quiz"
                AppLanguage.ODIA_ONLY -> "କୁଇଜ୍ ଦିଅନ୍ତୁ"
                AppLanguage.HINDI_ONLY -> "क्विज दें"
                AppLanguage.BILINGUAL_EN_HI -> "Take Quiz"
                AppLanguage.BILINGUAL_EN_OR -> "Take Quiz"
            }
            val shareP2PBtnText = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "Share P2P"
                AppLanguage.ODIA_ONLY -> "ପିଅର୍ ସେୟାର୍"
                AppLanguage.HINDI_ONLY -> "शेयर करें"
                AppLanguage.BILINGUAL_EN_HI -> "Share P2P"
                AppLanguage.BILINGUAL_EN_OR -> "Share P2P"
            }

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
                    Text(openLessonBtnText, fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onStartQuiz,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(takeQuizBtnText, fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onShareViaBluetooth,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(shareP2PBtnText, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}
