package com.locationtracker.api

import retrofit2.Response
import retrofit2.http.GET

interface InfoApiService {
    @GET("info") // Assuming the endpoint is "/info"
    suspend fun getInfo(): Response<Map<String, String>>
}
