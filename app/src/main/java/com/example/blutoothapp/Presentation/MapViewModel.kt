package com.example.blutoothapp.Presentation// MapViewModel.kt


import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blutoothapp.Data.LocationManager
import com.example.blutoothapp.Domain.Methods.LocationData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(
    private val locationManager: LocationManager
) : ViewModel() {
    
    var currentLocation by mutableStateOf<LocationData?>(null)
        private set
    
    var searchQuery by mutableStateOf("")
        private set
    
    var destinationLocation by mutableStateOf<LocationData?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var routePoints by mutableStateOf<List<LatLng>>(emptyList())
        private set
    
    init {
        getCurrentLocation()
    }
    
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        viewModelScope.launch {
            isLoading = true
            currentLocation = locationManager.getLastKnownLocation()
            isLoading = false
            
            locationManager.getCurrentLocation().collect { location ->
                location?.let {
                    currentLocation = it
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    
    fun searchLocation() {
        // In a real app, you'd use Google Places API or Geocoding API
        // For this example, we'll simulate a search
        viewModelScope.launch {
            isLoading = true
            // Simulate API call
            kotlinx.coroutines.delay(1000)
            
            // Mock search result (replace with actual geocoding)
            destinationLocation = LocationData(
                latitude = 37.7749, // San Francisco coordinates as example
                longitude = -122.4194,
                address = searchQuery
            )
            
            // Calculate route
            calculateRoute()
            isLoading = false
        }
    }
    
    private fun calculateRoute() {
        val start = currentLocation
        val end = destinationLocation
        
        if (start != null && end != null) {
            // Simplified route calculation (straight line)
            // In a real app, use Google Directions API
            routePoints = listOf(
                LatLng(start.latitude, start.longitude),
                LatLng(end.latitude, end.longitude)
            )
        }
    }
    
    fun clearSearch() {
        searchQuery = ""
        destinationLocation = null
        routePoints = emptyList()
    }
}
