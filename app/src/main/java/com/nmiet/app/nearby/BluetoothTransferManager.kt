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
import com.nmiet.app.domain.model.ContentItem
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class BluetoothTransferManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val onContentReceivedCallback: (suspend (items: List<ContentItem>, peerName: String) -> Unit)? = null
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
        private const val SERVICE_NAME = "NMIET_OFFLINE_TRANSFER"
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (-60).toShort()).toInt()

                    device?.let { dev ->
                        val name = dev.name ?: "Nearby Android Device"
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
                        TransferState.Error("No nearby Bluetooth peers found. Ensure nearby devices are discoverable.", canRetry = true)
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

        // Add currently paired/bonded devices right away
        try {
            bluetoothAdapter.bondedDevices?.forEach { dev ->
                val peer = PeerDevice(
                    id = dev.address,
                    name = dev.name ?: "Paired Device",
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

        // Also start listening for incoming transfers from peers
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
    override fun sendContentToPeer(peer: PeerDevice, items: List<ContentItem>) {
        if (items.isEmpty()) {
            _transferState.value = TransferState.Error("No items selected to share.", canRetry = false)
            return
        }
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

                // Serialize payload to JSON
                val jsonArray = JSONArray()
                items.forEach { item ->
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("summary", item.summary)
                        put("body", item.body)
                        put("category", item.category)
                        put("source", item.source)
                        put("publishedAt", item.publishedAt)
                        put("contentHash", item.contentHash)
                    }
                    jsonArray.put(obj)
                }

                val payload = JSONObject().apply {
                    put("type", "CONTENT_PAYLOAD")
                    put("version", 1)
                    put("items", jsonArray)
                }.toString()

                val writer = OutputStreamWriter(socket.outputStream)
                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 50,
                    currentItemTitle = items.first().title,
                    totalItems = items.size,
                    currentItemIndex = 1
                )

                writer.write(payload + "\n")
                writer.flush()

                _transferState.value = TransferState.Transferring(
                    peer = peer,
                    isOutgoing = true,
                    progressPercent = 100,
                    currentItemTitle = items.last().title,
                    totalItems = items.size,
                    currentItemIndex = items.size
                )

                socket.close()

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = items.size,
                    message = "Successfully transferred ${items.size} item(s) to ${peer.name} via Bluetooth RFCOMM."
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
                val jsonArray = json.getJSONArray("items")
                val receivedItems = mutableListOf<ContentItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    receivedItems.add(
                        ContentItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            summary = obj.getString("summary"),
                            body = obj.getString("body"),
                            category = obj.getString("category"),
                            source = obj.getString("source"),
                            publishedAt = obj.getLong("publishedAt"),
                            contentHash = obj.getString("contentHash"),
                            isSavedOffline = true,
                            isReceivedViaNearby = true,
                            receivedFromPeerName = peerName
                        )
                    )
                }

                onContentReceivedCallback?.invoke(receivedItems, peerName)

                _transferState.value = TransferState.Success(
                    peer = peer,
                    transferredCount = receivedItems.size,
                    message = "Received and stored ${receivedItems.size} items from $peerName via Bluetooth."
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
