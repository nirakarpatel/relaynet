package com.nmiet.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.permissions.NearbyPermissionManager
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val profile by settingsViewModel.profile.collectAsState()
    val currentLanguage by settingsViewModel.currentLanguage.collectAsState()
    val syncStatus by settingsViewModel.syncStatus.collectAsState()
    val hasPermissions by settingsViewModel.hasPermissions.collectAsState()
    val isBluetoothEnabled by settingsViewModel.isBluetoothEnabled.collectAsState()

    val audioCacheSizeMb by settingsViewModel.audioCacheSizeMb.collectAsState()
    val diagramsCacheSizeMb by settingsViewModel.diagramsCacheSizeMb.collectAsState()
    val coreDataSizeMb by settingsViewModel.coreDataSizeMb.collectAsState()
    val pendingSyncAttemptsCount by settingsViewModel.pendingSyncAttemptsCount.collectAsState()
    val configuredApiKey by settingsViewModel.nvidiaApiKey.collectAsState()
    var apiKeyInput by remember(configuredApiKey) { mutableStateOf(configuredApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        settingsViewModel.refreshHardwareState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "App Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (syncStatus.isOnline) "🟢 Live Cloud Gateway" else "🔴 Simulated Disconnected Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (syncStatus.isOnline) SuccessGreen else MaterialTheme.colorScheme.error
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Profile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Section 1: Offline Simulation Switch (Evaluator / Judge Control)
            Text(
                text = "Evaluator Offline Simulation Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!syncStatus.isOnline) WarningAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (!syncStatus.isOnline) WarningAmber else PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline Simulation Switch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (!syncStatus.isOnline)
                                        "ACTIVE: App forced into disconnected state. Proving Room SQLite offline functionality."
                                    else
                                        "INACTIVE: Live cloud gateway connection active.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = !syncStatus.isOnline,
                            onCheckedChange = { settingsViewModel.toggleOfflineSimulationSwitch(forceOffline = it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Storage Cleaner (Adaptive Storage Manager)
            Text(
                text = "Adaptive Storage Manager",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Local Storage Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• Core Data & Quizzes:", fontSize = 13.sp)
                        Text(text = "${"%.1f".format(coreDataSizeMb)} MB", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• Generated Flowcharts & Diagrams:", fontSize = 13.sp)
                        Text(text = "${"%.1f".format(diagramsCacheSizeMb)} MB", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• Cached Audio:", fontSize = 13.sp)
                        Text(text = "${"%.1f".format(audioCacheSizeMb)} MB", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarningAmber)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Footprint: ${"%.1f".format(coreDataSizeMb + diagramsCacheSizeMb + audioCacheSizeMb)} MB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Clear Diagram Cache button
                            OutlinedButton(
                                onClick = {
                                    val freedMb = diagramsCacheSizeMb
                                    settingsViewModel.clearDiagramCache()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Diagram cache cleared (${"%.1f".format(freedMb)} MB freed)."
                                        )
                                    }
                                },
                                enabled = diagramsCacheSizeMb > 0,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Clear Diagrams", fontSize = 11.sp)
                            }

                            // Clear Audio Cache button
                            Button(
                                onClick = {
                                    val freedMb = audioCacheSizeMb
                                    settingsViewModel.clearAudioCache()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Audio cache cleared (${"%.1f".format(freedMb)} MB freed). Room SQLite quiz progress preserved."
                                        )
                                    }
                                },
                                enabled = audioCacheSizeMb > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Audio", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Clearing audio or diagram cache safely frees space while preserving learner Room SQLite quiz progress.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: Sync Queue Status
            Text(
                text = "Sync Queue Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "$pendingSyncAttemptsCount Quiz attempts pending sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (pendingSyncAttemptsCount > 0)
                                        "Awaiting cloud reconnection to transmit score logs"
                                    else
                                        "All diagnostic quiz attempts fully synchronized",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                settingsViewModel.syncPendingAttempts()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Diagnostic score records synchronized with cloud.")
                                }
                            },
                            modifier = Modifier
                                .wrapContentWidth()
                                .defaultMinSize(minHeight = 44.dp)
                                .padding(start = 8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            enabled = syncStatus.isOnline && pendingSyncAttemptsCount > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync Now",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 4: Learner Profile & Language Selection
            Text(
                text = "Learner Profile Settings (ଭାଷା ପସନ୍ଦ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Educational Board:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Board.entries.forEach { b ->
                            FilterChip(
                                selected = profile.board == b,
                                onClick = { settingsViewModel.updateBoard(b) },
                                label = { Text(b.code, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Class Grade (ଶ୍ରେଣୀ):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(6, 7, 8, 9, 10).forEach { grade ->
                            FilterChip(
                                selected = profile.classGrade == grade,
                                onClick = { settingsViewModel.updateGrade(grade) },
                                label = { Text("Class $grade", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Preferred Language (ଭାଷା ପସନ୍ଦ / भाषा चयन):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languageOptions = listOf(
                            AppLanguage.ENGLISH_ONLY to "English",
                            AppLanguage.ODIA_ONLY to "ଓଡ଼ିଆ (Odia)",
                            AppLanguage.HINDI_ONLY to "हिंदी (Hindi)",
                            AppLanguage.BILINGUAL_EN_HI to "English + हिंदी",
                            AppLanguage.BILINGUAL_EN_OR to "English + ଓଡ଼ିଆ"
                        )

                        languageOptions.forEach { (lang, label) ->
                            val isSelected = currentLanguage == lang
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.updateLanguage(lang) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = if (isSelected)
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 5: Bluetooth Hardware & Permission Checks
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Bluetooth Mesh Hardware Readiness", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bluetooth Adapter Radio", fontSize = 12.sp)
                        }
                        SettingsStatusPill(isPositive = isBluetoothEnabled)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BLUETOOTH_CONNECT & SCAN Permissions", fontSize = 12.sp)
                        }
                        SettingsStatusPill(isPositive = hasPermissions)
                    }

                    if (!hasPermissions) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { permissionLauncher.launch(NearbyPermissionManager.getRequiredPermissions()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Request Nearby Bluetooth Permissions", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pilot Pack Reset Action (With AlertDialog Confirmation)
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset & Reload Class 9 Science Pilot Pack")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 6: Cloud AI & NVIDIA NIM API Key Configuration
            Text(
                text = "Cloud AI & NVIDIA NIM API Key",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NVIDIA NIM / Cloud API Key",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Add your API key manually for Nemotron-3-Super reasoning, Llama-3.2 Vision OCR, and FLUX.2 diagram generation.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API Key (nvapi-...)") },
                        placeholder = { Text("nvapi-xxxxxxxxxxxxxxxxxxxxxxxx") },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isApiKeyVisible) "Hide Key" else "Show Key"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (configuredApiKey.isNotBlank())
                                "Status: Active (${configuredApiKey.take(7)}••••${configuredApiKey.takeLast(4)})"
                            else
                                "Status: Offline / Fallback Mode",
                            fontSize = 11.sp,
                            color = if (configuredApiKey.isNotBlank()) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (configuredApiKey.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = ""
                                settingsViewModel.clearNvidiaApiKey()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("API Key cleared. Reverted to offline fallback.")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = configuredApiKey.isNotBlank() || apiKeyInput.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear")
                        }

                        Button(
                            onClick = {
                                settingsViewModel.saveNvidiaApiKey(apiKeyInput)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("API Key saved successfully for NVIDIA NIM services.")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = apiKeyInput.isNotBlank() && apiKeyInput != configuredApiKey
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Key")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Blueprint Footer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "RELAYNET • Mobile Architecture Blueprint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Target SDK 36 • Min SDK 26 • Kotlin 2.2 • Jetpack Compose Material 3 • Room SQLite Persistence • Bluetooth RFCOMM",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom padding to clear NavigationBar and floating voice assistant mic button
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Pilot Pack Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reload Pilot Pack?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to reload the Class 9 Photosynthesis pack? This will re-initialize the baseline SQLite catalog.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.resetToDemoCatalog()
                        showResetDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Class 9 Science Pilot Pack re-initialized.")
                        }
                    }
                ) {
                    Text("Confirm Reload", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsStatusPill(isPositive: Boolean) {
    val bgColor = if (isPositive) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    val color = if (isPositive) SuccessGreen else MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPositive) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isPositive) "Ready" else "Disabled",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
