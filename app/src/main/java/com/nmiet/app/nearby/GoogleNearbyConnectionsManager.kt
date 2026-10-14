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
import com.nmiet.app.data.local.ZipPackManager
import com.nmiet.app.domain.model.ContentManifestItem
import com.nmiet.app.domain.model.ContentMediaType
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerContentManifest
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.permissions.NearbyPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
    private val onPackReceivedCallback: (suspend (pack: LearningPack, peerName: String) -> Unit)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = false

    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context.applicationContext)
    }

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val discoveredEndpoints = mutableMapOf<String, PeerDevice>()
    private var connectedEndpointId: String? = null
    private var pendingAuthPeer: PeerDevice? = null

    // Track active incoming and outgoing file payloads
    private val incomingFilePayloads = mutableMapOf<Long, Payload>()
    private val outgoingFilePayloads = mutableMapOf<Long, LearningPack>()

    companion object {
        const val SERVICE_ID = "com.nmiet.app.relaynet.NEARBY_SERVICE"
        val STRATEGY: Strategy = Strategy.P2P_STAR
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
            _transferState.value = TransferState.Error(
                "Failed to start Nearby Discovery: ${e.localizedMessage ?: "Unknown error"}",
                canRetry = true
            )
        }
    }

    override fun stopScanning() {
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
            _transferState.value = TransferState.Scanning(discoveredEndpoints.values.toList())
        }

        override fun onEndpointLost(endpointId: String) {
            discoveredEndpoints.remove(endpointId)
            _transferState.value = TransferState.Scanning(discoveredEndpoints.values.toList())
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
        scope.launch {
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
        scope.launch {
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

    // --- Payload Transfer Callbacks ---

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val peer = discoveredEndpoints[endpointId] ?: pendingAuthPeer ?: PeerDevice(id = endpointId, name = "Peer", address = endpointId)

            when (payload.type) {
                Payload.Type.BYTES -> {
                    val bytes = payload.asBytes() ?: return
                    handleReceivedManifest(bytes, peer)
                }
                Payload.Type.FILE -> {
                    incomingFilePayloads[payload.id] = payload
                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = false,
                        progressPercent = 0,
                        currentItemTitle = "Receiving verified educational package..."
                    )
                }
                Payload.Type.STREAM -> {}
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            val peer = discoveredEndpoints[endpointId] ?: pendingAuthPeer ?: PeerDevice(id = endpointId, name = "Peer", address = endpointId)
            val isOutgoing = outgoingFilePayloads.containsKey(update.payloadId)

            when (update.status) {
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    val total = update.totalBytes
                    val transferred = update.bytesTransferred
                    val percent = if (total > 0) ((transferred * 100) / total).toInt() else 0

                    val title = if (isOutgoing) {
                        outgoingFilePayloads[update.payloadId]?.let { "Sending '${it.titleEnglish}'..." } ?: "Sending educational pack..."
                    } else {
                        "Receiving verified educational pack..."
                    }

                    _transferState.value = TransferState.Transferring(
                        peer = peer,
                        isOutgoing = isOutgoing,
                        progressPercent = percent,
                        currentItemTitle = title
                    )
                }
                PayloadTransferUpdate.Status.SUCCESS -> {
                    if (isOutgoing) {
                        val sentPack = outgoingFilePayloads.remove(update.payloadId)
                        _transferState.value = TransferState.Success(
                            peer = peer,
                            transferredCount = 1,
                            message = "Pack '${sentPack?.titleEnglish ?: "Educational Pack"}' successfully transferred."
                        )
                    } else {
                        val payload = incomingFilePayloads.remove(update.payloadId)
                        if (payload != null) {
                            val tempZip = File(context.cacheDir, "received_${update.payloadId}.zip")
                            val copied = copyPayloadToFile(payload, tempZip, context)
                            if (copied) {
                                validateAndImportReceivedFile(tempZip, peer)
                            } else {
                                _transferState.value = TransferState.Error("Failed to extract received file payload.", canRetry = false)
                            }
                        } else {
                            _transferState.value = TransferState.Success(
                                peer = peer,
                                transferredCount = 1,
                                message = "Transfer completed."
                            )
                        }
                    }
                }
                PayloadTransferUpdate.Status.FAILURE, PayloadTransferUpdate.Status.CANCELED -> {
                    incomingFilePayloads.remove(update.payloadId)
                    outgoingFilePayloads.remove(update.payloadId)
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

    private fun validateAndImportReceivedFile(tempFile: File, peer: PeerDevice) {
        scope.launch {
            try {
                val destDir = File(context.filesDir, "extracted_packs_${System.currentTimeMillis()}")
                val result = ZipPackManager.extractAndVerifyPack(tempFile, destDir)

                if (!result.isChecksumValid) {
                    _transferState.value = TransferState.Error("SHA-256 verification failed on received package.", canRetry = false)
                    tempFile.delete()
                    return@launch
                }

                repository.saveIncomingLearningPack(result.pack, peer.name)
                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Pack '${result.pack.titleEnglish}' safely verified and imported."
                )
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Validation failed: ${e.message}", canRetry = false)
            } finally {
                tempFile.delete()
            }
        }
    }

    override fun sendPackToPeer(peer: PeerDevice, pack: LearningPack) {
        val endpointId = connectedEndpointId ?: peer.id
        scope.launch {
            try {
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 0,
                    currentItemTitle = "Preparing '${pack.titleEnglish}' for transfer..."
                )

                val packFile = File(context.cacheDir, "outgoing_${pack.packId}.zip")
                ZipPackManager.createPackZip(pack, packFile)

                val filePayload = Payload.fromFile(packFile)
                outgoingFilePayloads[filePayload.id] = pack

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

    override fun cancelTransfer() {
        connectedEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
        connectedEndpointId = null
        pendingAuthPeer = null
        incomingFilePayloads.clear()
        outgoingFilePayloads.clear()
        stopScanning()
        try {
            connectionsClient.stopAllEndpoints()
        } catch (_: Exception) {}
        _transferState.value = TransferState.Idle
    }

    override fun resetToIdle() {
        cancelTransfer()
    }
}
