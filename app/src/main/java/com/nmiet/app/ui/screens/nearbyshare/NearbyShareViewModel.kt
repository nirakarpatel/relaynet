package com.nmiet.app.ui.screens.nearbyshare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.LearningPack
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

enum class ShareDirection {
    SEND, RECEIVE
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NearbyShareViewModel(
    private val repository: ContentRepository,
    val demoManager: DemoTransferManager,
    val bluetoothManager: BluetoothTransferManager
) : ViewModel() {

    private val _shareDirection = MutableStateFlow(ShareDirection.SEND)
    val shareDirection: StateFlow<ShareDirection> = _shareDirection.asStateFlow()

    private val _useDemoMode = MutableStateFlow(true)
    val useDemoMode: StateFlow<Boolean> = _useDemoMode.asStateFlow()

    private val _activeManager = MutableStateFlow<NearbyTransferManager>(demoManager)

    val transferState: StateFlow<TransferState> = _activeManager.flatMapLatest { manager ->
        manager.transferState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransferState.Idle)

    val learningPacks: StateFlow<List<LearningPack>> = repository.getLearningPacksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPackIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackIds: StateFlow<Set<String>> = _selectedPackIds.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLearningPacksFlow().collect { packs ->
                if (_selectedPackIds.value.isEmpty() && packs.isNotEmpty()) {
                    _selectedPackIds.value = setOf(packs.first().packId)
                }
            }
        }
    }

    fun setShareDirection(direction: ShareDirection) {
        _shareDirection.value = direction
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

    fun togglePackSelection(packId: String) {
        val current = _selectedPackIds.value.toMutableSet()
        if (current.contains(packId)) {
            if (current.size > 1) current.remove(packId)
        } else {
            current.add(packId)
        }
        _selectedPackIds.value = current
    }

    fun sendToPeer(peer: PeerDevice) {
        val firstSelected = _selectedPackIds.value.firstOrNull()
        val pack = learningPacks.value.firstOrNull { it.packId == firstSelected }
            ?: learningPacks.value.firstOrNull()
            ?: PilotCurriculumCatalog.getPilotLearningPack()

        _activeManager.value.sendPackToPeer(peer, pack)
    }

    fun simulateIncomingTransferFromPeer(peer: PeerDevice) {
        if (_useDemoMode.value) {
            val incomingPack = PilotCurriculumCatalog.getPilotLearningPack().copy(
                packId = "relaynet_class9_science_mesh_received_v1",
                titleEnglish = "[Node-02 Mesh] Photosynthesis & Plant Biochemistry",
                titleOdia = "[ସହପାଠୀ ସେୟାର] ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
                isReceivedViaMesh = true,
                peerSenderName = peer.name
            )
            demoManager.simulateReceivePackFromPeer(peer, incomingPack)
        }
    }

    fun cancelTransfer() {
        _activeManager.value.cancelTransfer()
    }

    fun resetTransfer() {
        _activeManager.value.resetToIdle()
    }
}
