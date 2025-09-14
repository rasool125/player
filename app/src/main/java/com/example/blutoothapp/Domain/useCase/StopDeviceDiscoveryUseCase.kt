package com.example.blutoothapp.Domain.useCase

import android.util.Log
import com.example.blutoothapp.Domain.Repository.BluetoothRepository

class StopDeviceDiscoveryUseCase(
    private val repository: BluetoothRepository
) {
    operator fun invoke() {
        Log.d("call___", "invoke: invoke")
        repository.stopDiscovery()
    }
}