package com.nmiet.app.ui.screens.nearbyshare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.DemoModeBadge
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun NearbyShareScreen(
    viewModel: NearbyShareViewModel,
    onNavigateToLibrary: () -> Unit
) {
    val transferState by viewModel.transferState.collectAsState()
    val isDemoMode by viewModel.useDemoMode.collectAsState()
    val cachedItems by viewModel.cachedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Nearby Share",
                isOnline = false, // Peer transfer is inherently offline
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
                Spacer(modifier = Modifier.height(12.dp))

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
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isDemoMode) DemoModeBadge()
                            }
                            Text(
                                text = if (isDemoMode)
                                    "Simulate discovery & transfer for single-device presentations."
                                else
                                    "Scan & transfer payloads directly between two physical Android phones.",
                                fontSize = 12.sp,
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

            // Transfer Status Card (Connecting / Transferring / Success / Error)
            item {
                when (val state = transferState) {
                    is TransferState.Transferring -> {
                        TransferringCard(
                            state = state,
                            onCancel = { viewModel.cancelTransfer() }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is TransferState.Connecting -> {
                        ConnectingCard(peer = state.targetPeer)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is TransferState.Success -> {
                        SuccessCard(
                            state = state,
                            onDismiss = { viewModel.resetTransfer() },
                            onOpenLibrary = onNavigateToLibrary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is TransferState.Error -> {
                        ErrorCard(
                            errorMessage = state.errorMessage,
                            canRetry = state.canRetry,
                            onRetry = { viewModel.startScanning() },
                            onDismiss = { viewModel.resetTransfer() }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    else -> {}
                }
            }

            // Scan Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Discovered Devices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val isScanning = transferState is TransferState.Scanning
                        Text(
                            text = if (isScanning) "Searching for nearby peers..." else "Ready to scan for nearby devices",
                            fontSize = 12.sp,
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
                            imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.BluetoothSearching,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isScanning) "Stop" else "Scan")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Discovered Peers list
            val peers = (transferState as? TransferState.Scanning)?.discoveredPeers ?: emptyList()
            if (peers.isEmpty()) {
                item {
                    val isScanning = transferState is TransferState.Scanning
                    EmptyView(
                        title = if (isScanning) "Scanning Nearby Mesh..." else "No Peers Connected",
                        subtitle = if (isScanning)
                            "Listening on Bluetooth channel for nearby NMIET nodes..."
                        else
                            "Tap 'Scan' to discover nearby students or devices available for offline sharing.",
                        icon = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Devices,
                        actionButtonText = if (!isScanning) "Start Scanning" else null,
                        onActionClick = { viewModel.startScanning() },
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(peers, key = { it.id }) { peer ->
                    PeerDeviceCard(
                        peer = peer,
                        isDemoMode = isDemoMode,
                        selectedCount = selectedItemIds.size,
                        onSendClick = { viewModel.sendToPeer(peer) },
                        onSimulateReceiveClick = { viewModel.simulateIncomingTransferFromPeer(peer) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Select Content Items to Transfer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Content To Share (${selectedItemIds.size}/${cachedItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = { viewModel.selectAllItems() }) {
                        Text("Select All", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(cachedItems, key = { "select-${it.id}" }) { item ->
                val isSelected = selectedItemIds.contains(item.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleItemSelection(item.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.category} • ${item.source}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun PeerDeviceCard(
    peer: PeerDevice,
    isDemoMode: Boolean,
    selectedCount: Int,
    onSendClick: () -> Unit,
    onSimulateReceiveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = peer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${peer.address} • Signal: ${peer.signalStrengthRssi} dBm",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendClick,
                    enabled = selectedCount > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Send ($selectedCount)", fontSize = 12.sp)
                }

                if (isDemoMode) {
                    OutlinedButton(
                        onClick = onSimulateReceiveClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Simulate Receive", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransferringCard(
    state: TransferState.Transferring,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (state.isOutgoing) "Sending to ${state.peer.name}..." else "Receiving from ${state.peer.name}...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${state.currentItemTitle} (${state.currentItemIndex}/${state.totalItems})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { state.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${state.progressPercent}% Completed",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ConnectingCard(peer: PeerDevice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Connecting to ${peer.name}...", fontWeight = FontWeight.Bold)
                Text(text = "Performing Bluetooth security handshake...", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SuccessCard(
    state: TransferState.Success,
    onDismiss: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Transfer Succeeded", fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = state.message, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenLibrary,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("View In Offline Library")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    errorMessage: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Transfer Interrupted", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = errorMessage, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canRetry) {
                    Button(onClick = onRetry) {
                        Text("Retry Scan")
                    }
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}
