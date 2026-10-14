package com.nmiet.app.ui.screens.nearbyshare

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.DemoModeBadge
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyShareScreen(
    viewModel: NearbyShareViewModel,
    onNavigateToCurriculum: () -> Unit
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val shareDirection by viewModel.shareDirection.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val isDemoMode by viewModel.useDemoMode.collectAsState()
    val packs by viewModel.learningPacks.collectAsState()
    val selectedPackIds by viewModel.selectedPackIds.collectAsState()

    val connectedPeerName = (transferState as? TransferState.Connected)?.connectedPeer?.name
        ?: (transferState as? TransferState.Transferring)?.peer?.name

    val screenTitle = when (currentLanguage) {
        AppLanguage.ENGLISH_ONLY -> "P2P ShikshaMesh"
        AppLanguage.ODIA_ONLY -> "ପିଅର୍ ଶିକ୍ଷାମେସ୍"
        AppLanguage.HINDI_ONLY -> "पी2पी शिक्षामेश"
        AppLanguage.BILINGUAL_EN_HI -> "P2P ShikshaMesh • शिक्षामेश"
        AppLanguage.BILINGUAL_EN_OR -> "P2P ShikshaMesh • ଶିକ୍ଷାମେସ୍"
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                isOnline = false,
                connectedPeerName = connectedPeerName ?: if (isDemoMode) "Node-02" else null,
                isDemoMode = isDemoMode
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

                val isSending = shareDirection == ShareDirection.SEND

                // Segmented Mode Toggle:
                // Tab 1: "Send Packs" / "ପାଠ ପଠାନ୍ତୁ" / "पाठ भेजें"
                // Tab 2: "Receive Packs" / "ଗ୍ରହଣ କରନ୍ତୁ" / "पाठ प्राप्त करें"
                val sendPacksText = when (currentLanguage) {
                    AppLanguage.ENGLISH_ONLY -> "Send Packs"
                    AppLanguage.ODIA_ONLY -> "ପାଠ ପଠାନ୍ତୁ"
                    AppLanguage.HINDI_ONLY -> "पाठ भेजें"
                    AppLanguage.BILINGUAL_EN_HI -> "Send (पाठ भेजें)"
                    AppLanguage.BILINGUAL_EN_OR -> "Send (ପାଠ ପଠାନ୍ତୁ)"
                }
                val receivePacksText = when (currentLanguage) {
                    AppLanguage.ENGLISH_ONLY -> "Receive Packs"
                    AppLanguage.ODIA_ONLY -> "ଗ୍ରହଣ କରନ୍ତୁ"
                    AppLanguage.HINDI_ONLY -> "पाठ प्राप्त करें"
                    AppLanguage.BILINGUAL_EN_HI -> "Receive (प्राप्त करें)"
                    AppLanguage.BILINGUAL_EN_OR -> "Receive (ଗ୍ରହଣ କରନ୍ତୁ)"
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = isSending,
                        onClick = { viewModel.setShareDirection(ShareDirection.SEND) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {},
                        label = {
                            Text(
                                text = sendPacksText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )

                    SegmentedButton(
                        selected = !isSending,
                        onClick = { viewModel.setShareDirection(ShareDirection.RECEIVE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {},
                        label = {
                            Text(
                                text = receivePacksText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Demo Simulation Mode Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDemoMode) "Demo Simulation Mode" else "Real Bluetooth RFCOMM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isDemoMode) DemoModeBadge()
                            }
                            Text(
                                text = if (isDemoMode)
                                    "Simulates full Bluetooth mesh transfer with progress & SHA-256 validation for presentation."
                                else
                                    "Connects directly over Bluetooth RFCOMM channel with nearby Android phones.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { viewModel.setDemoMode(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real-time Transfer Modal with 4-Step State Machine
            item {
                when (val state = transferState) {
                    is TransferState.Connecting -> {
                        TransferStateMachineCard(
                            stepName = "State 1: Discovery & Handshake",
                            statusMessage = "Connecting to ${state.targetPeer.name} over RFCOMM...",
                            progressPercent = 15,
                            checksumStatus = "Comparing Bluetooth Handshake Tokens..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Connected -> {
                        TransferStateMachineCard(
                            stepName = "State 2: Manifest Negotiation",
                            statusMessage = "Negotiating manifest hashes with ${state.connectedPeer.name}...",
                            progressPercent = 35,
                            checksumStatus = "Comparing SHA-256 hashes... Valid"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Transferring -> {
                        TransferStateMachineCard(
                            stepName = "State 3: Chunked Transfer",
                            statusMessage = "Progress: ${state.progressPercent}% • 120 KB/s via Bluetooth RFCOMM",
                            progressPercent = state.progressPercent,
                            checksumStatus = "Chunk ${state.progressPercent / 10} verified • ${state.currentItemTitle}",
                            onCancel = { viewModel.cancelTransfer() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Success -> {
                        SuccessPackCard(
                            state = state,
                            onDismiss = { viewModel.resetTransfer() },
                            onOpenCurriculum = onNavigateToCurriculum
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Error -> {
                        ErrorPackCard(
                            errorMessage = state.errorMessage,
                            canRetry = state.canRetry,
                            onRetry = { viewModel.startScanning() },
                            onDismiss = { viewModel.resetTransfer() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    else -> {}
                }
            }

            // Radar-Style Scanning Card & Discovered Peers List
            item {
                val isScanning = transferState is TransferState.Scanning

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = "Discovered Mesh Peers",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isScanning) "Scanning 2.4GHz Bluetooth band..." else "Ready to scan for nearby students",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Radar Action Button: "Scan Radar" / "ସ୍କାନ୍ ରାଡାର୍" / "स्कैन रडार"
                            val radarButtonText = if (isScanning) {
                                when (currentLanguage) {
                                    AppLanguage.ENGLISH_ONLY -> "Stop"
                                    AppLanguage.ODIA_ONLY -> "ବନ୍ଦ କରନ୍ତୁ"
                                    AppLanguage.HINDI_ONLY -> "रोकें"
                                    AppLanguage.BILINGUAL_EN_HI -> "Stop (रोकें)"
                                    AppLanguage.BILINGUAL_EN_OR -> "Stop (ବନ୍ଦ)"
                                }
                            } else {
                                when (currentLanguage) {
                                    AppLanguage.ENGLISH_ONLY -> "Scan Radar"
                                    AppLanguage.ODIA_ONLY -> "ସ୍କାନ୍ ରାଡାର୍"
                                    AppLanguage.HINDI_ONLY -> "स्कैन रडार"
                                    AppLanguage.BILINGUAL_EN_HI -> "Scan (स्कैन)"
                                    AppLanguage.BILINGUAL_EN_OR -> "Scan (ସ୍କାନ୍)"
                                }
                            }

                            Button(
                                onClick = { if (isScanning) viewModel.stopScanning() else viewModel.startScanning() },
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .defaultMinSize(minHeight = 44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else PrimaryBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScanning) Icons.Default.Stop else Icons.AutoMirrored.Filled.BluetoothSearching,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = radarButtonText,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        if (isScanning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            RadarPulseAnimationCard()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Peers List
            val peers = (transferState as? TransferState.Scanning)?.discoveredPeers ?: emptyList()
            if (peers.isEmpty()) {
                item {
                    val isScanning = transferState is TransferState.Scanning
                    EmptyView(
                        title = if (isScanning) "Searching for Nearby Peers..." else "No Peers Connected",
                        subtitle = if (isScanning)
                            "Listening for nearby student nodes (e.g., Node-02)..."
                        else
                            "Tap 'Scan Radar' to discover nearby students or school hubs.",
                        icon = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Devices,
                        actionButtonText = if (!isScanning) "Start Scanning" else null,
                        onActionClick = { viewModel.startScanning() },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(peers, key = { it.id }) { peer ->
                    PeerLearningDeviceCard(
                        peer = peer,
                        isDemoMode = isDemoMode,
                        shareDirection = shareDirection,
                        onSendClick = { viewModel.sendToPeer(peer) },
                        onSimulateReceiveClick = { viewModel.simulateIncomingTransferFromPeer(peer) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Manifest Selection Checklist
            if (shareDirection == ShareDirection.SEND) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Manifest Selection Checklist (ପାଠ ବାଛନ୍ତୁ)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Check learning packs from local Room SQLite database to include in transfer manifest.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(packs, key = { "manifest-${it.packId}" }) { pack ->
                    val isChecked = selectedPackIds.contains(pack.packId)
                    val footprintText = "%.1f MB".format(pack.sizeBytes / 1_000_000.0)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.togglePackSelection(pack.packId) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = pack.titleOdia, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                Text(text = pack.titleEnglish, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "Footprint: $footprintText • SHA-256: ${pack.checksumSha256.take(8)}...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
fun TransferStateMachineCard(
    stepName: String,
    statusMessage: String,
    progressPercent: Int,
    checksumStatus: String,
    onCancel: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = stepName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }

                if (onCancel != null) {
                    IconButton(onClick = onCancel, modifier = Modifier.size(22.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = statusMessage, fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = checksumStatus,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PeerLearningDeviceCard(
    peer: PeerDevice,
    isDemoMode: Boolean,
    shareDirection: ShareDirection,
    onSendClick: () -> Unit,
    onSimulateReceiveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = peer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${peer.address} • Signal: ${peer.signalStrengthRssi} dBm (RSSI)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (shareDirection == ShareDirection.SEND) {
                    Button(
                        onClick = onSendClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Manifest", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSimulateReceiveClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request & Receive Pack", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarPulseAnimationCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PrimaryBlue.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(70.dp)) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = 1.0f - pulseScale),
                radius = (size.minDimension / 2) * pulseScale,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        Text(text = "Scanning 2.4GHz RFCOMM...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
    }
}

@Composable
fun SuccessPackCard(state: TransferState.Success, onDismiss: () -> Unit, onOpenCurriculum: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "State 4: Validation Complete (Done!)", fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "SHA-256 checksum verified successfully. ${state.message}", fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCurriculum,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("View Lessons in Curriculum", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ErrorPackCard(errorMessage: String, canRetry: Boolean, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Transfer Interrupted", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = errorMessage, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canRetry) {
                    Button(onClick = onRetry) { Text("Retry", fontSize = 12.sp) }
                }
                OutlinedButton(onClick = onDismiss) { Text("Dismiss", fontSize = 12.sp) }
            }
        }
    }
}
