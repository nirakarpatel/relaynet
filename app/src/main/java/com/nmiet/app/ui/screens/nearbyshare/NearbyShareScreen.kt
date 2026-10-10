package com.nmiet.app.ui.screens.nearbyshare

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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.DemoModeBadge
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen

@Composable
fun NearbyShareScreen(
    viewModel: NearbyShareViewModel,
    onNavigateToCurriculum: () -> Unit
) {
    val transferState by viewModel.transferState.collectAsState()
    val isDemoMode by viewModel.useDemoMode.collectAsState()
    val packs by viewModel.learningPacks.collectAsState()
    val selectedPackId by viewModel.selectedPackId.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "P2P Pack Share",
                isOnline = false,
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

                // Transport Mode Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDemoMode) "Demo Simulation Mode" else "Real Bluetooth RFCOMM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isDemoMode) DemoModeBadge()
                            }
                            Text(
                                text = if (isDemoMode)
                                    "Simulates two-device mesh transfer with progress & SHA-256 validation."
                                else
                                    "Transfers learning pack directly between two physical Android phones over Bluetooth.",
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

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Transfer Status Card
            item {
                when (val state = transferState) {
                    is TransferState.Transferring -> {
                        TransferringPackCard(state = state, onCancel = { viewModel.cancelTransfer() })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TransferState.Connecting -> {
                        ConnectingPackCard(peer = state.targetPeer)
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

            // Discovered Peers Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Nearby Learners & Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val isScanning = transferState is TransferState.Scanning
                        Text(
                            text = if (isScanning) "Listening for nearby student devices..." else "Ready to discover peers over Bluetooth",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isScanning = transferState is TransferState.Scanning
                    Button(
                        onClick = {
                            if (isScanning) viewModel.stopScanning() else viewModel.startScanning()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Stop else Icons.AutoMirrored.Filled.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isScanning) "Stop" else "Scan", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            val peers = (transferState as? TransferState.Scanning)?.discoveredPeers ?: emptyList()
            if (peers.isEmpty()) {
                item {
                    val isScanning = transferState is TransferState.Scanning
                    EmptyView(
                        title = if (isScanning) "Searching for Nearby Peers..." else "No Devices Connected",
                        subtitle = if (isScanning)
                            "Scanning 2.4GHz Bluetooth band for RelayNet student nodes..."
                        else
                            "Tap 'Scan' to discover nearby students or school hubs to share packs offline.",
                        icon = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Devices,
                        actionButtonText = if (!isScanning) "Start Scanning" else null,
                        onActionClick = { viewModel.startScanning() },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(peers, key = { it.id }) { peer ->
                    PeerLearningDeviceCard(
                        peer = peer,
                        isDemoMode = isDemoMode,
                        onSendClick = { viewModel.sendToPeer(peer) },
                        onSimulateReceiveClick = { viewModel.simulateIncomingTransferFromPeer(peer) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Learning Pack to Transfer Selection
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Pack to Share (ପାଠ ବାଛନ୍ତୁ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(packs, key = { "select-${it.packId}" }) { pack ->
                val isSelected = selectedPackId == pack.packId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectPack(pack.packId) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pack.titleEnglish, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = pack.titleOdia, fontSize = 12.sp, color = PrimaryBlue)
                            Text(text = "${pack.sizeBytes / 1024} KB • SHA-256: ${pack.checksumSha256.take(8)}...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun PeerLearningDeviceCard(
    peer: PeerDevice,
    isDemoMode: Boolean,
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
                    Text(text = "${peer.address} • Signal: ${peer.signalStrengthRssi} dBm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSendClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Pack", fontSize = 12.sp)
                }

                if (isDemoMode) {
                    OutlinedButton(
                        onClick = onSimulateReceiveClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Receive", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransferringPackCard(state: TransferState.Transferring, onCancel: () -> Unit) {
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
                Text(
                    text = if (state.isOutgoing) "Sending to ${state.peer.name}..." else "Receiving from ${state.peer.name}...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onCancel, modifier = Modifier.size(22.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = state.currentItemTitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${state.progressPercent}% Transferred • Checksum Verifying...", fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConnectingPackCard(peer: PeerDevice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "Connecting to ${peer.name}...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Establishing Bluetooth RFCOMM socket channel...", fontSize = 11.sp)
            }
        }
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
                Text(text = "Transfer Verified", fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = state.message, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCurriculum,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("View Lessons", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Done", fontSize = 12.sp)
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
