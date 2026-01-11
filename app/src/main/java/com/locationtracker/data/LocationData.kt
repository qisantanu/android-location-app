package com.locationtracker.data

data class LocationData(
    val lat: Double,
    val lng: Double,
    val trip_details: String = "---",
    val accuracy: Float,
    val timestamp: Long,
    val id: Long = 0,
    val synced: Boolean = false
)
