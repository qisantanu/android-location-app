package com.locationtracker.api

import com.locationtracker.data.SettingItem
import com.locationtracker.data.UpdateSettingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface SettingsApiService {
    @GET("settings")
    suspend fun getSettings(): List<SettingItem>

    @PUT("{id}/setting")
    suspend fun updateSetting(
        @Path("id") id: Int,
        @Body request: UpdateSettingRequest
    ): Response<Unit>
}
