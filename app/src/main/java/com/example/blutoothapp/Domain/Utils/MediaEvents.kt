package com.example.blutoothapp.Domain.Utils

import com.example.blutoothapp.Domain.Methods.BluetoothDevice

sealed class BluetoothEvents {
    data object GetPairedDevices :BluetoothEvents()
    data object GetScanDevices :BluetoothEvents()
    data object ReleaseBluetoothResources :BluetoothEvents()
    data object StartDeviceDiscovery :BluetoothEvents()
    data object StopDeviceDiscovery :BluetoothEvents()
    data class connectToDevice(val device: BluetoothDevice) :BluetoothEvents()
}