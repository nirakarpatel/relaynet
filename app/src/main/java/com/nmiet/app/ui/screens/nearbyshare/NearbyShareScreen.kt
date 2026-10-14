package com.nmiet.app.ui.screens.nearbyshare

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.permissions.NearbyPermissionManager
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.DemoModeBadge
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.components.LocalizedText
import com.nmiet.app.ui.components.MultilingualString
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyShareScreen(
    viewModel: NearbyShareViewModel,
    onNavigateToCurriculum: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val shareDirection by viewModel.shareDirection.collectAsState()
    val selectedModelType by viewModel.selectedModelType.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val isDemoMode by viewModel.useDemoMode.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val packs by viewModel.learningPacks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedPackIds by viewModel.selectedPackIds.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val peers by viewModel.discoveredPeers.collectAsState()

    var hasPermissions by remember {
        mutableStateOf(NearbyPermissionManager.hasNearbyPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermissions = NearbyPermissionManager.hasNearbyPermissions(context)
        if (hasPermissions) {
            viewModel.startScanning()
        }
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Returned from discoverable request
    }

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
                connectedPeerName = connectedPeerName ?: if (isDemoMode) "Demo-Node-02" else null,
                isDemoMode = isDemoMode
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

                // Transport Protocol Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDemoMode) MaterialTheme.colorScheme.surfaceVariant else PrimaryBlue.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Transport Protocol",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (selectedProtocol == TransportProtocol.DEMO_SIMULATION) {
                                DemoModeBadge()
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedProtocol == TransportProtocol.GOOGLE_NEARBY_CONNECTIONS,
                                onClick = { viewModel.selectProtocol(TransportProtocol.GOOGLE_NEARBY_CONNECTIONS) },
                                label = { Text("Nearby (P2P)", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedProtocol == TransportProtocol.BLUETOOTH_RFCOMM,
                                onClick = { viewModel.selectProtocol(TransportProtocol.BLUETOOTH_RFCOMM) },
                                label = { Text("Bluetooth", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedProtocol == TransportProtocol.DEMO_SIMULATION,
                                onClick = { viewModel.selectProtocol(TransportProtocol.DEMO_SIMULATION) },
                                label = { Text("Demo Mock", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (selectedProtocol) {
                                TransportProtocol.GOOGLE_NEARBY_CONNECTIONS ->
                                    "Google Nearby Connections: High-speed Star topology offline P2P."
                                TransportProtocol.BLUETOOTH_RFCOMM ->
                                    "Bluetooth RFCOMM: Native direct socket connection without Play Services."
                                TransportProtocol.DEMO_SIMULATION ->
                                    "Demo Simulation: Offline test run with simulated progress & SHA-256 validation."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real-time Transfer State Machine Card
            item {
                when (val state = transferState) {
                    is TransferState.Connecting -> {
                        TransferStateMachineCard(
                            stepName = "Step 1: RFCOMM Handshake",
                            statusMessage = "Connecting to ${state.targetPeer.name} over Bluetooth RFCOMM channel...",
                            progressPercent = 15,
                            checksumStatus = "Negotiating RFCOMM socket & UUID..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Connected -> {
                        TransferStateMachineCard(
                            stepName = "Step 2: Manifest & SHA-256 Setup",
                            statusMessage = "Connected to ${state.connectedPeer.name}. Preparing chunk stream...",
                            progressPercent = 35,
                            checksumStatus = "Computing payload SHA-256 checksum..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Transferring -> {
                        TransferStateMachineCard(
                            stepName = "Step 3: Real-Time Chunk Streaming",
                            statusMessage = if (state.isOutgoing)
                                "Sending to ${state.peer.name} • ${state.progressPercent}%"
                            else
                                "Receiving from ${state.peer.name} • ${state.progressPercent}%",
                            progressPercent = state.progressPercent,
                            checksumStatus = state.currentItemTitle,
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
                    is TransferState.AuthVerification -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🔐 Nearby Connection Request",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Peer: ${state.peer.name}\nVerify matching 4-digit authentication code on both devices:",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryBlue)
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = state.authenticationDigits,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        letterSpacing = 4.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.rejectConnection(state.peer) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject")
                                    }
                                    Button(
                                        onClick = { viewModel.acceptConnection(state.peer) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Text("Accept & Connect")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.ManifestReceived -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Content Manifest Received from ${state.peer.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Discovered ${state.manifest.items.size} total items (${state.missingOrUpdatableItems.size} new/updatable for your grade).",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                state.missingOrUpdatableItems.forEach { item ->
                                    Text("• ${item.title} (${(item.sizeBytes / 1_000_000.0).let { "%.1f".format(it) }} MB, v${item.version})", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.sendToPeer(state.peer) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text("Send Selected Learning Pack")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    else -> {}
                }
            }

            // Radar Scanning Card & Discovered Peers
            item {
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
                                        text = if (shareDirection == ShareDirection.SEND) "Peer Discovery Radar" else "Receiver Mesh Listener",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isScanning) "Searching for RelayNet app devices..." else "Discovered nearby students with RelayNet app",
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
                                onClick = {
                                    if (isScanning) {
                                        viewModel.stopScanning()
                                    } else {
                                        if (!hasPermissions && !isDemoMode) {
                                            permissionLauncher.launch(NearbyPermissionManager.getRequiredPermissions())
                                        } else {
                                            viewModel.startScanning()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .defaultMinSize(minHeight = 44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else PrimaryBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScanning) Icons.Default.Stop else Icons.AutoMirrored.Filled.BluetoothSearching,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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

            // Discovered Peers List
            if (peers.isEmpty()) {
                item {
                    EmptyView(
                        title = if (isScanning) "Scanning for Nearby RelayNet Apps..." else "No RelayNet Peers Found",
                        subtitle = if (isScanning)
                            "Filtering 2.4GHz spectrum for devices actively running RelayNet app..."
                        else
                            "Make sure Bluetooth is enabled and RelayNet is open on the other device.",
                        icon = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Devices,
                        actionButtonText = if (!isScanning) "Scan Radar" else null,
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

            // Model Selection Section (Curriculum Learning Packs OR AI Generated Notes)
            if (shareDirection == ShareDirection.SEND) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    LocalizedText(
                        text = MultilingualString(
                            english = "Manifest Selection Checklist",
                            odia = "ପାଠ ତାଲିକା ଚୟନ",
                            hindi = "पाठ चयन सूची"
                        ),
                        currentLanguageFlow = viewModel.currentLanguage,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Choose whether to transfer full Curriculum Learning Packs or AI Generated Notes models.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedModelType == ShareModelType.LEARNING_PACK,
                            onClick = { viewModel.setModelType(ShareModelType.LEARNING_PACK) },
                            label = { Text("📚 Learning Packs (${packs.size})", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )

                        FilterChip(
                            selected = selectedModelType == ShareModelType.AI_GENERATED_NOTE,
                            onClick = { viewModel.setModelType(ShareModelType.AI_GENERATED_NOTE) },
                            label = { Text("📝 AI Notes (${notes.size})", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (selectedModelType == ShareModelType.LEARNING_PACK) {
                    items(packs, key = { "pack-${it.packId}" }) { pack ->
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
                                    .clickable { viewModel.togglePackSelection(pack.packId) }
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
                                        text = "Grade ${pack.classGrade} • $footprintText • SHA-256: ${pack.checksumSha256.take(8)}...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (notes.isEmpty()) {
                        item {
                            Text(
                                text = "No generated notes in library. Generate one in 'Notes Generator' or tap to send demo note.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(notes, key = { "note-${it.noteId}" }) { note ->
                            val isSelected = selectedNoteId == note.noteId

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectNote(note.noteId) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = note.titleEnglish, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                        if (note.titleOdia.isNotBlank()) {
                                            Text(text = note.titleOdia, fontSize = 12.sp)
                                        }
                                        Text(
                                            text = "Topic: ${note.topicName} • ${note.keyConcepts.size} concepts • ${note.examTips.size} exam tips",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isDemoMode) "Send (Demo)" else "Send Realtime Bluetooth", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSimulateReceiveClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isDemoMode) "Simulate Receive" else "Pull Model via Bluetooth", fontSize = 11.sp)
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
fun SuccessPackCard(
    state: TransferState.Success,
    onDismiss: () -> Unit,
    onOpenCurriculum: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transfer Successful & Verified",
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = state.message, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCurriculum,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Open in Curriculum", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ErrorPackCard(
    errorMessage: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transfer Error",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = errorMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canRetry) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry", fontSize = 11.sp)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss", fontSize = 11.sp)
                }
            }
        }
    }
}
