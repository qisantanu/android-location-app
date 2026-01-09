package com.locationtracker.api

import com.locationtracker.data.LocationsPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApiService {
    @POST("locations")
    suspend fun sendLocations(@Body payload: LocationsPayload): Response<Unit>
}
