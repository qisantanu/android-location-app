package com.locationtracker.api

import com.locationtracker.data.DistanceStats
import retrofit2.Response
import retrofit2.http.GET

interface DistanceStatsApiService {
    @GET("distance-stats")
    suspend fun getDistanceStats(): Response<DistanceStats>
}
