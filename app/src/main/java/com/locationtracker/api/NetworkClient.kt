package com.locationtracker.api

import android.content.Context
import com.locationtracker.data.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    private fun createRetrofit(context: Context): Retrofit {
        val baseUrl = AppPreferences.getBaseUrl(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("X-API-KEY", "suxmahdixumotherfukers")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(context: Context): LocationApiService {
        return createRetrofit(context).create(LocationApiService::class.java)
    }

    fun createSettingsApi(context: Context): SettingsApiService {
        return createRetrofit(context).create(SettingsApiService::class.java)
    }
}
