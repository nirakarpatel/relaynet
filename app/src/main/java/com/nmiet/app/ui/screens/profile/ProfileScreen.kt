package com.nmiet.app.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.navigation.Screen
import com.nmiet.app.permissions.NearbyPermissionManager
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun ProfileScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val profile by settingsViewModel.profile.collectAsState()
    val syncStatus by settingsViewModel.syncStatus.collectAsState()
    val isBluetoothEnabled by settingsViewModel.isBluetoothEnabled.collectAsState()
    val hasNearbyPermissions by settingsViewModel.hasPermissions.collectAsState()
    val audioCacheSizeMb by settingsViewModel.audioCacheSizeMb.collectAsState()
    val diagramsCacheSizeMb by settingsViewModel.diagramsCacheSizeMb.collectAsState()
    val coreDataSizeMb by settingsViewModel.coreDataSizeMb.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editedName by remember(profile.name) { mutableStateOf(profile.name) }

    // Bluetooth detailed permission check
    val hasScanAndConnect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    } else {
        hasNearbyPermissions
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Student Profile & Identity",
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Student Avatar & Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Avatar Surface with Student Initials
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = PrimaryBlue,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = profile.name.split(" ")
                                    .filter { it.isNotBlank() }
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifEmpty { "S" }

                                Text(
                                    text = initials,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editedName = profile.name
                                        showEditProfileDialog = true
                                    }
                            ) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        editedName = profile.name
                                        showEditProfileDialog = true
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Text(
                                text = "ID: ${profile.studentId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Class & Board pill tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Class ${profile.classGrade} • ${profile.board.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Learning Medium Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Instruction Medium:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val mediumLabel = when (profile.preferredLanguage) {
                            AppLanguage.ODIA_ONLY -> "ଓଡ଼ିଆ (Odia Medium)"
                            AppLanguage.HINDI_ONLY -> "हिंदी (Hindi Medium)"
                            AppLanguage.ENGLISH_ONLY -> "English (English Medium)"
                            AppLanguage.BILINGUAL_EN_HI -> "Bilingual (English + हिंदी)"
                            AppLanguage.BILINGUAL_EN_OR -> "Bilingual (English + ଓଡ଼ିଆ)"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = mediumLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. System Health & Runtime Status Summary Cards
            Text(
                text = "System Health & Runtime Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Cards (3 horizontal cards)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Card A: Cloud Gateway Status
                val isOnline = syncStatus.isOnline
                HealthStatusCard(
                    title = "Cloud Gateway Status",
                    statusText = if (isOnline) "🟢 Cloud Gateway Active" else "🔴 Offline (Room SQLite Active)",
                    subtitle = if (isOnline) "Connected to online AI cluster & diagnostic sync" else "Zero-connectivity fallback active with full local catalog",
                    icon = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                    iconTint = if (isOnline) SuccessGreen else MaterialTheme.colorScheme.error,
                    containerColor = if (isOnline) SuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                )

                // Card B: Bluetooth Radio & Permissions Status
                val btStatusText = when {
                    !hasScanAndConnect -> "🔴 Permissions Needed"
                    !isBluetoothEnabled -> "🟡 Radio Off"
                    else -> "🟢 Bluetooth Ready"
                }
                val btSubtitle = when {
                    !hasScanAndConnect -> "Grant BLUETOOTH_CONNECT & SCAN to enable peer mesh"
                    !isBluetoothEnabled -> "Turn on device Bluetooth adapter for P2P transfers"
                    else -> "RFCOMM socket listener ready for local classroom broadcast"
                }
                val btTint = when {
                    !hasScanAndConnect -> MaterialTheme.colorScheme.error
                    !isBluetoothEnabled -> WarningAmber
                    else -> SuccessGreen
                }
                HealthStatusCard(
                    title = "Bluetooth Radio & Permissions",
                    statusText = btStatusText,
                    subtitle = btSubtitle,
                    icon = Icons.Default.Bluetooth,
                    iconTint = btTint,
                    containerColor = btTint.copy(alpha = 0.08f),
                    onClick = { navController.navigate(Screen.ConnectivityPermissions.route) }
                )

                // Card C: Offline Storage Footprint
                val totalFootprintMb = "%.1f".format(coreDataSizeMb + diagramsCacheSizeMb + audioCacheSizeMb)
                HealthStatusCard(
                    title = "Offline Storage Footprint",
                    statusText = "💾 $totalFootprintMb MB Total",
                    subtitle = "${"%.1f".format(coreDataSizeMb)} MB Core • ${"%.1f".format(diagramsCacheSizeMb)} MB Diagrams • ${"%.1f".format(audioCacheSizeMb)} MB Audio",
                    icon = Icons.Default.Storage,
                    iconTint = PrimaryBlue,
                    containerColor = PrimaryBlue.copy(alpha = 0.08f),
                    onClick = { navController.navigate(Screen.OfflineContent.route) }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Menu Navigation List
            Text(
                text = "Preferences & System Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Item 1: App Settings
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        iconTint = PrimaryBlue,
                        title = "App Settings",
                        subtitle = "Language preferences, offline simulator, cache cleaner, and dev tools.",
                        onClick = { navController.navigate(Screen.AppSettings.route) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 2: Connectivity & Permissions
                    ProfileMenuItem(
                        icon = Icons.Default.CellTower,
                        iconTint = SuccessGreen,
                        title = "Connectivity & Permissions",
                        subtitle = "Bluetooth adapter state, scan permissions, and RFCOMM discovery.",
                        onClick = { navController.navigate(Screen.ConnectivityPermissions.route) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 3: Offline Content Management
                    ProfileMenuItem(
                        icon = Icons.Default.Storage,
                        iconTint = WarningAmber,
                        title = "Offline Content Management",
                        subtitle = "Manage cached Class 9 packs, clean audio cache, and check sync queue.",
                        onClick = { navController.navigate(Screen.OfflineContent.route) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 4: Privacy & Security
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF6750A4),
                        title = "Privacy & Security",
                        subtitle = "P2P manifest verification, SHA-256 integrity, zero personal tracking.",
                        onClick = { navController.navigate(Screen.PrivacySecurity.route) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 5: About Relaynet
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        iconTint = PrimaryBlue,
                        title = "About Relaynet",
                        subtitle = "Version 1.0 • NEXTECH 2.0 / NIRMIT 2026 • Team Thinkory.",
                        onClick = { navController.navigate(Screen.AboutRelaynet.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Optional Sign Out / Profile Reset Action
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out / Reset Learner Session",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bottom padding to clear NavigationBar and floating voice assistant mic button
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Dialog: Edit Profile Name
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Name", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Update the student name displayed on your offline certificates, diagnostic reports, and peer mesh badge.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            settingsViewModel.updateLearnerName(editedName.trim())
                        }
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Sign Out / Reset Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out / Reset Session?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will reset your active student session back to baseline pilot credentials while preserving offline SQLite learning packs.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.resetLearnerProfile()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Reset Session", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HealthStatusCard(
    title: String,
    statusText: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
