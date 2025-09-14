package com.example.blutoothapp.Presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.blutoothapp.Domain.Methods.BluetoothDevice
import com.example.blutoothapp.Domain.Utils.BluetoothEvents


@Composable
fun DevicesScreen(
    navController: NavController,
    viewModel: BluetoothViewModel){
// BluetoothScreen.kt

    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val scannedDevices by viewModel.scanDevices.collectAsState()

    Log.d("BluetoothViewModel", "DevicesScreen:  ${scannedDevices.size} ")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PairedDevicesSection(pairedDevices, Modifier.weight(1f))
        ScannedDevicesSection(scannedDevices , Modifier.weight(1f), viewModel)
    }
}

@Composable
fun PairedDevicesSection(pairedDevices: List<BluetoothDevice> , modifier: Modifier) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Paired Devices")
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(pairedDevices) { device ->
                BluetoothDeviceItem(device ){

                }
            }
        }
    }
}

@Composable
fun ScannedDevicesSection(scannedDevices: List<BluetoothDevice>,  modifier: Modifier, viewModel: BluetoothViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scanned Devices")
        Spacer(modifier = Modifier.height(16.dp))
        Toast.makeText(context, scannedDevices.size.toString(), Toast.LENGTH_SHORT).show()
        LazyColumn {
            items(scannedDevices) { device ->
                BluetoothDeviceItem(device){
                    viewModel.onEvent(BluetoothEvents.connectToDevice(device))
                }
            }
        }
    }
}

@Composable
fun BluetoothDeviceItem(device: BluetoothDevice, onClick : ()-> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text( device.name ?: "Unkhown device")
        Button(onClick = { /* Handle device connection */ }) {
            onClick.invoke()
        }
    }
}
