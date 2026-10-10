package com.nmiet.app.nearby

import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DemoTransferManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val onContentReceivedCallback: (suspend (items: List<ContentItem>, peerName: String) -> Unit)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = true

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private var activeJob: Job? = null

    private val simulatedPeers = listOf(
        PeerDevice(
            id = "demo-peer-1",
            name = "Device B (CSE Lab Peer)",
            address = "74:D2:1D:91:AA:01",
            signalStrengthRssi = -48
        ),
        PeerDevice(
            id = "demo-peer-2",
            name = "Student Phone (NMIET Library)",
            address = "AC:5F:3E:12:88:B4",
            signalStrengthRssi = -64
        ),
        PeerDevice(
            id = "demo-peer-3",
            name = "Faculty Tablet (Admin Block)",
            address = "38:F9:D3:45:90:7C",
            signalStrengthRssi = -78
        )
    )

    override fun startScanning() {
        activeJob?.cancel()
        _transferState.value = TransferState.Scanning(emptyList())

        activeJob = scope.launch {
            // Emulate staggered peer discovery
            delay(600)
            _transferState.value = TransferState.Scanning(simulatedPeers.take(1))
            delay(700)
            _transferState.value = TransferState.Scanning(simulatedPeers.take(2))
            delay(800)
            _transferState.value = TransferState.Scanning(simulatedPeers)
        }
    }

    override fun stopScanning() {
        if (_transferState.value is TransferState.Scanning) {
            val currentPeers = (_transferState.value as TransferState.Scanning).discoveredPeers
            activeJob?.cancel()
            _transferState.value = if (currentPeers.isEmpty()) TransferState.Idle else TransferState.Scanning(currentPeers)
        }
    }

    override fun sendContentToPeer(peer: PeerDevice, items: List<ContentItem>) {
        if (items.isEmpty()) {
            _transferState.value = TransferState.Error("No content items selected to transfer.", canRetry = false)
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch {
            _transferState.value = TransferState.Connecting(peer)
            delay(700) // simulated handshake

            _transferState.value = TransferState.Connected(peer)
            delay(500)

            val totalItems = items.size
            items.forEachIndexed { index, item ->
                val steps = listOf(15, 35, 60, 85, 100)
                for (progress in steps) {
                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = true,
                        progressPercent = progress,
                        currentItemTitle = item.title,
                        totalItems = totalItems,
                        currentItemIndex = index + 1
                    )
                    delay(250)
                }
            }

            _transferState.value = TransferState.Success(
                peer = peer,
                transferredCount = items.size,
                message = "Successfully shared ${items.size} item(s) with ${peer.name} [Demo Mode]."
            )
        }
    }

    fun simulateReceiveContentFromPeer(peer: PeerDevice, items: List<ContentItem>) {
        activeJob?.cancel()
        activeJob = scope.launch {
            _transferState.value = TransferState.Connecting(peer)
            delay(600)

            _transferState.value = TransferState.Connected(peer)
            delay(400)

            items.forEachIndexed { index, item ->
                val steps = listOf(20, 50, 80, 100)
                for (progress in steps) {
                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = false,
                        progressPercent = progress,
                        currentItemTitle = item.title,
                        totalItems = items.size,
                        currentItemIndex = index + 1
                    )
                    delay(200)
                }
            }

            onContentReceivedCallback?.invoke(items, peer.name)

            _transferState.value = TransferState.Success(
                peer = peer,
                transferredCount = items.size,
                message = "Received ${items.size} item(s) from ${peer.name} [Demo Mode] and saved offline."
            )
        }
    }

    override fun cancelTransfer() {
        activeJob?.cancel()
        _transferState.value = TransferState.Idle
    }

    override fun resetToIdle() {
        activeJob?.cancel()
        _transferState.value = TransferState.Idle
    }
}
