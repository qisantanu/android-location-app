package com.locationtracker.api

import com.locationtracker.data.LocationData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApiService {
    @POST("locations")
    suspend fun sendLocation(@Body location: LocationData): Response<Unit>

    @POST("locations/bulk")
    suspend fun sendLocations(@Body locations: List<LocationData>): Response<Unit>
}
