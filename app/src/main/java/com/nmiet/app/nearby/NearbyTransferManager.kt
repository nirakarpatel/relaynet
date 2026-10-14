package com.nmiet.app.nearby

import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import kotlinx.coroutines.flow.StateFlow

interface NearbyTransferManager {
    val transferState: StateFlow<TransferState>
    val isDemoMode: Boolean

    fun startScanning()
    fun stopScanning()
    fun sendPackToPeer(peer: PeerDevice, pack: LearningPack)
    fun acceptConnection(peer: PeerDevice) {}
    fun rejectConnection(peer: PeerDevice) {}
    fun sendManifestToPeer(peer: PeerDevice) {}
    fun cancelTransfer()
    fun resetToIdle()
}
