package com.nmiet.app.nearby

import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import kotlinx.coroutines.flow.StateFlow

interface NearbyTransferManager {
    val transferState: StateFlow<TransferState>
    val isScanning: StateFlow<Boolean>
    val discoveredPeers: StateFlow<List<PeerDevice>>
    val isDemoMode: Boolean

    fun startScanning()
    fun stopScanning()
    fun sendPackToPeer(peer: PeerDevice, pack: LearningPack)
    fun sendNoteToPeer(peer: PeerDevice, note: GeneratedNote)
    fun requestPackFromPeer(peer: PeerDevice, packId: String = "")
    fun cancelTransfer()
    fun resetToIdle()
}
