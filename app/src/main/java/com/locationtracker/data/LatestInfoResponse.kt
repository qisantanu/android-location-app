package com.locationtracker.data

import com.google.gson.annotations.SerializedName

data class RouteDetail(
    @SerializedName("distance") val distance: Int
)

data class LatestInfoResponse(
    @SerializedName("distance") val distance: Int,
    @SerializedName("location_name") val locationName: String,
    @SerializedName("remaining_distance") val remainingDistance: Int = 0,
    @SerializedName("route_info") val routeInfo: Map<String, RouteDetail>? = null
)
