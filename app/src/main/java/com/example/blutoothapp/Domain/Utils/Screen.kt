package com.example.blutoothapp.Domain.Utils

sealed class Screen(val route: String) {
    object BluetoothHome: Screen("BluetoothHome")
}