package com.example.blutoothapp.Domain.useCase

import com.example.blutoothapp.Domain.Repository.BluetoothRepository

class ReleaseBluetoothResourcesUseCase(
    private val repository: BluetoothRepository
) {
    operator fun invoke() {
        repository.release()
    }
}