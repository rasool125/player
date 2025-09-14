package com.example.blutoothapp.Data.Repositiry

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.blutoothapp.Data.FoundDeviceReceiver
import com.example.blutoothapp.Domain.Methods.BluetoothDeviceDomain
import com.example.blutoothapp.Domain.Repository.BluetoothRepository
import com.example.blutoothapp.Domain.Utils.ConnectionState
import com.example.blutoothapp.Domain.toBluetoothDeviceDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothRepositoryImpl(
    private val context: Context
) : BluetoothRepository {

    companion object {
        private const val TAG = "BluetoothRepository"
        private const val CONNECTION_TIMEOUT = 10000L // 10 seconds
        private const val MAX_RETRY_ATTEMPTS = 3

        // Multiple UUIDs to try for different device types
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Serial Port Profile
        private val HEADSET_UUID = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb") // Headset Profile
        private val A2DP_UUID = UUID.fromString("0000110D-0000-1000-8000-00805F9B34FB") // Advanced Audio
    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    // Existing StateFlows
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState>
        get() = _connectionState.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    override val connectedDevice: StateFlow<BluetoothDeviceDomain?>
        get() = _connectedDevice.asStateFlow()

    // Connection variables
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectJob: Job? = null
    private var isConnectionCancelled = false

    // Coroutine scope for connection operations
    private val connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        Log.d(TAG, "Device found: ${device.name} - ${device.address}")
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) {
                devices
            } else {
                devices.toMutableList().apply { add(newDevice) }
            }
        }
    }

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        Log.d(TAG, "Starting device discovery")

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Log.e(TAG, "BLUETOOTH_SCAN permission not granted")
            return
        }

        try {
            context.registerReceiver(
                foundDeviceReceiver,
                IntentFilter(BluetoothDevice.ACTION_FOUND)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receiver", e)
        }

        updatePairedDevices()
        _scannedDevices.value = emptyList() // Clear previous scan results
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun connectToDevice(device: BluetoothDeviceDomain) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            _connectionState.value = ConnectionState.Error("BLUETOOTH_CONNECT permission not granted")
            return
        }

        if (bluetoothAdapter == null) {
            _connectionState.value = ConnectionState.Error("Bluetooth adapter is null")
            return
        }

        if (_connectionState.value is ConnectionState.Connecting) {
            Log.d(TAG, "Already connecting, ignoring request")
            return
        }

        if (_connectionState.value is ConnectionState.Connected) {
            disconnectFromDevice() // Disconnect current device first
        }

        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        if (bluetoothDevice == null) {
            _connectionState.value = ConnectionState.Error("Could not find device with address: ${device.address}")
            return
        }

        Log.d(TAG, "Attempting to connect to: ${device.name} (${device.address})")

        // Cancel any ongoing discovery to improve connection performance
        bluetoothAdapter?.cancelDiscovery()

        // Cancel previous connection job
        connectJob?.cancel()

        _connectionState.value = ConnectionState.Connecting
        isConnectionCancelled = false

        // Start connection in coroutine with timeout and retry logic
        connectJob = connectionScope.launch {
            try {
                connectWithRetryLogic(bluetoothDevice, device)
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed with exception", e)
                if (!isConnectionCancelled) {
                    _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun connectWithRetryLogic(
        bluetoothDevice: BluetoothDevice,
        deviceDomain: BluetoothDeviceDomain
    ) {
        var lastException: Exception? = null

        // Get UUIDs to try (device-specific UUIDs first, then fallback UUIDs)
        val uuidsToTry = mutableListOf<UUID>()

        // Add device-specific UUIDs first
        bluetoothDevice.uuids?.forEach { parcelUuid ->
            uuidsToTry.add(parcelUuid.uuid)
        }

        // Add common fallback UUIDs
        uuidsToTry.addAll(listOf(SPP_UUID, HEADSET_UUID, A2DP_UUID))

        // Remove duplicates
        val uniqueUUIDs = uuidsToTry.distinct()

        Log.d(TAG, "Trying ${uniqueUUIDs.size} UUIDs for connection")

        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            if (isConnectionCancelled) return

            Log.d(TAG, "Connection attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS")

            for ((index, uuid) in uniqueUUIDs.withIndex()) {
                if (isConnectionCancelled) return

                try {
                    Log.d(TAG, "Trying UUID ${index + 1}/${uniqueUUIDs.size}: $uuid")

                    val socket = createSocketWithFallback(bluetoothDevice, uuid)

                    // Try to connect with timeout
                    val connected = withTimeoutOrNull(CONNECTION_TIMEOUT) {
                        withContext(Dispatchers.IO) {
                            socket.connect()
                            socket.isConnected
                        }
                    }

                    if (connected == true && socket.isConnected) {
                        // Connection successful
                        bluetoothSocket = socket
                        _connectedDevice.value = deviceDomain
                        _connectionState.value = ConnectionState.Connected
                        Log.i(TAG, "Successfully connected to ${deviceDomain.name} with UUID: $uuid")
                        return
                    } else {
                        socket.close()
                        Log.w(TAG, "Connection timeout or failed for UUID: $uuid")
                    }

                } catch (e: Exception) {
                    Log.w(TAG, "Connection failed with UUID $uuid: ${e.message}")
                    lastException = e
                }
            }

            // Wait before retry (except for last attempt)
            if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                Log.d(TAG, "Waiting before retry...")
                delay(2000) // Wait 2 seconds between attempts
            }
        }

        // All attempts failed
        if (!isConnectionCancelled) {
            val errorMessage = "Failed to connect after $MAX_RETRY_ATTEMPTS attempts with ${uniqueUUIDs.size} UUIDs"
            _connectionState.value = ConnectionState.Error(errorMessage, lastException)
            Log.e(TAG, errorMessage, lastException)
        }
    }

    private fun createSocketWithFallback(device: BluetoothDevice, uuid: UUID): BluetoothSocket {
        return try {
            // Try secure connection first
            Log.d(TAG, "Attempting secure connection")
            device.createRfcommSocketToServiceRecord(uuid)
        } catch (e: Exception) {
            Log.w(TAG, "Secure connection failed, trying insecure: ${e.message}")
            try {
                // Try insecure connection
                device.createInsecureRfcommSocketToServiceRecord(uuid)
            } catch (e2: Exception) {
                Log.w(TAG, "Insecure connection failed, trying reflection: ${e2.message}")
                // Last resort: reflection method
                createSocketUsingReflection(device)
            }
        }
    }

    private fun createSocketUsingReflection(device: BluetoothDevice): BluetoothSocket {
        return try {
            Log.d(TAG, "Using reflection method to create socket")
            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            method.invoke(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            Log.e(TAG, "Reflection method failed", e)
            throw IOException("Failed to create socket using reflection: ${e.message}")
        }
    }

    override fun disconnectFromDevice() {
        Log.d(TAG, "Disconnecting from device")

        isConnectionCancelled = true
        _connectionState.value = ConnectionState.Disconnecting

        // Cancel connection job
        connectJob?.cancel()
        connectJob = null

        // Close socket
        bluetoothSocket?.let { socket ->
            try {
                socket.close()
                Log.d(TAG, "Socket closed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing socket", e)
            }
        }

        bluetoothSocket = null
        _connectedDevice.value = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun release() {
        Log.d(TAG, "Releasing Bluetooth resources")

        disconnectFromDevice()
        connectionScope.cancel()

        try {
            context.unregisterReceiver(foundDeviceReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    // Enhanced method to check if device is ready for connection
    private suspend fun prepareDeviceForConnection(device: BluetoothDevice): Boolean {
        return try {
            // Check if device is bonded
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                Log.w(TAG, "Device not bonded: ${device.address}")
                // You could implement pairing logic here if needed
                return false
            }

            // Cancel discovery to improve connection performance
            bluetoothAdapter?.cancelDiscovery()

            // Small delay to ensure discovery is cancelled
            delay(500)

            Log.d(TAG, "Device prepared for connection: ${device.address}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare device for connection", e)
            false
        }
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted")
            return
        }

        try {
            bluetoothAdapter
                ?.bondedDevices
                ?.map { it.toBluetoothDeviceDomain() }
                ?.also { devices ->
                    _pairedDevices.update { devices }
                    Log.d(TAG, "Updated paired devices: ${devices.size} devices")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating paired devices", e)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to get device info for debugging
    private fun logDeviceInfo(device: BluetoothDevice) {
        try {
            Log.d(TAG, "Device Info:")
            Log.d(TAG, "  Name: ${device.name}")
            Log.d(TAG, "  Address: ${device.address}")
            Log.d(TAG, "  Bond State: ${device.bondState}")
            Log.d(TAG, "  Type: ${device.type}")
            Log.d(TAG, "  UUIDs: ${device.uuids?.joinToString { it.uuid.toString() } ?: "None"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging device info", e)
        }
    }
}