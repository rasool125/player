package com.example.blutoothapp.Domain.useCase

import android.util.Log
import com.example.blutoothapp.Domain.Methods.BluetoothDevice
import com.example.blutoothapp.Domain.Repository.BluetoothRepository
import kotlinx.coroutines.flow.StateFlow

class GetScannedDevicesUseCase(
    private val repository: BluetoothRepository
) {

    operator fun invoke(): StateFlow<List<BluetoothDevice>> {
        Log.d("BluetoothRepository", "startDiscovery:${repository.scannedDevices.value.size}  ")
        return repository.scannedDevices
    }
}