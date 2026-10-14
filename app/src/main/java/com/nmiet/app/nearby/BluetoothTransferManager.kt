package com.nmiet.app.nearby

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import com.nmiet.app.data.local.GeneratedNoteEntity
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class BluetoothTransferManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val onPackReceivedCallback: (suspend (pack: LearningPack, peerName: String) -> Unit)? = null,
    private val onNoteReceivedCallback: (suspend (note: GeneratedNote, peerName: String) -> Unit)? = null,
    private val packLookupProvider: (suspend (packId: String) -> LearningPack?)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = false

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    override val discoveredPeers: StateFlow<List<PeerDevice>> = _discoveredPeers.asStateFlow()

    private val discoveredPeersMap = mutableMapOf<String, PeerDevice>()
    private var isReceiverRegistered = false
    private var isBleScanning = false
    private var isBleAdvertising = false
    private var serverJob: Job? = null
    private var activeTransferJob: Job? = null

    init {
        loadBondedDevices()
    }

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        val PARCEL_SERVICE_UUID: ParcelUuid = ParcelUuid(SERVICE_UUID)
        private const val SERVICE_NAME = "RELAYNET_PACK_TRANSFER"

        private const val MAGIC_0: Byte = 0x52 // 'R'
        private const val MAGIC_1: Byte = 0x4E // 'N'
        private const val TYPE_PACK: Byte = 1
        private const val TYPE_NOTE: Byte = 2
        private const val TYPE_REQUEST: Byte = 3

        private const val ACK_SUCCESS: Byte = 0x06
        private const val ACK_FAILURE: Byte = 0x15

        private const val CHUNK_SIZE = 2048
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for x86") ||
               Build.MANUFACTURER.contains("Genymotion") ||
               Build.HARDWARE.contains("goldfish") ||
               Build.HARDWARE.contains("ranchu") ||
               Build.PRODUCT.contains("sdk_gphone")
    }

    @SuppressLint("MissingPermission")
    fun loadBondedDevices() {
        try {
            val bonded = bluetoothAdapter?.bondedDevices ?: emptySet()
            for (dev in bonded) {
                val rawName = try { dev.name } catch (_: SecurityException) { null }
                val displayName = if (!rawName.isNullOrBlank()) rawName else "Paired Device (${dev.address.takeLast(5)})"
                discoveredPeersMap[dev.address] = PeerDevice(
                    id = dev.address,
                    name = "$displayName [Paired]",
                    address = dev.address,
                    signalStrengthRssi = -50
                )
            }
            if (discoveredPeersMap.isNotEmpty()) {
                _discoveredPeers.value = discoveredPeersMap.values.toList()
            }
        } catch (_: Exception) {}
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device ?: return
            val devName = try {
                device.name?.ifBlank { null }
                    ?: result.scanRecord?.deviceName
                    ?: "Bluetooth Device (${device.address.takeLast(5)})"
            } catch (_: SecurityException) {
                "Bluetooth Device (${device.address.takeLast(5)})"
            }

            val peer = PeerDevice(
                id = device.address,
                name = devName,
                address = device.address,
                signalStrengthRssi = result.rssi
            )
            discoveredPeersMap[device.address] = peer
            _discoveredPeers.value = discoveredPeersMap.values.toList()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { onScanResult(0, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            // BLE scan error
        }
    }

    private val bleAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isBleAdvertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            isBleAdvertising = false
        }
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    @Suppress("DEPRECATION")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (-60).toShort()).toInt()

                    device?.let { dev ->
                        val rawName = try { dev.name } catch (_: SecurityException) { null }
                        val name = if (!rawName.isNullOrBlank()) {
                            rawName
                        } else {
                            "Bluetooth Device (${dev.address.takeLast(5)})"
                        }

                        val peer = PeerDevice(
                            id = dev.address,
                            name = name,
                            address = dev.address,
                            signalStrengthRssi = rssi
                        )
                        discoveredPeersMap[dev.address] = peer
                        _discoveredPeers.value = discoveredPeersMap.values.toList()
                    }
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    @Suppress("DEPRECATION")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { dev ->
                        val rawName = try { dev.name } catch (_: SecurityException) { null }
                        if (!rawName.isNullOrBlank()) {
                            discoveredPeersMap[dev.address]?.let { existing ->
                                discoveredPeersMap[dev.address] = existing.copy(name = rawName)
                                _discoveredPeers.value = discoveredPeersMap.values.toList()
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Classic discovery finished, BLE scan may continue
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is disabled. Please turn on Bluetooth.", canRetry = true)
            return
        }

        discoveredPeersMap.clear()
        _discoveredPeers.value = emptyList()
        _isScanning.value = true
        if (_transferState.value is TransferState.Scanning || _transferState.value is TransferState.Error) {
            _transferState.value = TransferState.Idle
        }

        // Always load existing bonded/paired devices first so they are immediately available
        loadBondedDevices()

        // If running in an Android Emulator where physical Bluetooth radio doesn't exist, provide a virtual test peer
        if (isEmulator() && discoveredPeersMap.isEmpty()) {
            val virtualPeer = PeerDevice(
                id = "emulator-virtual-hub",
                name = "RelayNet Virtual Node (Emulator Test)",
                address = "02:00:00:00:00:01",
                signalStrengthRssi = -42
            )
            discoveredPeersMap[virtualPeer.address] = virtualPeer
            _discoveredPeers.value = discoveredPeersMap.values.toList()
        }

        // Start BLE Advertising so other RelayNet devices can find this phone
        startBleAdvertising()

        // Start BLE Scanning
        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner != null) {
            try {
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                scanner.startScan(null, scanSettings, bleScanCallback)
                isBleScanning = true
            } catch (_: Exception) {}
        }

        // Register classic receiver for all nearby Bluetooth devices
        registerReceiver()
        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        } catch (_: Exception) {}

        // Start background RFCOMM server to accept transfers from any connecting peer
        startServerListening()
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        _isScanning.value = false

        // 1. Stop BLE Scanning
        if (isBleScanning) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
            } catch (_: Exception) {}
            isBleScanning = false
        }

        // 2. Cancel Classic Discovery
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (_: Exception) {}

        // 3. Unregister receiver
        unregisterReceiver()

        // 4. Reset state to Idle (stops radar pulse, preserves discovered peers)
        if (_transferState.value is TransferState.Scanning) {
            _transferState.value = TransferState.Idle
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleAdvertising() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled || isBleAdvertising) return
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(0) // Advertise continuously while on screen
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(PARCEL_SERVICE_UUID)
                .build()

            advertiser.startAdvertising(settings, data, bleAdvertiseCallback)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun stopBleAdvertising() {
        if (!isBleAdvertising) return
        try {
            bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(bleAdvertiseCallback)
        } catch (_: Exception) {}
        isBleAdvertising = false
    }

    @SuppressLint("MissingPermission")
    fun startServerListening() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (serverJob?.isActive == true) return

        startBleAdvertising()

        serverJob = scope.launch(Dispatchers.IO) {
            var serverSocket: BluetoothServerSocket? = null
            try {
                // Insecure RFCOMM avoids Android pairing PIN dialogs for seamless peer data transfer
                serverSocket = try {
                    bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                } catch (_: Exception) {
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                }

                while (isActive) {
                    val socket: BluetoothSocket? = try {
                        serverSocket.accept()
                    } catch (e: IOException) {
                        break
                    }
                    if (socket != null) {
                        scope.launch(Dispatchers.IO) {
                            handleIncomingConnection(socket)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { serverSocket?.close() } catch (_: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendPackToPeer(peer: PeerDevice, pack: LearningPack) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is disabled. Please turn on Bluetooth.", canRetry = true)
            return
        }

        activeTransferJob?.cancel()
        activeTransferJob = scope.launch(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                _transferState.value = TransferState.Connecting(peer)

                // Critical: Stop scanning and discovery before connecting to free Bluetooth radio bandwidth
                stopScanning()
                delay(120)

                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(peer.address)

                // Try insecure RFCOMM first (no OS pairing dialog), then fallback
                socket = try {
                    device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
                } catch (_: Exception) {
                    device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                }

                socket.connect()

                _transferState.value = TransferState.Connected(peer)
                delay(100)

                // Serialize LearningPack model
                val entity = LearningPackEntity.fromDomain(pack)
                val json = JSONObject().apply {
                    put("type", "RELAYNET_LEARNING_PACK")
                    put("packId", entity.packId)
                    put("title", entity.title)
                    put("titleOdia", entity.titleOdia)
                    put("titleEnglish", entity.titleEnglish)
                    put("board", entity.board)
                    put("grade", entity.grade)
                    put("subject", entity.subject)
                    put("chapterOdia", entity.chapterOdia)
                    put("chapterEnglish", entity.chapterEnglish)
                    put("topicOdia", entity.topicOdia)
                    put("topicEnglish", entity.topicEnglish)
                    put("odiaContentJson", entity.odiaContentJson)
                    put("englishContentJson", entity.englishContentJson)
                    put("equationOrFormula", entity.equationOrFormula ?: "")
                    put("sectionsJson", entity.sectionsJson)
                    put("quizQuestionsJson", entity.quizQuestionsJson)
                    put("sha256Hash", entity.sha256Checksum)
                    put("sourceAttribution", entity.sourceAttribution)
                }

                val payloadBytes = json.toString().toByteArray(Charsets.UTF_8)
                val sha256 = calculateSha256(payloadBytes)

                val dos = DataOutputStream(socket.outputStream)
                val dis = DataInputStream(socket.inputStream)

                // Header Framing
                dos.writeByte(MAGIC_0.toInt())
                dos.writeByte(MAGIC_1.toInt())
                dos.writeByte(TYPE_PACK.toInt())
                dos.writeInt(payloadBytes.size)
                dos.writeUTF(sha256)
                dos.flush()

                // Live Chunk Streaming
                streamPayloadInChunks(
                    dos = dos,
                    payloadBytes = payloadBytes,
                    peer = peer,
                    itemTitle = pack.titleEnglish
                )

                // Wait for Receiver's SHA-256 ACK
                val ack = dis.readByte()
                if (ack == ACK_SUCCESS) {
                    _transferState.value = TransferState.Success(
                        peer = peer,
                        transferredCount = 1,
                        message = "Pack '${pack.titleEnglish}' delivered to ${peer.name} via Bluetooth RFCOMM • SHA-256 Verified!"
                    )
                } else {
                    _transferState.value = TransferState.Error("Peer reported checksum validation error during transfer.", canRetry = true)
                }
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Transfer failed: ${e.localizedMessage ?: "Could not connect to peer. Make sure app is open on both devices."}", canRetry = true)
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendNoteToPeer(peer: PeerDevice, note: GeneratedNote) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is disabled.", canRetry = true)
            return
        }

        activeTransferJob?.cancel()
        activeTransferJob = scope.launch(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                _transferState.value = TransferState.Connecting(peer)
                stopScanning()
                delay(120)

                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(peer.address)
                socket = try {
                    device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
                } catch (_: Exception) {
                    device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                }
                socket.connect()

                _transferState.value = TransferState.Connected(peer)
                delay(100)

                val entity = GeneratedNoteEntity.fromDomain(note)
                val json = JSONObject().apply {
                    put("type", "RELAYNET_GENERATED_NOTE")
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

                val payloadBytes = json.toString().toByteArray(Charsets.UTF_8)
                val sha256 = calculateSha256(payloadBytes)

                val dos = DataOutputStream(socket.outputStream)
                val dis = DataInputStream(socket.inputStream)

                dos.writeByte(MAGIC_0.toInt())
                dos.writeByte(MAGIC_1.toInt())
                dos.writeByte(TYPE_NOTE.toInt())
                dos.writeInt(payloadBytes.size)
                dos.writeUTF(sha256)
                dos.flush()

                streamPayloadInChunks(
                    dos = dos,
                    payloadBytes = payloadBytes,
                    peer = peer,
                    itemTitle = "${note.titleEnglish} (AI Study Note)"
                )

                val ack = dis.readByte()
                if (ack == ACK_SUCCESS) {
                    _transferState.value = TransferState.Success(
                        peer = peer,
                        transferredCount = 1,
                        message = "AI Note '${note.titleEnglish}' delivered to ${peer.name} via Bluetooth RFCOMM • SHA-256 Verified!"
                    )
                } else {
                    _transferState.value = TransferState.Error("Peer reported checksum validation error.", canRetry = true)
                }
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Transfer failed: ${e.localizedMessage ?: "Connection error"}", canRetry = true)
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestPackFromPeer(peer: PeerDevice, packId: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is disabled.", canRetry = true)
            return
        }

        activeTransferJob?.cancel()
        activeTransferJob = scope.launch(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                _transferState.value = TransferState.Connecting(peer)
                stopScanning()
                delay(120)

                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(peer.address)
                socket = try {
                    device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
                } catch (_: Exception) {
                    device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                }
                socket.connect()

                _transferState.value = TransferState.Connected(peer)

                val dos = DataOutputStream(socket.outputStream)
                val dis = DataInputStream(socket.inputStream)

                dos.writeByte(MAGIC_0.toInt())
                dos.writeByte(MAGIC_1.toInt())
                dos.writeByte(TYPE_REQUEST.toInt())
                dos.writeInt(packId.toByteArray(Charsets.UTF_8).size)
                dos.writeUTF(packId)
                dos.flush()

                handleIncomingStream(dis, dos, peer)
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Pull request failed: ${e.localizedMessage ?: "Could not connect to peer."}", canRetry = true)
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }

    private suspend fun streamPayloadInChunks(
        dos: DataOutputStream,
        payloadBytes: ByteArray,
        peer: PeerDevice,
        itemTitle: String
    ) {
        var offset = 0
        val total = payloadBytes.size

        while (offset < total) {
            val count = minOf(CHUNK_SIZE, total - offset)
            dos.write(payloadBytes, offset, count)
            offset += count

            val progress = ((offset.toDouble() / total.toDouble()) * 100).toInt().coerceIn(1, 99)
            _transferState.value = TransferState.Transferring(
                peer = peer,
                isOutgoing = true,
                progressPercent = progress,
                currentItemTitle = "$itemTitle (${offset / 1024} KB / ${total / 1024} KB)",
                totalItems = 1,
                currentItemIndex = 1
            )
            delay(10)
        }
        dos.flush()

        _transferState.value = TransferState.Transferring(
            peer = peer,
            isOutgoing = true,
            progressPercent = 100,
            currentItemTitle = itemTitle,
            totalItems = 1,
            currentItemIndex = 1
        )
    }

    private suspend fun handleIncomingConnection(socket: BluetoothSocket) = withContext(Dispatchers.IO) {
        try {
            val peerName = try {
                socket.remoteDevice?.name?.ifBlank { "RelayNet Peer" } ?: "RelayNet Peer"
            } catch (_: SecurityException) {
                "RelayNet Peer"
            }
            val peerAddress = try {
                socket.remoteDevice?.address ?: "Unknown"
            } catch (_: SecurityException) {
                "Unknown"
            }

            val peer = PeerDevice(
                id = peerAddress,
                name = peerName,
                address = peerAddress
            )

            val dis = DataInputStream(socket.inputStream)
            val dos = DataOutputStream(socket.outputStream)

            handleIncomingStream(dis, dos, peer)
        } catch (e: Exception) {
            _transferState.value = TransferState.Error("Incoming connection failed: ${e.localizedMessage}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private suspend fun handleIncomingStream(
        dis: DataInputStream,
        dos: DataOutputStream,
        peer: PeerDevice
    ) {
        _transferState.value = TransferState.Connected(peer)
        delay(100)

        val b0 = dis.readByte()
        val b1 = dis.readByte()

        if (b0 == MAGIC_0 && b1 == MAGIC_1) {
            val type = dis.readByte()
            val payloadSize = dis.readInt()
            val expectedSha256 = dis.readUTF()

            if (type == TYPE_REQUEST) {
                val packId = expectedSha256
                val pack = packLookupProvider?.invoke(packId)
                if (pack != null) {
                    val entity = LearningPackEntity.fromDomain(pack)
                    val json = JSONObject().apply {
                        put("type", "RELAYNET_LEARNING_PACK")
                        put("packId", entity.packId)
                        put("title", entity.title)
                        put("titleOdia", entity.titleOdia)
                        put("titleEnglish", entity.titleEnglish)
                        put("board", entity.board)
                        put("grade", entity.grade)
                        put("subject", entity.subject)
                        put("chapterOdia", entity.chapterOdia)
                        put("chapterEnglish", entity.chapterEnglish)
                        put("topicOdia", entity.topicOdia)
                        put("topicEnglish", entity.topicEnglish)
                        put("odiaContentJson", entity.odiaContentJson)
                        put("englishContentJson", entity.englishContentJson)
                        put("equationOrFormula", entity.equationOrFormula ?: "")
                        put("sectionsJson", entity.sectionsJson)
                        put("quizQuestionsJson", entity.quizQuestionsJson)
                        put("sha256Hash", entity.sha256Checksum)
                        put("sourceAttribution", entity.sourceAttribution)
                    }
                    val respBytes = json.toString().toByteArray(Charsets.UTF_8)
                    val respSha = calculateSha256(respBytes)

                    dos.writeByte(MAGIC_0.toInt())
                    dos.writeByte(MAGIC_1.toInt())
                    dos.writeByte(TYPE_PACK.toInt())
                    dos.writeInt(respBytes.size)
                    dos.writeUTF(respSha)
                    dos.flush()

                    streamPayloadInChunks(dos, respBytes, peer, pack.titleEnglish)
                }
                return
            }

            val baos = ByteArrayOutputStream(payloadSize)
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesReadTotal = 0

            while (bytesReadTotal < payloadSize) {
                val toRead = minOf(buffer.size, payloadSize - bytesReadTotal)
                val count = dis.read(buffer, 0, toRead)
                if (count == -1) break
                baos.write(buffer, 0, count)
                bytesReadTotal += count

                val progress = ((bytesReadTotal.toDouble() / payloadSize.toDouble()) * 100).toInt().coerceIn(1, 99)
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = false,
                    progressPercent = progress,
                    currentItemTitle = "Receiving Model (${bytesReadTotal / 1024} KB / ${payloadSize / 1024} KB)",
                    totalItems = 1,
                    currentItemIndex = 1
                )
            }

            val receivedBytes = baos.toByteArray()
            val computedSha256 = calculateSha256(receivedBytes)
            val isVerified = computedSha256.equals(expectedSha256, ignoreCase = true)

            dos.writeByte(if (isVerified) ACK_SUCCESS.toInt() else ACK_FAILURE.toInt())
            dos.flush()

            if (!isVerified) {
                _transferState.value = TransferState.Error("SHA-256 verification mismatch for incoming payload.", canRetry = true)
                return
            }

            val jsonString = String(receivedBytes, Charsets.UTF_8)
            val json = JSONObject(jsonString)

            if (type == TYPE_PACK) {
                val titleEng = json.optString("titleEnglish", json.optString("title", "Photosynthesis"))
                val entity = LearningPackEntity(
                    packId = json.getString("packId"),
                    titleOdia = json.optString("titleOdia", "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)"),
                    titleEnglish = titleEng,
                    subject = json.optString("subject", "Science (ବିଜ୍ଞାନ)"),
                    chapterNumber = json.optInt("chapterNumber", 4),
                    origin = com.nmiet.app.domain.model.ContentOrigin.P2P_RECEIVED,
                    senderPeerName = peer.name,
                    receivedTimestamp = System.currentTimeMillis(),
                    sha256Checksum = computedSha256,
                    isCachedLocally = true,
                    board = json.optString("board", "ODISHA_STATE"),
                    grade = json.optInt("grade", 9),
                    title = titleEng,
                    odiaContentJson = json.optString("odiaContentJson", ""),
                    englishContentJson = json.optString("englishContentJson", ""),
                    chapterOdia = json.optString("chapterOdia", ""),
                    chapterEnglish = json.optString("chapterEnglish", ""),
                    topicOdia = json.optString("topicOdia", ""),
                    topicEnglish = json.optString("topicEnglish", ""),
                    equationOrFormula = if (json.optString("equationOrFormula").isEmpty()) null else json.getString("equationOrFormula"),
                    sectionsJson = json.optString("sectionsJson", "[]"),
                    quizQuestionsJson = json.optString("quizQuestionsJson", "[]"),
                    sourceAttribution = json.optString("sourceAttribution", "P2P Bluetooth Peer")
                )

                val pack = entity.toDomain()
                onPackReceivedCallback?.invoke(pack, peer.name)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Received and verified Learning Pack '${pack.titleEnglish}' from ${peer.name} via Bluetooth RFCOMM."
                )
            } else if (type == TYPE_NOTE) {
                val entity = GeneratedNoteEntity(
                    noteId = json.getString("noteId"),
                    topicId = json.optString("topicId", "topic_custom"),
                    topicName = json.optString("topicName", "P2P Shared Note"),
                    titleOdia = json.optString("titleOdia", ""),
                    titleEnglish = json.optString("titleEnglish", "AI Study Note"),
                    contentJson = json.optString("contentJson", "{}"),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    isBookmarked = true,
                    isOfflineGenerated = true
                )
                val note = entity.toDomain()
                onNoteReceivedCallback?.invoke(note, peer.name)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Received and verified AI Study Note '${note.titleEnglish}' from ${peer.name} via Bluetooth RFCOMM."
                )
            }
        }
    }

    private fun calculateSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_NAME_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.registerReceiver(discoveryReceiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(discoveryReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    override fun cancelTransfer() {
        activeTransferJob?.cancel()
        stopScanning()
        _transferState.value = TransferState.Idle
    }

    override fun resetToIdle() {
        activeTransferJob?.cancel()
        stopScanning()
        _transferState.value = TransferState.Idle
    }
}
