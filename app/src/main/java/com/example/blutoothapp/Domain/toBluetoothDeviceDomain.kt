package com.example.blutoothapp.Domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.blutoothapp.Domain.Methods.BluetoothDeviceDomain


@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}