package com.nmiet.app.domain.model

sealed interface TransferState {
    data object Idle : TransferState

    data class Scanning(
        val discoveredPeers: List<PeerDevice> = emptyList()
    ) : TransferState

    data class Connecting(
        val targetPeer: PeerDevice
    ) : TransferState

    data class Connected(
        val connectedPeer: PeerDevice
    ) : TransferState

    data class Transferring(
        val peer: PeerDevice,
        val isOutgoing: Boolean,
        val progressPercent: Int,
        val currentItemTitle: String,
        val totalItems: Int = 1,
        val currentItemIndex: Int = 1
    ) : TransferState

    data class Success(
        val peer: PeerDevice,
        val transferredCount: Int,
        val message: String
    ) : TransferState

    data class Error(
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : TransferState
}
