package com.nmiet.app.ui.screens.nearbyshare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.nearby.BluetoothTransferManager
import com.nmiet.app.nearby.DemoTransferManager
import com.nmiet.app.nearby.GoogleNearbyConnectionsManager
import com.nmiet.app.nearby.NearbyTransferManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ShareDirection {
    SEND, RECEIVE
}

enum class TransportProtocol {
    DEMO_SIMULATION,
    GOOGLE_NEARBY_CONNECTIONS,
    BLUETOOTH_RFCOMM
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NearbyShareViewModel(
    private val repository: ContentRepository,
    val demoManager: DemoTransferManager,
    val bluetoothManager: BluetoothTransferManager,
    val nearbyManager: GoogleNearbyConnectionsManager? = null
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager?.currentLanguage
        ?: repository.getLearnerProfileFlow()
            .map { it.preferredLanguage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BILINGUAL_EN_OR)

    private val _shareDirection = MutableStateFlow(ShareDirection.SEND)
    val shareDirection: StateFlow<ShareDirection> = _shareDirection.asStateFlow()

    private val _selectedProtocol = MutableStateFlow(TransportProtocol.GOOGLE_NEARBY_CONNECTIONS)
    val selectedProtocol: StateFlow<TransportProtocol> = _selectedProtocol.asStateFlow()

    private val _useDemoMode = MutableStateFlow(false)
    val useDemoMode: StateFlow<Boolean> = _useDemoMode.asStateFlow()

    private val _activeManager = MutableStateFlow<NearbyTransferManager>(nearbyManager ?: bluetoothManager)

    val transferState: StateFlow<TransferState> = _activeManager.flatMapLatest { manager ->
        manager.transferState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransferState.Idle)

    val isScanning: StateFlow<Boolean> = _activeManager.flatMapLatest { manager ->
        manager.isScanning
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val discoveredPeers: StateFlow<List<PeerDevice>> = _activeManager.flatMapLatest { manager ->
        manager.discoveredPeers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learningPacks: StateFlow<List<LearningPack>> = repository.getLearningPacksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<GeneratedNote>> = repository.getAllGeneratedNotesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPackIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackIds: StateFlow<Set<String>> = _selectedPackIds.asStateFlow()

    private val _selectedNoteId = MutableStateFlow<String?>(null)
    val selectedNoteId: StateFlow<String?> = _selectedNoteId.asStateFlow()

    init {
        // Default to real Bluetooth manager and ensure server is ready
        bluetoothManager.loadBondedDevices()
        bluetoothManager.startServerListening()

        viewModelScope.launch {
            repository.getLearningPacksFlow().collect { packs ->
                if (_selectedPackIds.value.isEmpty() && packs.isNotEmpty()) {
                    _selectedPackIds.value = setOf(packs.first().packId)
                }
            }
        }
        viewModelScope.launch {
            repository.getAllGeneratedNotesFlow().collect { noteList ->
                if (_selectedNoteId.value == null && noteList.isNotEmpty()) {
                    _selectedNoteId.value = noteList.first().noteId
                }
            }
        }
    }

    fun isBluetoothAdapterEnabled(): Boolean {
        return bluetoothManager.bluetoothAdapter?.isEnabled == true
    }

    fun getLocalBluetoothName(): String {
        return try {
            bluetoothManager.bluetoothAdapter?.name ?: "RelayNet Android Node"
        } catch (_: SecurityException) {
            "RelayNet Android Node"
        }
    }

    fun refreshDevices() {
        if (!_useDemoMode.value) {
            bluetoothManager.loadBondedDevices()
        }
    }

    fun setShareDirection(direction: ShareDirection) {
        _shareDirection.value = direction
        if (direction == ShareDirection.RECEIVE && !_useDemoMode.value) {
            bluetoothManager.startServerListening()
        }
    }

    fun setModelType(type: ShareModelType) {
        _selectedModelType.value = type
    }

    fun setDemoMode(enabled: Boolean) {
        _useDemoMode.value = enabled
        selectProtocol(if (enabled) TransportProtocol.DEMO_SIMULATION else TransportProtocol.GOOGLE_NEARBY_CONNECTIONS)
    }

    fun selectProtocol(protocol: TransportProtocol) {
        if (_selectedProtocol.value == protocol && _activeManager.value.transferState.value == TransferState.Idle) return
        _activeManager.value.cancelTransfer()
        _selectedProtocol.value = protocol
        _useDemoMode.value = (protocol == TransportProtocol.DEMO_SIMULATION)

        val manager: NearbyTransferManager = when (protocol) {
            TransportProtocol.DEMO_SIMULATION -> demoManager
            TransportProtocol.GOOGLE_NEARBY_CONNECTIONS -> nearbyManager ?: bluetoothManager
            TransportProtocol.BLUETOOTH_RFCOMM -> bluetoothManager
        }

        _activeManager.value = manager
        manager.resetToIdle()
        if (!enabled) {
            bluetoothManager.loadBondedDevices()
            bluetoothManager.startServerListening()
        }
    }

    fun acceptConnection(peer: PeerDevice) {
        _activeManager.value.acceptConnection(peer)
    }

    fun rejectConnection(peer: PeerDevice) {
        _activeManager.value.rejectConnection(peer)
    }

    fun exchangeManifestWithPeer(peer: PeerDevice) {
        _activeManager.value.sendManifestToPeer(peer)
    }

    fun startScanning() {
        if (!_useDemoMode.value) {
            bluetoothManager.startServerListening()
        }
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

    fun selectNote(noteId: String) {
        _selectedNoteId.value = noteId
    }

    fun sendToPeer(peer: PeerDevice) {
        if (_selectedModelType.value == ShareModelType.LEARNING_PACK) {
            val firstSelected = _selectedPackIds.value.firstOrNull()
            val pack = learningPacks.value.firstOrNull { it.packId == firstSelected }
                ?: learningPacks.value.firstOrNull()
                ?: PilotCurriculumCatalog.getPilotLearningPack()

            _activeManager.value.sendPackToPeer(peer, pack)
        } else {
            val noteId = _selectedNoteId.value
            val note = notes.value.firstOrNull { it.noteId == noteId }
                ?: notes.value.firstOrNull()
                ?: GeneratedNote.createDemoPhotosynthesisNote()

            _activeManager.value.sendNoteToPeer(peer, note)
        }
    }

    fun requestPackFromPeer(peer: PeerDevice) {
        val firstSelected = _selectedPackIds.value.firstOrNull() ?: "relaynet_class9_science_photosynthesis_v1"
        _activeManager.value.requestPackFromPeer(peer, firstSelected)
    }

    fun simulateIncomingTransferFromPeer(peer: PeerDevice) {
        if (_useDemoMode.value) {
            if (_selectedModelType.value == ShareModelType.LEARNING_PACK) {
                val incomingPack = PilotCurriculumCatalog.getPilotLearningPack().copy(
                    packId = "relaynet_class9_science_mesh_received_v1",
                    titleEnglish = "[Node-02 Mesh] Photosynthesis & Plant Biochemistry",
                    titleOdia = "[ସହପାଠୀ ସେୟାର] ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
                    isReceivedViaMesh = true,
                    peerSenderName = peer.name
                )
                demoManager.simulateReceivePackFromPeer(peer, incomingPack)
            } else {
                val note = GeneratedNote.createDemoPhotosynthesisNote().copy(
                    noteId = "note_received_${System.currentTimeMillis()}",
                    titleEnglish = "[P2P Mesh] Photosynthesis Key Equations",
                    titleOdia = "[ସହପାଠୀ ସେୟାର] ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ"
                )
                demoManager.simulateReceiveNoteFromPeer(peer, note)
            }
        } else {
            requestPackFromPeer(peer)
        }
    }

    fun cancelTransfer() {
        _activeManager.value.cancelTransfer()
    }

    fun resetTransfer() {
        _activeManager.value.resetToIdle()
    }

    override fun onCleared() {
        super.onCleared()
        _activeManager.value.stopScanning()
    }
}
