package com.nmiet.app.nearby

import com.nmiet.app.domain.model.LearningPack
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
    private val onPackReceivedCallback: (suspend (pack: LearningPack, peerName: String) -> Unit)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = true

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private var activeJob: Job? = null

    private val simulatedPeers = listOf(
        PeerDevice(
            id = "demo-peer-b",
            name = "Student Phone B (Offline Learner)",
            address = "74:D2:1D:91:AA:01",
            signalStrengthRssi = -46
        ),
        PeerDevice(
            id = "demo-peer-school",
            name = "Village School Terminal (Gram Panchayat)",
            address = "AC:5F:3E:12:88:B4",
            signalStrengthRssi = -62
        ),
        PeerDevice(
            id = "demo-peer-tablet",
            name = "Class 9 Study Tablet (Community Center)",
            address = "38:F9:D3:45:90:7C",
            signalStrengthRssi = -75
        )
    )

    override fun startScanning() {
        activeJob?.cancel()
        _transferState.value = TransferState.Scanning(emptyList())

        activeJob = scope.launch {
            delay(500)
            _transferState.value = TransferState.Scanning(simulatedPeers.take(1))
            delay(600)
            _transferState.value = TransferState.Scanning(simulatedPeers.take(2))
            delay(600)
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

    override fun sendPackToPeer(peer: PeerDevice, pack: LearningPack) {
        activeJob?.cancel()
        activeJob = scope.launch {
            _transferState.value = TransferState.Connecting(peer)
            delay(600)

            _transferState.value = TransferState.Connected(peer)
            delay(400)

            val steps = listOf(15, 35, 60, 85, 100)
            for (progress in steps) {
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = progress,
                    currentItemTitle = "${pack.titleEnglish} (${pack.sizeBytes / 1024} KB)",
                    totalItems = 1,
                    currentItemIndex = 1
                )
                delay(220)
            }

            _transferState.value = TransferState.Success(
                peer = peer,
                transferredCount = 1,
                message = "Learning pack '${pack.titleEnglish}' transferred successfully to ${peer.name} [SHA-256 verified]."
            )
        }
    }

    fun simulateReceivePackFromPeer(peer: PeerDevice, pack: LearningPack) {
        activeJob?.cancel()
        activeJob = scope.launch {
            _transferState.value = TransferState.Connecting(peer)
            delay(500)

            _transferState.value = TransferState.Connected(peer)
            delay(400)

            val steps = listOf(20, 45, 75, 100)
            for (progress in steps) {
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = false,
                    progressPercent = progress,
                    currentItemTitle = "${pack.titleEnglish} (${pack.sizeBytes / 1024} KB)",
                    totalItems = 1,
                    currentItemIndex = 1
                )
                delay(200)
            }

            onPackReceivedCallback?.invoke(pack, peer.name)

            _transferState.value = TransferState.Success(
                peer = peer,
                transferredCount = 1,
                message = "Received and verified pack '${pack.titleEnglish}' from ${peer.name}. Ready for offline learning!"
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
