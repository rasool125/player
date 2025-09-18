package com.example.blutoothapp.Presentation// MapScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    
    // Request location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        locationPermissions.launchMultiplePermissionRequest()
    }
    
    if (locationPermissions.allPermissionsGranted) {
        MapContent(viewModel = viewModel)
    } else {
        PermissionDeniedContent(
            onRequestPermissions = {
                locationPermissions.launchMultiplePermissionRequest()
            }
        )
    }
}

@Composable
private fun MapContent(viewModel: MapViewModel) {
    val currentLocation = viewModel.currentLocation
    val destinationLocation = viewModel.destinationLocation
    
    // Default camera position (will be updated when location is available)
    val defaultPosition = LatLng(37.7749, -122.4194) // San Francisco
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultPosition,
            15f
        )
    }
    
    // Update camera when current location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    15f
                ),
                durationMs = 1000
            )
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = true
            )
        ) {
            // Current location marker
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(
                        position = LatLng(location.latitude, location.longitude)
                    ),
                    title = "Your Location"
                )
            }
            
            // Destination marker
            destinationLocation?.let { destination ->
                Marker(
                    state = MarkerState(
                        position = LatLng(destination.latitude, destination.longitude)
                    ),
                    title = destination.address.ifEmpty { "Destination" }
                )
            }
            
            // Route polyline
            if (viewModel.routePoints.isNotEmpty()) {
                Polyline(
                    points = viewModel.routePoints,
                    color = androidx.compose.ui.graphics.Color.Blue,
                    width = 8f
                )
            }
        }
        
        // Search Box
        SearchBox(
            query = viewModel.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSearch = viewModel::searchLocation,
            onClear = viewModel::clearSearch,
            isLoading = viewModel.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Navigation controls
        if (destinationLocation != null) {
            NavigationControls(
                onStartNavigation = {
                    // Start navigation logic
                },
                onClearRoute = viewModel::clearSearch,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search location...") },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true
        )
    }
}

@Composable
private fun NavigationControls(
    onStartNavigation: () -> Unit,
    onClearRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartNavigation,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Navigation")
            }
            
            OutlinedButton(
                onClick = onClearRoute,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear Route")
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Location permissions are required for this app to work properly.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRequestPermissions) {
            Text("Grant Permissions")
        }
    }
}
