package com.example.blutoothapp.Domain.useCase

data class BluetoothUseCases(
    val getPairedDevicesUseCase: GetPairedDevicesUseCase,
    val getScannedDevicesUseCase: GetScannedDevicesUseCase,
    val releaseBluetoothResourcesUseCase: ReleaseBluetoothResourcesUseCase,
    val startDeviceDiscoveryUseCase: StartDeviceDiscoveryUseCase,
    val StopDeviceDiscoveryUseCase: StopDeviceDiscoveryUseCase,
    val connectToDeviceUseCase: connectToDeviceUseCase
)
