package com.example.blutoothapp.Domain.useCase

import android.util.Log
import com.example.blutoothapp.Domain.Repository.BluetoothRepository

class StartDeviceDiscoveryUseCase(
    private val repository: BluetoothRepository
) {
    operator fun invoke() {
        Log.d("tag___" , "Starting device discovery...")
        repository.startDiscovery()
    }
}