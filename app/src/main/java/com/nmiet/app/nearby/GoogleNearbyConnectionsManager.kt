package com.nmiet.app.nearby

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.nmiet.app.data.local.GeneratedNoteEntity
import com.nmiet.app.data.local.ZipPackManager
import com.nmiet.app.domain.model.ContentManifestItem
import com.nmiet.app.domain.model.ContentMediaType
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerContentManifest
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.permissions.NearbyPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Collections
import java.util.LinkedHashMap
import java.util.UUID

/**
 * Implementation of Google Nearby Connections API (Section 10).
 *
 * Connection Strategy Selection:
 * We select Strategy.P2P_STAR as default because:
 * 1. It forms a high-bandwidth star topology connecting one hub (e.g. teacher or student with packs)
 *    to multiple learners simultaneously.
 * 2. It automatically negotiates high-speed Wi-Fi Direct or Hotspot links over Bluetooth.
 */
class GoogleNearbyConnectionsManager(
    private val context: Context,
    private val repository: ContentRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val onPackReceivedCallback: (suspend (pack: LearningPack, peerName: String) -> Unit)? = null,
    private val onNoteReceivedCallback: (suspend (note: GeneratedNote, peerName: String) -> Unit)? = null,
    private val clientProvider: (() -> ConnectionsClient)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = false

    private val connectionsClient: ConnectionsClient by lazy {
        clientProvider?.invoke() ?: Nearby.getConnectionsClient(context.applicationContext)
    }

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val discoveredPeers: StateFlow<List<PeerDevice>> = _discoveredPeers.asStateFlow()

    private val discoveredEndpoints = mutableMapOf<String, PeerDevice>()
    private var connectedEndpointId: String? = null
    private var pendingAuthPeer: PeerDevice? = null

    // Track active incoming and outgoing file payloads
    private val incomingFilePayloads = mutableMapOf<Long, Payload>()
    private val outgoingFilePayloads = mutableMapOf<Long, LearningPack>()

    // Track active pending note transfers (sender side) with correlation & timeout
    data class PendingNoteTransfer(
        val messageId: String,
        val note: GeneratedNote,
        val peer: PeerDevice,
        val payloadId: Long,
        val timestamp: Long,
        var timeoutJob: Job? = null
    )
    private val pendingNoteTransfersByMessageId = mutableMapOf<String, PendingNoteTransfer>()
    private val pendingNoteTransfersByPayloadId = mutableMapOf<Long, PendingNoteTransfer>()

    // Set of processed note message IDs to handle duplicate messages idempotently (bounded to prevent leaks)
    private val processedNoteMessageIds: MutableSet<String> = Collections.synchronizedSet(
        object : LinkedHashMap<String, Boolean>(100, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
                return size > 300
            }
        }.keys
    )

    // Track pack requests and correlated file transfer metadata
    private val pendingPackRequests = mutableMapOf<String, PendingPackRequest>()
    private val incomingPackMetadataByPayloadId = mutableMapOf<Long, IncomingPackMetadata>()
    private val completedIncomingFiles = mutableMapOf<Long, File>()

    data class PendingPackRequest(
        val requestId: String,
        val packId: String,
        val peer: PeerDevice,
        val timestamp: Long
    )

    data class IncomingPackMetadata(
        val requestId: String,
        val packId: String,
        val filePayloadId: Long,
        val fileSizeBytes: Long,
        val sha256Checksum: String,
        val titleEnglish: String,
        val peer: PeerDevice
    )

    companion object {
        const val SERVICE_ID = "com.nmiet.app.relaynet.NEARBY_SERVICE"
        val STRATEGY: Strategy = Strategy.P2P_STAR

        const val TYPE_MANIFEST = "RELAYNET_MANIFEST_EXCHANGE"
        const val TYPE_NOTE = "RELAYNET_GENERATED_NOTE"
        const val TYPE_NOTE_ACK = "RELAYNET_NOTE_ACK"
        const val TYPE_PACK_REQUEST = "RELAYNET_PACK_REQUEST"
        const val TYPE_PACK_RESPONSE = "RELAYNET_PACK_RESPONSE"

        data class NoteTransferMessage(
            val protocolVersion: Int,
            val messageId: String,
            val note: GeneratedNote
        )

        data class NoteAckMessage(
            val protocolVersion: Int,
            val messageId: String,
            val noteId: String,
            val status: String, // "SUCCESS" or "ERROR"
            val errorMessage: String = ""
        )

        data class PackRequestMessage(
            val protocolVersion: Int,
            val requestId: String,
            val packId: String
        )

        data class PackResponseMessage(
            val protocolVersion: Int,
            val requestId: String,
            val packId: String,
            val status: String, // AVAILABLE, NOT_FOUND, ERROR
            val message: String = "",
            val titleEnglish: String = "",
            val filePayloadId: Long = -1L,
            val fileSizeBytes: Long = 0L,
            val sha256Checksum: String = ""
        )

        fun encodeNote(note: GeneratedNote, messageId: String = "msg_note_${note.noteId}"): ByteArray {
            val entity = GeneratedNoteEntity.fromDomain(note)
            val json = JSONObject().apply {
                put("type", TYPE_NOTE)
                put("protocolVersion", 1)
                put("messageId", messageId)
                put("noteId", entity.noteId)
                put("topicId", entity.topicId)
                put("topicName", entity.topicName)
                put("titleOdia", entity.titleOdia)
                put("titleEnglish", entity.titleEnglish)
                put("contentJson", entity.contentJson)
                put("createdAt", entity.createdAt)
                put("isBookmarked", entity.isBookmarked)
                put("isOfflineGenerated", entity.isOfflineGenerated)
            }
            return json.toString().toByteArray(Charsets.UTF_8)
        }

        fun decodeNote(bytes: ByteArray): GeneratedNote? {
            return decodeNoteTransfer(bytes)?.note
        }

        fun decodeNoteTransfer(bytes: ByteArray): NoteTransferMessage? {
            if (bytes.isEmpty()) return null
            return try {
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                if (json.optString("type") != TYPE_NOTE) return null
                val version = json.optInt("protocolVersion", 1)
                if (version > 1) return null
                val noteId = json.optString("noteId", "").trim()
                if (noteId.isEmpty()) return null

                val messageId = json.optString("messageId", "msg_note_$noteId").trim().ifEmpty { "msg_note_$noteId" }

                val entity = GeneratedNoteEntity(
                    noteId = noteId,
                    topicId = json.optString("topicId", "topic_custom"),
                    topicName = json.optString("topicName", "P2P Shared Note"),
                    titleOdia = json.optString("titleOdia", ""),
                    titleEnglish = json.optString("titleEnglish", "AI Study Note"),
                    contentJson = json.optString("contentJson", "{}"),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    isBookmarked = json.optBoolean("isBookmarked", true),
                    isOfflineGenerated = json.optBoolean("isOfflineGenerated", true)
                )
                NoteTransferMessage(
                    protocolVersion = version,
                    messageId = messageId,
                    note = entity.toDomain()
                )
            } catch (_: Exception) {
                null
            }
        }

        fun encodeNoteAck(ack: NoteAckMessage): ByteArray {
            val json = JSONObject().apply {
                put("type", TYPE_NOTE_ACK)
                put("protocolVersion", ack.protocolVersion)
                put("messageId", ack.messageId.trim())
                put("noteId", ack.noteId.trim())
                put("status", ack.status.trim().uppercase())
                if (ack.errorMessage.isNotBlank()) {
                    put("errorMessage", ack.errorMessage)
                }
            }
            return json.toString().toByteArray(Charsets.UTF_8)
        }

        fun decodeNoteAck(bytes: ByteArray): NoteAckMessage? {
            if (bytes.isEmpty()) return null
            return try {
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                if (json.optString("type") != TYPE_NOTE_ACK) return null
                val version = json.optInt("protocolVersion", 1)
                if (version > 1) return null
                val messageId = json.optString("messageId", "").trim()
                val noteId = json.optString("noteId", "").trim()
                val status = json.optString("status", "").trim().uppercase()
                if (messageId.isEmpty() || noteId.isEmpty() || status.isEmpty()) return null
                if (status !in listOf("SUCCESS", "ERROR")) return null

                NoteAckMessage(
                    protocolVersion = version,
                    messageId = messageId,
                    noteId = noteId,
                    status = status,
                    errorMessage = json.optString("errorMessage", "")
                )
            } catch (_: Exception) {
                null
            }
        }

        fun encodePackRequest(requestId: String, packId: String): ByteArray {
            val json = JSONObject().apply {
                put("type", TYPE_PACK_REQUEST)
                put("protocolVersion", 1)
                put("requestId", requestId.trim())
                put("packId", packId.trim())
            }
            return json.toString().toByteArray(Charsets.UTF_8)
        }

        fun decodePackRequest(bytes: ByteArray): PackRequestMessage? {
            if (bytes.isEmpty()) return null
            return try {
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                if (json.optString("type") != TYPE_PACK_REQUEST) return null
                val version = json.optInt("protocolVersion", 1)
                if (version > 1) return null
                val reqId = json.optString("requestId", "").trim()
                val pId = json.optString("packId", "").trim()
                if (reqId.isEmpty() || pId.isEmpty()) return null

                PackRequestMessage(
                    protocolVersion = version,
                    requestId = reqId,
                    packId = pId
                )
            } catch (_: Exception) {
                null
            }
        }

        fun encodePackResponse(response: PackResponseMessage): ByteArray {
            val json = JSONObject().apply {
                put("type", TYPE_PACK_RESPONSE)
                put("protocolVersion", response.protocolVersion)
                put("requestId", response.requestId.trim())
                put("packId", response.packId.trim())
                put("status", response.status.trim())
                if (response.message.isNotBlank()) put("message", response.message)
                if (response.titleEnglish.isNotBlank()) put("titleEnglish", response.titleEnglish)
                if (response.filePayloadId > 0L) put("filePayloadId", response.filePayloadId)
                if (response.fileSizeBytes > 0L) put("fileSizeBytes", response.fileSizeBytes)
                if (response.sha256Checksum.isNotBlank()) put("sha256Checksum", response.sha256Checksum)
            }
            return json.toString().toByteArray(Charsets.UTF_8)
        }

        fun decodePackResponse(bytes: ByteArray): PackResponseMessage? {
            if (bytes.isEmpty()) return null
            return try {
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                if (json.optString("type") != TYPE_PACK_RESPONSE) return null
                val version = json.optInt("protocolVersion", 1)
                if (version > 1) return null
                val reqId = json.optString("requestId", "").trim()
                val pId = json.optString("packId", "").trim()
                val status = json.optString("status", "").trim().uppercase()
                if (reqId.isEmpty() || pId.isEmpty() || status.isEmpty()) return null
                if (status !in listOf("AVAILABLE", "NOT_FOUND", "ERROR")) return null

                val filePayloadId = json.optLong("filePayloadId", -1L)
                val sha256Checksum = json.optString("sha256Checksum", "")
                if (status == "AVAILABLE" && (filePayloadId <= 0L || sha256Checksum.isBlank())) {
                    return null
                }

                PackResponseMessage(
                    protocolVersion = version,
                    requestId = reqId,
                    packId = pId,
                    status = status,
                    message = json.optString("message", ""),
                    titleEnglish = json.optString("titleEnglish", ""),
                    filePayloadId = filePayloadId,
                    fileSizeBytes = json.optLong("fileSizeBytes", 0L),
                    sha256Checksum = sha256Checksum
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val gms = GoogleApiAvailability.getInstance()
        val resultCode = gms.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    override fun startScanning() {
        if (!isGooglePlayServicesAvailable()) {
            _transferState.value = TransferState.Error(
                "Google Play services is not available or outdated on this device.",
                canRetry = false
            )
            return
        }

        if (!NearbyPermissionManager.hasNearbyPermissions(context)) {
            _transferState.value = TransferState.Error(
                "Nearby Wi-Fi and Bluetooth permissions are required to scan for peers.",
                canRetry = false
            )
            return
        }

        // Clean up previous sessions before initiating discovery/advertising
        stopScanning()
        discoveredEndpoints.clear()
        _discoveredPeers.value = emptyList()
        _isScanning.value = true
        _transferState.value = TransferState.Scanning(emptyList())

        // Start both Advertising (so other peers discover this device) and Discovery
        startAdvertising()
        startDiscovery()
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        val localName = "Learner-${android.os.Build.MODEL.take(8)}"

        connectionsClient.startAdvertising(
            localName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnFailureListener { e ->
            // Advertising failure does not prevent discovery from functioning
        }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnFailureListener { e ->
            _isScanning.value = false
            _transferState.value = TransferState.Error(
                "Failed to start Nearby Discovery: ${e.localizedMessage ?: "Unknown error"}",
                canRetry = true
            )
        }
    }

    override fun stopScanning() {
        _isScanning.value = false
        try {
            connectionsClient.stopDiscovery()
            connectionsClient.stopAdvertising()
        } catch (_: Exception) {}
    }

    // --- Endpoint Discovery Callbacks ---

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val peer = PeerDevice(
                id = endpointId,
                name = info.endpointName.ifBlank { "Nearby Android Learner" },
                address = endpointId,
                signalStrengthRssi = -55
            )
            discoveredEndpoints[endpointId] = peer
            val peerList = discoveredEndpoints.values.toList()
            _discoveredPeers.value = peerList
            _transferState.value = TransferState.Scanning(peerList)
        }

        override fun onEndpointLost(endpointId: String) {
            discoveredEndpoints.remove(endpointId)
            val peerList = discoveredEndpoints.values.toList()
            _discoveredPeers.value = peerList
            _transferState.value = TransferState.Scanning(peerList)
        }
    }

    // --- Connection Lifecycle Callbacks ---

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            val peer = PeerDevice(
                id = endpointId,
                name = connectionInfo.endpointName.ifBlank { "Nearby Peer" },
                address = endpointId
            )
            pendingAuthPeer = peer

            // Section requirement: Verify authentication token on both devices before accepting
            _transferState.value = TransferState.AuthVerification(
                peer = peer,
                authenticationDigits = connectionInfo.authenticationDigits,
                isIncoming = connectionInfo.isIncomingConnection
            )
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            when (resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    connectedEndpointId = endpointId
                    val peer = discoveredEndpoints[endpointId] ?: pendingAuthPeer ?: PeerDevice(id = endpointId, name = "Connected Peer", address = endpointId)
                    _transferState.value = TransferState.Connected(peer)
                    // Automatically exchange content manifest
                    sendManifestToPeer(peer)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _transferState.value = TransferState.Error("Connection was rejected by peer.", canRetry = true)
                }
                else -> {
                    _transferState.value = TransferState.Error("Connection failed with code: ${resolution.status.statusCode}", canRetry = true)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (connectedEndpointId == endpointId) {
                connectedEndpointId = null
                synchronized(pendingNoteTransfersByMessageId) {
                    if (pendingNoteTransfersByMessageId.isNotEmpty()) {
                        pendingNoteTransfersByMessageId.values.forEach { it.timeoutJob?.cancel() }
                        pendingNoteTransfersByMessageId.clear()
                        pendingNoteTransfersByPayloadId.clear()
                        _transferState.value = TransferState.Error(
                            "Peer disconnected before note delivery confirmation was received.",
                            canRetry = true
                        )
                        return
                    }
                }
                _transferState.value = TransferState.Idle
            }
        }
    }

    override fun acceptConnection(peer: PeerDevice) {
        connectionsClient.acceptConnection(peer.id, payloadCallback)
    }

    override fun rejectConnection(peer: PeerDevice) {
        try {
            connectionsClient.rejectConnection(peer.id)
        } catch (_: Exception) {}
        pendingAuthPeer = null
        _transferState.value = TransferState.Idle
    }

    // --- Manifest Exchange & Comparison ---

    override fun sendManifestToPeer(peer: PeerDevice) {
        scope.launch(Dispatchers.IO) {
            val packs = repository.getLearningPacksFlow().firstOrNull() ?: emptyList()
            val manifestItems = packs.map { pack ->
                ContentManifestItem(
                    contentId = pack.packId,
                    title = pack.titleEnglish,
                    mediaType = ContentMediaType.LEARNING_PACK_ZIP,
                    language = if (pack.titleOdia.isNotBlank()) "or+en" else "en",
                    version = pack.version,
                    sizeBytes = pack.sizeBytes,
                    sha256Checksum = pack.checksumSha256
                )
            }

            val manifest = PeerContentManifest(
                deviceName = "Learner-${android.os.Build.MODEL.take(8)}",
                items = manifestItems
            )

            val payload = Payload.fromBytes(manifest.serialize())
            connectionsClient.sendPayload(peer.id, payload)
        }
    }

    private fun handleReceivedManifest(bytes: ByteArray, peer: PeerDevice) {
        val manifest = PeerContentManifest.deserialize(bytes) ?: return
        scope.launch(Dispatchers.IO) {
            val localPacks = repository.getLearningPacksFlow().firstOrNull() ?: emptyList()

            // Compare received manifest with locally installed content
            val missingOrUpdatable = manifest.items.filter { item ->
                val localPack = localPacks.find { it.packId == item.contentId }
                localPack == null || localPack.version < item.version
            }

            _transferState.value = TransferState.ManifestReceived(
                peer = peer,
                manifest = manifest,
                missingOrUpdatableItems = missingOrUpdatable
            )
        }
    }

    // --- Discriminator Router for Incoming Bytes ---

    private fun handleReceivedBytes(bytes: ByteArray, peer: PeerDevice) {
        if (bytes.isEmpty()) return
        val jsonString = try {
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            return
        }
        val json = try {
            JSONObject(jsonString)
        } catch (_: Exception) {
            return
        }

        when (json.optString("type")) {
            TYPE_MANIFEST -> {
                handleReceivedManifest(bytes, peer)
            }
            TYPE_NOTE -> {
                handleReceivedNote(bytes, json, peer)
            }
            TYPE_NOTE_ACK -> {
                handleIncomingNoteAck(bytes, peer)
            }
            TYPE_PACK_REQUEST -> {
                handleIncomingPackRequest(bytes, json, peer)
            }
            TYPE_PACK_RESPONSE -> {
                handleIncomingPackResponse(bytes, json, peer)
            }
            else -> {
                // Ignore unknown message types safely
            }
        }
    }

    private fun handleReceivedNote(bytes: ByteArray, json: JSONObject, peer: PeerDevice) {
        val endpointId = connectedEndpointId ?: peer.id
        val noteTransfer = decodeNoteTransfer(bytes)

        if (noteTransfer == null) {
            val messageId = json.optString("messageId", "").trim()
            val noteId = json.optString("noteId", "").trim()
            if (messageId.isNotEmpty() && noteId.isNotEmpty()) {
                sendNoteAck(endpointId, messageId, noteId, "ERROR", "Malformed note payload or unsupported version")
            }
            _transferState.value = TransferState.Error(
                "Received malformed or unsupported note payload.",
                canRetry = false
            )
            return
        }

        val note = noteTransfer.note
        val messageId = noteTransfer.messageId

        scope.launch(Dispatchers.IO) {
            // Idempotency: Handle duplicate messages idempotently without repeated persistence
            if (processedNoteMessageIds.contains(messageId)) {
                // Already processed: re-send success ACK so sender confirms, without duplicate persistence
                sendNoteAck(endpointId, messageId, note.noteId, "SUCCESS", "")
                return@launch
            }

            try {
                // Exactly one owner for note persistence: GoogleNearbyConnectionsManager
                repository.saveGeneratedNote(note)
                processedNoteMessageIds.add(messageId)

                // Send correlated SUCCESS ACK after persistence succeeds
                sendNoteAck(endpointId, messageId, note.noteId, "SUCCESS", "")

                // Notify UI/application components via callback without duplicate database write
                onNoteReceivedCallback?.invoke(note, peer.name)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "AI Study Note '${note.titleEnglish}' received and saved from ${peer.name}."
                )
            } catch (e: Exception) {
                // Send error response if persistence fails
                sendNoteAck(endpointId, messageId, note.noteId, "ERROR", e.message ?: "Database persistence error")
                _transferState.value = TransferState.Error(
                    "Failed to save received note: ${e.message}",
                    canRetry = false
                )
            }
        }
    }

    private fun sendNoteAck(
        endpointId: String,
        messageId: String,
        noteId: String,
        status: String,
        errorMessage: String
    ) {
        val ack = NoteAckMessage(
            protocolVersion = 1,
            messageId = messageId,
            noteId = noteId,
            status = status,
            errorMessage = errorMessage
        )
        val bytes = encodeNoteAck(ack)
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    private fun handleIncomingNoteAck(bytes: ByteArray, peer: PeerDevice) {
        val ack = decodeNoteAck(bytes) ?: return
        val pending = synchronized(pendingNoteTransfersByMessageId) {
            pendingNoteTransfersByMessageId.remove(ack.messageId)
        }

        if (pending == null) {
            // Late, duplicate, or mismatched ACK: ignore safely
            return
        }

        pending.timeoutJob?.cancel()
        synchronized(pendingNoteTransfersByPayloadId) {
            pendingNoteTransfersByPayloadId.remove(pending.payloadId)
        }

        if (ack.status == "SUCCESS") {
            _transferState.value = TransferState.Success(
                peer = peer,
                transferredCount = 1,
                message = "AI Study Note '${pending.note.titleEnglish}' safely received and saved by ${peer.name}."
            )
        } else {
            val err = ack.errorMessage.ifBlank { "Peer reported failure saving note." }
            _transferState.value = TransferState.Error(
                "Peer failed to save note: $err",
                canRetry = true
            )
        }
    }

    private fun handleIncomingPackRequest(bytes: ByteArray, json: JSONObject, peer: PeerDevice) {
        val endpointId = connectedEndpointId ?: peer.id
        val request = decodePackRequest(bytes)
        if (request == null) {
            val reqId = json.optString("requestId", "")
            val pId = json.optString("packId", "")
            sendPackResponseError(endpointId, reqId, pId, "Malformed or unsupported pack request.")
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val pack = repository.getPackByIdOnce(request.packId)
                val archiveFile = repository.getPackArchiveFile(request.packId)

                if (pack == null || archiveFile == null || !archiveFile.exists()) {
                    sendPackResponseNotFound(
                        endpointId = endpointId,
                        requestId = request.requestId,
                        packId = request.packId,
                        message = "Pack '${request.packId}' not found on peer."
                    )
                    return@launch
                }

                if (!archiveFile.canRead() || archiveFile.length() <= 0L) {
                    sendPackResponseError(
                        endpointId = endpointId,
                        requestId = request.requestId,
                        packId = request.packId,
                        message = "Pack archive for '${request.packId}' is inaccessible or empty."
                    )
                    return@launch
                }

                val sha256 = ZipPackManager.calculateSha256(archiveFile)
                val filePayload = Payload.fromFile(archiveFile)

                outgoingFilePayloads[filePayload.id] = pack

                val resp = PackResponseMessage(
                    protocolVersion = 1,
                    requestId = request.requestId,
                    packId = request.packId,
                    status = "AVAILABLE",
                    titleEnglish = pack.titleEnglish,
                    filePayloadId = filePayload.id,
                    fileSizeBytes = archiveFile.length(),
                    sha256Checksum = sha256
                )
                val respBytes = encodePackResponse(resp)
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(respBytes))

                connectionsClient.sendPayload(endpointId, filePayload)
                    .addOnFailureListener { e ->
                        outgoingFilePayloads.remove(filePayload.id)
                        _transferState.value = TransferState.Error(
                            "Failed to transmit file payload: ${e.message}",
                            canRetry = true
                        )
                    }
            } catch (e: Exception) {
                sendPackResponseError(
                    endpointId = endpointId,
                    requestId = request.requestId,
                    packId = request.packId,
                    message = "Error packaging requested pack: ${e.message}"
                )
            }
        }
    }

    private fun sendPackResponseNotFound(endpointId: String, requestId: String, packId: String, message: String) {
        val resp = PackResponseMessage(
            protocolVersion = 1,
            requestId = requestId,
            packId = packId,
            status = "NOT_FOUND",
            message = message
        )
        val bytes = encodePackResponse(resp)
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    private fun sendPackResponseError(endpointId: String, requestId: String, packId: String, message: String) {
        val resp = PackResponseMessage(
            protocolVersion = 1,
            requestId = requestId,
            packId = packId,
            status = "ERROR",
            message = message
        )
        val bytes = encodePackResponse(resp)
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    private fun handleIncomingPackResponse(bytes: ByteArray, json: JSONObject, peer: PeerDevice) {
        val response = decodePackResponse(bytes)
        if (response == null) {
            val reqId = json.optString("requestId", "")
            if (reqId.isNotBlank()) pendingPackRequests.remove(reqId)
            _transferState.value = TransferState.Error(
                "Received malformed pack response from peer.",
                canRetry = false
            )
            return
        }

        when (response.status) {
            "NOT_FOUND" -> {
                pendingPackRequests.remove(response.requestId)
                _transferState.value = TransferState.Error(
                    response.message.ifBlank { "Requested pack '${response.packId}' was not found on peer." },
                    canRetry = false
                )
            }
            "ERROR" -> {
                pendingPackRequests.remove(response.requestId)
                _transferState.value = TransferState.Error(
                    response.message.ifBlank { "Peer failed to access pack '${response.packId}'." },
                    canRetry = true
                )
            }
            "AVAILABLE" -> {
                if (response.filePayloadId <= 0L || response.sha256Checksum.isBlank()) {
                    pendingPackRequests.remove(response.requestId)
                    _transferState.value = TransferState.Error(
                        "Security verification failed: peer provided incomplete or missing pack checksum.",
                        canRetry = false
                    )
                    return
                }

                val meta = IncomingPackMetadata(
                    requestId = response.requestId,
                    packId = response.packId,
                    filePayloadId = response.filePayloadId,
                    fileSizeBytes = response.fileSizeBytes,
                    sha256Checksum = response.sha256Checksum,
                    titleEnglish = response.titleEnglish,
                    peer = peer
                )
                incomingPackMetadataByPayloadId[response.filePayloadId] = meta

                // Check if file payload already finished downloading
                val completedFile = completedIncomingFiles.remove(response.filePayloadId)
                if (completedFile != null) {
                    validateAndImportReceivedPackFile(completedFile, meta, peer)
                }
            }
        }
    }

    // --- Payload Transfer Callbacks ---

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val peer = discoveredEndpoints[endpointId] ?: pendingAuthPeer ?: PeerDevice(id = endpointId, name = "Peer", address = endpointId)

            when (payload.type) {
                Payload.Type.BYTES -> {
                    val bytes = payload.asBytes() ?: return
                    handleReceivedBytes(bytes, peer)
                }
                Payload.Type.FILE -> {
                    incomingFilePayloads[payload.id] = payload
                    val meta = incomingPackMetadataByPayloadId[payload.id]
                    val title = meta?.let { "Receiving '${it.titleEnglish}'..." } ?: "Receiving verified educational package..."
                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = false,
                        progressPercent = 0,
                        currentItemTitle = title
                    )
                }
                Payload.Type.STREAM -> {}
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            val peer = discoveredEndpoints[endpointId] ?: pendingAuthPeer ?: PeerDevice(id = endpointId, name = "Peer", address = endpointId)
            val isOutgoingFile = outgoingFilePayloads.containsKey(update.payloadId)
            val pendingNote = synchronized(pendingNoteTransfersByPayloadId) {
                pendingNoteTransfersByPayloadId[update.payloadId]
            }

            when (update.status) {
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    val total = update.totalBytes
                    val transferred = update.bytesTransferred
                    val percent = if (total > 0) ((transferred * 100) / total).toInt() else 0

                    val title = when {
                        isOutgoingFile -> outgoingFilePayloads[update.payloadId]?.let { "Sending '${it.titleEnglish}'..." } ?: "Sending educational pack..."
                        pendingNote != null -> "Sending note '${pendingNote.note.titleEnglish}'..."
                        else -> {
                            val meta = incomingPackMetadataByPayloadId[update.payloadId]
                            meta?.let { "Receiving '${it.titleEnglish}'..." } ?: "Receiving verified educational package..."
                        }
                    }

                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = isOutgoingFile || (pendingNote != null),
                        progressPercent = percent,
                        currentItemTitle = title
                    )
                }
                PayloadTransferUpdate.Status.SUCCESS -> {
                    when {
                        isOutgoingFile -> {
                            val sentPack = outgoingFilePayloads.remove(update.payloadId)
                            _transferState.value = TransferState.Success(
                                peer = peer,
                                transferredCount = 1,
                                message = "Pack '${sentPack?.titleEnglish ?: "Educational Pack"}' successfully transferred."
                            )
                        }
                        pendingNote != null -> {
                            // Note payload bytes have been delivered by Nearby; keep state in Transferring
                            // until peer application explicitly acknowledges saving the note into database
                            _transferState.value = TransferState.Transferring(
                                peer = peer,
                                isOutgoing = true,
                                progressPercent = 100,
                                currentItemTitle = "Delivered payload. Waiting for ${peer.name} to confirm saving note..."
                            )
                        }
                        else -> {
                            val payload = incomingFilePayloads.remove(update.payloadId)
                            if (payload != null) {
                                val tempZip = File(context.cacheDir, "received_${update.payloadId}.zip")
                                val copied = copyPayloadToFile(payload, tempZip, context)
                                if (copied) {
                                    val meta = incomingPackMetadataByPayloadId.remove(update.payloadId)
                                    if (meta != null) {
                                        validateAndImportReceivedPackFile(tempZip, meta, peer)
                                    } else {
                                        // Wait for incoming metadata packet to correlate
                                        completedIncomingFiles[update.payloadId] = tempZip
                                    }
                                } else {
                                    _transferState.value = TransferState.Error("Failed to extract received file payload.", canRetry = false)
                                }
                            }
                        }
                    }
                }
                PayloadTransferUpdate.Status.FAILURE, PayloadTransferUpdate.Status.CANCELED -> {
                    incomingFilePayloads.remove(update.payloadId)
                    outgoingFilePayloads.remove(update.payloadId)
                    val failedPendingNote = synchronized(pendingNoteTransfersByPayloadId) {
                        pendingNoteTransfersByPayloadId.remove(update.payloadId)
                    }
                    if (failedPendingNote != null) {
                        failedPendingNote.timeoutJob?.cancel()
                        synchronized(pendingNoteTransfersByMessageId) {
                            pendingNoteTransfersByMessageId.remove(failedPendingNote.messageId)
                        }
                    }
                    incomingPackMetadataByPayloadId.remove(update.payloadId)
                    completedIncomingFiles.remove(update.payloadId)?.delete()
                    _transferState.value = TransferState.Error("Transfer interrupted or failed.", canRetry = true)
                }
            }
        }
    }

    private fun copyPayloadToFile(payload: Payload, destFile: File, context: Context): Boolean {
        val fileObj = payload.asFile() ?: return false
        return try {
            val javaFile = fileObj.asJavaFile()
            if (javaFile != null && javaFile.exists()) {
                javaFile.copyTo(destFile, overwrite = true)
                true
            } else {
                val uri = fileObj.asUri()
                if (uri != null) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    true
                } else {
                    val pfd = fileObj.asParcelFileDescriptor()
                    if (pfd != null) {
                        java.io.FileInputStream(pfd.fileDescriptor).use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        } catch (_: Exception) {
            false
        } finally {
            try {
                payload.close()
            } catch (_: Exception) {}
        }
    }

    private fun validateAndImportReceivedPackFile(
        tempFile: File,
        meta: IncomingPackMetadata?,
        peer: PeerDevice
    ) {
        scope.launch(Dispatchers.IO) {
            val destDir = File(context.filesDir, "extracted_packs_${System.currentTimeMillis()}")
            try {
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    _transferState.value = TransferState.Error("Received package archive is missing or empty.", canRetry = false)
                    return@launch
                }

                // Check expected size if metadata was received
                if (meta != null && meta.fileSizeBytes > 0L && tempFile.length() != meta.fileSizeBytes) {
                    _transferState.value = TransferState.Error(
                        "File size mismatch: expected ${meta.fileSizeBytes} bytes, received ${tempFile.length()} bytes.",
                        canRetry = false
                    )
                    return@launch
                }

                // Strict rule: Never treat an absent or invalid checksum as successful verification
                val expectedSha = meta?.sha256Checksum?.trim()
                if (expectedSha.isNullOrEmpty()) {
                    _transferState.value = TransferState.Error(
                        "Security verification failed: missing or empty SHA-256 checksum.",
                        canRetry = false
                    )
                    return@launch
                }

                val computedSha = ZipPackManager.calculateSha256(tempFile)
                if (!computedSha.equals(expectedSha, ignoreCase = true)) {
                    _transferState.value = TransferState.Error(
                        "SHA-256 verification mismatch on received package archive.",
                        canRetry = false
                    )
                    return@launch
                }

                // Path-traversal & zip bomb protected extraction
                val result = ZipPackManager.extractAndVerifyPack(tempFile, destDir)

                if (!result.isChecksumValid) {
                    destDir.deleteRecursively()
                    _transferState.value = TransferState.Error(
                        "Internal archive manifest SHA-256 verification failed.",
                        canRetry = false
                    )
                    return@launch
                }

                repository.saveIncomingLearningPack(result.pack, peer.name)
                onPackReceivedCallback?.invoke(result.pack, peer.name)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Pack '${result.pack.titleEnglish}' safely verified and imported."
                )
            } catch (e: Exception) {
                destDir.deleteRecursively()
                _transferState.value = TransferState.Error("Validation or import failed: ${e.message}", canRetry = false)
            } finally {
                if (meta != null) {
                    pendingPackRequests.remove(meta.requestId)
                }
                try {
                    tempFile.delete()
                } catch (_: Exception) {}
            }
        }
    }

    override fun sendPackToPeer(peer: PeerDevice, pack: LearningPack) {
        val endpointId = connectedEndpointId ?: peer.id
        scope.launch(Dispatchers.IO) {
            try {
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 0,
                    currentItemTitle = "Preparing '${pack.titleEnglish}' for transfer..."
                )

                val packFile = File(context.cacheDir, "outgoing_${pack.packId}.zip")
                ZipPackManager.createPackZip(pack, packFile)
                val sha256 = ZipPackManager.calculateSha256(packFile)

                val filePayload = Payload.fromFile(packFile)
                outgoingFilePayloads[filePayload.id] = pack

                // Send metadata response so receiver can correlate payload ID, size, and SHA-256
                val meta = PackResponseMessage(
                    protocolVersion = 1,
                    requestId = "push_${System.currentTimeMillis()}",
                    packId = pack.packId,
                    status = "AVAILABLE",
                    titleEnglish = pack.titleEnglish,
                    filePayloadId = filePayload.id,
                    fileSizeBytes = packFile.length(),
                    sha256Checksum = sha256
                )
                val metaBytes = encodePackResponse(meta)
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(metaBytes))

                connectionsClient.sendPayload(endpointId, filePayload)
                    .addOnFailureListener { e ->
                        outgoingFilePayloads.remove(filePayload.id)
                        _transferState.value = TransferState.Error(
                            "Failed to transmit file payload: ${e.message}",
                            canRetry = true
                        )
                    }
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Failed to prepare pack file: ${e.message}", canRetry = false)
            }
        }
    }

    override fun sendNoteToPeer(peer: PeerDevice, note: GeneratedNote) {
        val endpointId = connectedEndpointId
        if (endpointId == null || endpointId != peer.id) {
            _transferState.value = TransferState.Error(
                "Peer '${peer.name}' is not currently connected via Nearby.",
                canRetry = true
            )
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val messageId = "msg_note_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                val bytes = encodeNote(note, messageId)
                val payload = Payload.fromBytes(bytes)

                val pending = PendingNoteTransfer(
                    messageId = messageId,
                    note = note,
                    peer = peer,
                    payloadId = payload.id,
                    timestamp = System.currentTimeMillis()
                )

                // Timeout: Do not wait indefinitely for an ACK (15s timeout)
                val timeoutJob = scope.launch(Dispatchers.IO) {
                    delay(15_000L)
                    val timedOut = synchronized(pendingNoteTransfersByMessageId) {
                        pendingNoteTransfersByMessageId.remove(messageId)
                    }
                    if (timedOut != null) {
                        synchronized(pendingNoteTransfersByPayloadId) {
                            pendingNoteTransfersByPayloadId.remove(payload.id)
                        }
                        _transferState.value = TransferState.Error(
                            "Timed out waiting for ${peer.name} to confirm saving note '${note.titleEnglish}'.",
                            canRetry = true
                        )
                    }
                }
                pending.timeoutJob = timeoutJob

                synchronized(pendingNoteTransfersByMessageId) {
                    pendingNoteTransfersByMessageId[messageId] = pending
                }
                synchronized(pendingNoteTransfersByPayloadId) {
                    pendingNoteTransfersByPayloadId[payload.id] = pending
                }

                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 0,
                    currentItemTitle = "Sending note '${note.titleEnglish}'..."
                )

                connectionsClient.sendPayload(endpointId, payload)
                    .addOnFailureListener { e ->
                        timeoutJob.cancel()
                        synchronized(pendingNoteTransfersByMessageId) {
                            pendingNoteTransfersByMessageId.remove(messageId)
                        }
                        synchronized(pendingNoteTransfersByPayloadId) {
                            pendingNoteTransfersByPayloadId.remove(payload.id)
                        }
                        _transferState.value = TransferState.Error(
                            "Failed to send note to peer: ${e.message}",
                            canRetry = true
                        )
                    }
            } catch (e: Exception) {
                _transferState.value = TransferState.Error(
                    "Failed to prepare note payload: ${e.message}",
                    canRetry = false
                )
            }
        }
    }

    override fun requestPackFromPeer(peer: PeerDevice, packId: String) {
        val endpointId = connectedEndpointId
        if (endpointId == null || endpointId != peer.id) {
            _transferState.value = TransferState.Error(
                "Peer '${peer.name}' is not currently connected via Nearby.",
                canRetry = true
            )
            return
        }

        val cleanPackId = packId.trim()
        if (cleanPackId.isBlank()) {
            _transferState.value = TransferState.Error(
                "Pack request failed: packId cannot be empty.",
                canRetry = false
            )
            return
        }

        val requestId = "req_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        pendingPackRequests[requestId] = PendingPackRequest(
            requestId = requestId,
            packId = cleanPackId,
            peer = peer,
            timestamp = System.currentTimeMillis()
        )

        _transferState.value = TransferState.Transferring(
            peer = peer,
            isOutgoing = false,
            progressPercent = 0,
            currentItemTitle = "Requesting educational pack '$cleanPackId'..."
        )

        val bytes = encodePackRequest(requestId, cleanPackId)
        val payload = Payload.fromBytes(bytes)

        connectionsClient.sendPayload(endpointId, payload)
            .addOnFailureListener { e ->
                pendingPackRequests.remove(requestId)
                _transferState.value = TransferState.Error(
                    "Failed to send pack request: ${e.message}",
                    canRetry = true
                )
            }
    }

    override fun cancelTransfer() {
        connectedEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
        connectedEndpointId = null
        pendingAuthPeer = null
        incomingFilePayloads.clear()
        outgoingFilePayloads.clear()

        // Clean up pending note transfers and cancel timeout jobs
        synchronized(pendingNoteTransfersByMessageId) {
            pendingNoteTransfersByMessageId.values.forEach { it.timeoutJob?.cancel() }
            pendingNoteTransfersByMessageId.clear()
        }
        synchronized(pendingNoteTransfersByPayloadId) {
            pendingNoteTransfersByPayloadId.clear()
        }

        pendingPackRequests.clear()
        incomingPackMetadataByPayloadId.clear()
        completedIncomingFiles.values.forEach {
            try { it.delete() } catch (_: Exception) {}
        }
        completedIncomingFiles.clear()
        stopScanning()
        discoveredEndpoints.clear()
        _discoveredPeers.value = emptyList()
        try {
            connectionsClient.stopAllEndpoints()
        } catch (_: Exception) {}
        _transferState.value = TransferState.Idle
    }

    override fun resetToIdle() {
        cancelTransfer()
    }
}
