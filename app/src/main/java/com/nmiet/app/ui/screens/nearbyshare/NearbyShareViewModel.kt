package com.nmiet.app.ui.screens.nearbyshare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.nearby.BluetoothTransferManager
import com.nmiet.app.nearby.DemoTransferManager
import com.nmiet.app.nearby.NearbyTransferManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NearbyShareViewModel(
    private val repository: ContentRepository,
    val demoManager: DemoTransferManager,
    val bluetoothManager: BluetoothTransferManager
) : ViewModel() {

    private val _useDemoMode = MutableStateFlow(true)
    val useDemoMode: StateFlow<Boolean> = _useDemoMode.asStateFlow()

    private val _activeManager = MutableStateFlow<NearbyTransferManager>(demoManager)

    val transferState: StateFlow<TransferState> = _activeManager.flatMapLatest { manager ->
        manager.transferState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransferState.Idle
    )

    val cachedItems: StateFlow<List<ContentItem>> = repository.getAllContentFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    init {
        // Pre-select all cached items by default
        viewModelScope.launch {
            repository.getAllContentFlow().collect { list ->
                if (_selectedItemIds.value.isEmpty() && list.isNotEmpty()) {
                    _selectedItemIds.value = list.map { it.id }.toSet()
                }
            }
        }
    }

    fun setDemoMode(enabled: Boolean) {
        _useDemoMode.value = enabled
        val manager = if (enabled) demoManager else bluetoothManager
        _activeManager.value = manager
        manager.resetToIdle()
    }

    fun startScanning() {
        _activeManager.value.startScanning()
    }

    fun stopScanning() {
        _activeManager.value.stopScanning()
    }

    fun toggleItemSelection(id: String) {
        val current = _selectedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItemIds.value = current
    }

    fun selectAllItems() {
        _selectedItemIds.value = cachedItems.value.map { it.id }.toSet()
    }

    fun sendToPeer(peer: PeerDevice) {
        val selected = cachedItems.value.filter { _selectedItemIds.value.contains(it.id) }
        _activeManager.value.sendContentToPeer(peer, selected)
    }

    fun simulateIncomingTransferFromPeer(peer: PeerDevice) {
        if (_useDemoMode.value) {
            val incomingDemoItems = listOf(
                ContentItem(
                    id = "nmiet-peer-shared-01",
                    title = "[Peer Received] Mid-Term Practical Exam Schedule Released",
                    summary = "Received over Bluetooth from ${peer.name}. Contains full lab batch allocations.",
                    body = "This document was transferred directly from a peer device over Bluetooth mesh. Lab examinations for CSE 3rd year will be conducted in Computer Center 2. Check your batch numbers on the department bulletin board.",
                    category = "Exams",
                    source = "NMIET Peer Mesh",
                    publishedAt = System.currentTimeMillis() - 3600000,
                    contentHash = "a7f9b2c8d4e1",
                    isSavedOffline = true,
                    isReceivedViaNearby = true,
                    receivedFromPeerName = peer.name
                ),
                ContentItem(
                    id = "nmiet-peer-shared-02",
                    title = "[Peer Received] Library Emergency Hours & Off-Grid Access",
                    summary = "Shared offline by ${peer.name} during internet outage.",
                    body = "Received offline via peer-to-peer transport. Study rooms remain open 24/7 with backup generator power. Offline research terminals are accessible on the second floor.",
                    category = "Campus Life",
                    source = "NMIET Peer Mesh",
                    publishedAt = System.currentTimeMillis() - 7200000,
                    contentHash = "b810d4e9c3f2",
                    isSavedOffline = true,
                    isReceivedViaNearby = true,
                    receivedFromPeerName = peer.name
                )
            )
            demoManager.simulateReceiveContentFromPeer(peer, incomingDemoItems)
        }
    }

    fun cancelTransfer() {
        _activeManager.value.cancelTransfer()
    }

    fun resetTransfer() {
        _activeManager.value.resetToIdle()
    }
}
