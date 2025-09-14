package com.example.blutoothapp.Domain.Utils

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnecting : ConnectionState()
    data class Error(val message: String, val exception: Throwable? = null) : ConnectionState()
    object Disconnected : ConnectionState()
}
