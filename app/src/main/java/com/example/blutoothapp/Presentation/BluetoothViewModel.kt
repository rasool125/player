package com.example.blutoothapp.Presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blutoothapp.Domain.Methods.BluetoothDevice
import com.example.blutoothapp.Domain.Utils.BluetoothEvents
import com.example.blutoothapp.Domain.useCase.BluetoothUseCases
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(val bluetoothUseCases: BluetoothUseCases) : ViewModel() {

    // StateFlows for each media type
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> get() = _pairedDevices

    private val _scanDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scanDevices: StateFlow<List<BluetoothDevice>> get() = _scanDevices

    init {
        Log.d("BluetoothViewModel", "ViewModel initialized")
        onEvent(BluetoothEvents.StartDeviceDiscovery)

        viewModelScope.launch {

            while (true) {
                delay(5000) // Wait 5 seconds
                onEvent(BluetoothEvents.GetScanDevices)
            }

        }




//        onEvent(BluetoothEvents.GetPairedDevices)
    }

    // Optimized data fetching with caching
    fun onEvent(event: BluetoothEvents) {
        Log.d("BluetoothViewModel", "Event received: $event")

        when (event) {
            is BluetoothEvents.GetPairedDevices -> {
                val devices = bluetoothUseCases.getPairedDevicesUseCase().value
                Log.d("BluetoothViewModel", "Fetched paired devices: ${devices.size}")
                _pairedDevices.value = devices
            }

            is BluetoothEvents.GetScanDevices -> {
                val devices = bluetoothUseCases.getScannedDevicesUseCase().value
                Log.d("BluetoothViewModel", "Fetched scan devices: ${devices.size}")
                _scanDevices.value = devices
            }

            is BluetoothEvents.StartDeviceDiscovery -> {
                Log.d("BluetoothViewModel", "Starting device discovery...")
                bluetoothUseCases.startDeviceDiscoveryUseCase()
            }

            is BluetoothEvents.StopDeviceDiscovery -> {
                Log.d("BluetoothViewModel", "Stopping device discovery...")
                bluetoothUseCases.StopDeviceDiscoveryUseCase()
            }

            is BluetoothEvents.ReleaseBluetoothResources -> {
                Log.d("BluetoothViewModel", "Releasing Bluetooth resources...")
                bluetoothUseCases.releaseBluetoothResourcesUseCase()
            }
            is BluetoothEvents.connectToDevice ->{
                bluetoothUseCases.connectToDeviceUseCase(event.device)
            }

        }
    }
}
