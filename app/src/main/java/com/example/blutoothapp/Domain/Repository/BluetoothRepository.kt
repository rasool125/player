package com.example.blutoothapp.Domain.Repository

import com.example.blutoothapp.Domain.Methods.BluetoothDevice
import com.example.blutoothapp.Domain.Methods.BluetoothDeviceDomain
import com.example.blutoothapp.Domain.Utils.ConnectionState
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val connectionState: StateFlow<ConnectionState>
    val connectedDevice: StateFlow<BluetoothDeviceDomain?>

    fun startDiscovery()
    fun stopDiscovery()
    fun connectToDevice(device: BluetoothDeviceDomain)
    fun disconnectFromDevice()
    fun release()
}
