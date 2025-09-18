package com.example.blutoothapp.Domain.Methods

// LocationData.kt
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

// Route.kt
data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

data class RouteData(
    val points: List<RoutePoint>,
    val distance: String,
    val duration: String
)
