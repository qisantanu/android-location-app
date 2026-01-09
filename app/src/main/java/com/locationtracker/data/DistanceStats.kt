package com.locationtracker.data

import com.google.gson.annotations.SerializedName

data class DistanceStats(
    @SerializedName("total_distance_today")
    val totalDistanceToday: Double,
    
    @SerializedName("distance_last_60_minutes")
    val distanceLast60Minutes: Double,
    
    @SerializedName("average_speed")
    val averageSpeed: Double = 0.0,
    
    @SerializedName("trip_duration")
    val tripDuration: Long = 0L,
    
    @SerializedName("last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    
    @SerializedName("active_trips")
    val activeTrips: Int = 0,
    
    @SerializedName("locations_count")
    val locationsCount: Int = 0
)
