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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransferState.Idle)

    val learningPacks: StateFlow<List<LearningPack>> = repository.getLearningPacksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPackId = MutableStateFlow<String?>(null)
    val selectedPackId: StateFlow<String?> = _selectedPackId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLearningPacksFlow().collect { packs ->
                if (_selectedPackId.value == null && packs.isNotEmpty()) {
                    _selectedPackId.value = packs.first().packId
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

    fun selectPack(packId: String) {
        _selectedPackId.value = packId
    }

    fun sendToPeer(peer: PeerDevice) {
        val pack = learningPacks.value.firstOrNull { it.packId == _selectedPackId.value }
            ?: learningPacks.value.firstOrNull()
            ?: PilotCurriculumCatalog.getPilotLearningPack()

        _activeManager.value.sendPackToPeer(peer, pack)
    }

    fun simulateIncomingTransferFromPeer(peer: PeerDevice) {
        if (_useDemoMode.value) {
            val incomingPack = PilotCurriculumCatalog.getPilotLearningPack().copy(
                packId = "relaynet_class9_science_mesh_received_v1",
                titleEnglish = "[Peer Mesh] Photosynthesis & Plant Nutrition",
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
