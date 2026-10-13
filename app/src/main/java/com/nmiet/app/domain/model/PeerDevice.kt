package com.nmiet.app.domain.model

data class PeerDevice(
    val id: String,
    val name: String,
    val address: String,
    val signalStrengthRssi: Int = -55,
    val isAvailableForTransfer: Boolean = true
)
