package com.example.blutoothapp.Domain.useCase

import com.example.blutoothapp.Domain.Methods.BluetoothDevice
import com.example.blutoothapp.Domain.Repository.BluetoothRepository
import kotlinx.coroutines.flow.StateFlow

class connectToDeviceUseCase(
    private val repository: BluetoothRepository
) {
    operator fun invoke(device: BluetoothDevice) {
        repository.connectToDevice(device)
    }
}