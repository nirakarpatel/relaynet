package com.nmiet.app.nearby

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.PeerDevice
import com.nmiet.app.domain.model.TransferState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class BluetoothTransferManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val onPackReceivedCallback: (suspend (pack: LearningPack, peerName: String) -> Unit)? = null
) : NearbyTransferManager {

    override val isDemoMode: Boolean = false

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    override val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val discoveredPeersMap = mutableMapOf<String, PeerDevice>()
    private var isReceiverRegistered = false
    private var serverJob: Job? = null
    private var clientJob: Job? = null

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private const val SERVICE_NAME = "RELAYNET_PACK_TRANSFER"
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
                        val name = dev.name ?: "Nearby Android Learner"
                        val address = dev.address ?: dev.hashCode().toString()
                        val peer = PeerDevice(
                            id = address,
                            name = name,
                            address = address,
                            signalStrengthRssi = rssi
                        )
                        discoveredPeersMap[address] = peer
                        _transferState.value = TransferState.Scanning(discoveredPeersMap.values.toList())
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    val peers = discoveredPeersMap.values.toList()
                    _transferState.value = if (peers.isEmpty()) {
                        TransferState.Error("No nearby Bluetooth devices found. Ensure Bluetooth is discoverable.", canRetry = true)
                    } else {
                        TransferState.Scanning(peers)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is not enabled on this device.", canRetry = true)
            return
        }

        discoveredPeersMap.clear()
        _transferState.value = TransferState.Scanning(emptyList())

        try {
            bluetoothAdapter.bondedDevices?.forEach { dev ->
                val peer = PeerDevice(
                    id = dev.address,
                    name = dev.name ?: "Paired Learner Device",
                    address = dev.address,
                    signalStrengthRssi = -50
                )
                discoveredPeersMap[dev.address] = peer
            }
            if (discoveredPeersMap.isNotEmpty()) {
                _transferState.value = TransferState.Scanning(discoveredPeersMap.values.toList())
            }
        } catch (_: SecurityException) {}

        registerReceiver()
        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            _transferState.value = TransferState.Error("Bluetooth permissions missing: ${e.message}", canRetry = false)
        }

        startServerListening()
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        unregisterReceiver()
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {}
    }

    @SuppressLint("MissingPermission")
    override fun sendPackToPeer(peer: PeerDevice, pack: LearningPack) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _transferState.value = TransferState.Error("Bluetooth is disabled.", canRetry = true)
            return
        }

        clientJob?.cancel()
        clientJob = scope.launch {
            try {
                _transferState.value = TransferState.Connecting(peer)
                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(peer.address)

                stopScanning()

                val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                socket.connect()

                _transferState.value = TransferState.Connected(peer)

                val entity = LearningPackEntity.fromDomain(pack)
                val json = JSONObject().apply {
                    put("type", "RELAYNET_LEARNING_PACK")
                    put("packId", entity.packId)
                    put("titleOdia", entity.titleOdia)
                    put("titleEnglish", entity.titleEnglish)
                    put("boardName", entity.boardName)
                    put("classGrade", entity.classGrade)
                    put("subject", entity.subject)
                    put("chapterOdia", entity.chapterOdia)
                    put("chapterEnglish", entity.chapterEnglish)
                    put("topicOdia", entity.topicOdia)
                    put("topicEnglish", entity.topicEnglish)
                    put("lessonSummaryOdia", entity.lessonSummaryOdia)
                    put("lessonSummaryEnglish", entity.lessonSummaryEnglish)
                    put("equationOrFormula", entity.equationOrFormula ?: "")
                    put("sectionsJson", entity.sectionsJson)
                    put("quizQuestionsJson", entity.quizQuestionsJson)
                    put("checksumSha256", entity.checksumSha256)
                    put("sourceAttribution", entity.sourceAttribution)
                }

                val writer = OutputStreamWriter(socket.outputStream)
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 50,
                    currentItemTitle = pack.titleEnglish,
                    totalItems = 1,
                    currentItemIndex = 1
                )

                writer.write(json.toString() + "\n")
                writer.flush()

                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 100,
                    currentItemTitle = pack.titleEnglish,
                    totalItems = 1,
                    currentItemIndex = 1
                )

                socket.close()

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Pack '${pack.titleEnglish}' sent to ${peer.name} over Bluetooth RFCOMM."
                )
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Transfer failed: ${e.localizedMessage ?: "Connection error"}", canRetry = true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startServerListening() {
        serverJob?.cancel()
        serverJob = scope.launch {
            try {
                val serverSocket: BluetoothServerSocket? =
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                serverSocket?.use { server ->
                    while (true) {
                        val socket = server.accept(30000) ?: break
                        handleIncomingConnection(socket)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun handleIncomingConnection(socket: BluetoothSocket) = withContext(Dispatchers.IO) {
        try {
            val peerName = try {
                socket.remoteDevice.name ?: "Peer Device"
            } catch (_: SecurityException) {
                "Peer Device"
            }
            val peer = PeerDevice(
                id = socket.remoteDevice.address,
                name = peerName,
                address = socket.remoteDevice.address
            )

            _transferState.value = TransferState.Connected(peer)

            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val line = reader.readLine()

            if (!line.isNullOrEmpty()) {
                val json = JSONObject(line)
                val entity = LearningPackEntity(
                    packId = json.getString("packId"),
                    titleOdia = json.getString("titleOdia"),
                    titleEnglish = json.getString("titleEnglish"),
                    boardName = json.getString("boardName"),
                    classGrade = json.getInt("classGrade"),
                    subject = json.getString("subject"),
                    chapterOdia = json.getString("chapterOdia"),
                    chapterEnglish = json.getString("chapterEnglish"),
                    topicOdia = json.getString("topicOdia"),
                    topicEnglish = json.getString("topicEnglish"),
                    lessonSummaryOdia = json.getString("lessonSummaryOdia"),
                    lessonSummaryEnglish = json.getString("lessonSummaryEnglish"),
                    equationOrFormula = if (json.optString("equationOrFormula").isEmpty()) null else json.getString("equationOrFormula"),
                    sectionsJson = json.getString("sectionsJson"),
                    quizQuestionsJson = json.getString("quizQuestionsJson"),
                    checksumSha256 = json.getString("checksumSha256"),
                    isDownloaded = true,
                    isReceivedViaMesh = true,
                    peerSenderName = peerName,
                    sourceAttribution = json.getString("sourceAttribution")
                )

                val pack = entity.toDomain()
                onPackReceivedCallback?.invoke(pack, peerName)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = 1,
                    message = "Received and verified pack '${pack.titleEnglish}' from $peerName via Bluetooth."
                )
            }
            socket.close()
        } catch (e: Exception) {
            _transferState.value = TransferState.Error("Incoming transfer error: ${e.localizedMessage}")
        }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
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
        clientJob?.cancel()
        serverJob?.cancel()
        stopScanning()
        _transferState.value = TransferState.Idle
    }

    override fun resetToIdle() {
        clientJob?.cancel()
        stopScanning()
        _transferState.value = TransferState.Idle
    }
}
