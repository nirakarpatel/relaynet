package com.nmiet.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.nmiet.app.R
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.permissions.NearbyPermissionManager
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

/**
 * Screen 1: Connectivity & Permissions Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectivityPermissionsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val isBluetoothEnabled by settingsViewModel.isBluetoothEnabled.collectAsState()
    val hasPermissions by settingsViewModel.hasPermissions.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        settingsViewModel.refreshHardwareState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connectivity & Permissions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Bluetooth Mesh Hardware Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Bluetooth Radio Adapter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Required for P2P lesson broadcast", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        SettingsStatusPill(isPositive = isBluetoothEnabled)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("BLUETOOTH_CONNECT & SCAN", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Runtime nearby device discovery", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        SettingsStatusPill(isPositive = hasPermissions)
                    }

                    if (!hasPermissions) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { permissionLauncher.launch(NearbyPermissionManager.getRequiredPermissions()) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Nearby Bluetooth Permissions")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "P2P RFCOMM Discovery Architecture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureCheckRow(
                        icon = Icons.Default.CellTower,
                        title = "Zero-Internet RFCOMM Sockets",
                        description = "Direct peer-to-peer transport using UUID 00001101-0000-1000-8000-00805F9B34FB."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureCheckRow(
                        icon = Icons.Default.Radio,
                        title = "Asynchronous Stream Slicing",
                        description = "Learning pack payloads chunked into 1024-byte packets with CRC32 frame checksums."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureCheckRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Automatic Peer Handshake",
                        description = "Discovery radar validates student pack manifests before initiating file ingestion."
                    )
                }
            }

            // Bottom padding to clear NavigationBar and floating voice assistant mic button
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

/**
 * Screen 2: Offline Content Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineContentScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val audioCacheSizeMb by settingsViewModel.audioCacheSizeMb.collectAsState()
    val diagramsCacheSizeMb by settingsViewModel.diagramsCacheSizeMb.collectAsState()
    val coreDataSizeMb by settingsViewModel.coreDataSizeMb.collectAsState()
    val pendingSyncAttemptsCount by settingsViewModel.pendingSyncAttemptsCount.collectAsState()
    val syncStatus by settingsViewModel.syncStatus.collectAsState()
    val currentLanguage by settingsViewModel.currentLanguage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Offline Content Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Cached Curriculum Packs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Pack 1: Photosynthesis
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
                        Column {
                            Text("Photosynthesis & Plant Nutrition", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val packSubtitle = when (currentLanguage) {
                                AppLanguage.ENGLISH_ONLY -> "Life Science • Class 9 Science"
                                AppLanguage.ODIA_ONLY -> "ଆଲୋକ ଶ୍ଳେଷଣ • Class 9 Science"
                                AppLanguage.HINDI_ONLY -> "प्रकाश संश्लेषण • Class 9 Science"
                                AppLanguage.BILINGUAL_EN_HI -> "Photosynthesis (प्रकाश संश्लेषण) • Class 9 Science"
                                AppLanguage.BILINGUAL_EN_OR -> "ଆଲୋକ ଶ୍ଳେଷଣ • Class 9 Science"
                            }
                            Text(packSubtitle, fontSize = 12.sp, color = PrimaryBlue)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Downloaded",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "3 Sections • 10 Diagnostic MCQs • Multilingual Audio",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Footprint Breakdown
            Text(
                text = "Local Storage Footprint",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Core Data & Quizzes:", fontSize = 13.sp)
                        Text("${"%.1f".format(coreDataSizeMb)} MB", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Generated Flowcharts & Diagrams:", fontSize = 13.sp)
                        Text("${"%.1f".format(diagramsCacheSizeMb)} MB", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Cached Audio:", fontSize = 13.sp)
                        Text("${"%.1f".format(audioCacheSizeMb)} MB", fontWeight = FontWeight.Bold, color = WarningAmber)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total: ${"%.1f".format(coreDataSizeMb + diagramsCacheSizeMb + audioCacheSizeMb)} MB", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val freed = diagramsCacheSizeMb
                                    settingsViewModel.clearDiagramCache()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Diagram cache cleared (${"%.1f".format(freed)} MB freed).")
                                    }
                                },
                                enabled = diagramsCacheSizeMb > 0,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Clear Diagrams", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val freed = audioCacheSizeMb
                                    settingsViewModel.clearAudioCache()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Audio cache cleared (${"%.1f".format(freed)} MB freed).")
                                    }
                                },
                                enabled = audioCacheSizeMb > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Audio", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pending Sync Queue
            Text(
                text = "Diagnostic Sync Queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$pendingSyncAttemptsCount Quiz attempts pending", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            if (syncStatus.isOnline) "Ready to sync with school gateway" else "Awaiting cloud gateway reconnection",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            settingsViewModel.syncPendingAttempts()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Sync completed successfully.")
                            }
                        },
                        enabled = syncStatus.isOnline && pendingSyncAttemptsCount > 0,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 12.sp)
                    }
                }
            }

            // Bottom padding to clear NavigationBar and floating voice assistant mic button
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

/**
 * Screen 3: Privacy & Security Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "RelayNet Offline Privacy Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RelayNet is engineered from the ground up for rural classrooms with zero third-party telemetry, ad networks, or personal data tracking.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureCheckRow(
                        icon = Icons.Default.Shield,
                        title = "SHA-256 Manifest Cryptographic Integrity",
                        description = "Every incoming P2P lesson pack is cryptographically validated using SHA-256 hashes against trusted curriculum publisher keys."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureCheckRow(
                        icon = Icons.Default.Lock,
                        title = "Zero Personal Data Collection",
                        description = "Student quiz records and diagnostic scores remain inside local Room SQLite tables on device and never transmit without explicit user sync."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureCheckRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Sandboxed RFCOMM Transfer",
                        description = "Bluetooth RFCOMM data transfers execute inside isolated buffer sandboxes preventing arbitrary code execution or unauthorized access."
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Screen 4: About RelayNet Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutRelaynetScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About RelayNet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(84.dp)
                            .border(1.5.dp, PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_relaynet_logo),
                            contentDescription = "RelayNet Official Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "RelayNet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Version 1.0.0 (Build 20261014)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "NEXTECH 2.0 / NIRMIT 2026 • Team Thinkory",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Mission Statement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RelayNet democratizes digital learning across rural Odisha and secondary schools with unreliable connectivity by providing an autonomous, offline-first educational platform with local AI tutoring, Bluetooth peer mesh synchronization, and bilingual regional language support.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Technical Architecture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TechSpecRow("Target Android SDK", "36 (Android 16 / Upside Down Cake+)")
                    TechSpecRow("Minimum SDK", "26 (Android 8.0 Oreo)")
                    TechSpecRow("UI Toolkit", "Jetpack Compose + Material 3")
                    TechSpecRow("Language", "Kotlin 2.2 + Coroutines/StateFlow")
                    TechSpecRow("Offline Persistence", "Room SQLite Database + DataStore")
                    TechSpecRow("P2P Mesh", "Bluetooth RFCOMM Sockets (IEEE 802.15.1)")
                    TechSpecRow("AI Tutor Engine", "Offline Structured Heuristic + Gemini Online API")
                    TechSpecRow("Speech Synthesis", "Android System TextToSpeech (Odia/Hindi)")
                }
            }

            // Bottom padding to clear NavigationBar and floating voice assistant mic button
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun TechSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun FeatureCheckRow(icon: ImageVector, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}
