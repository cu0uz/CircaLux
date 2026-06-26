package com.example.circalux.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "uv_index_max,temperature_2m_max,weathercode,sunrise,sunset",
        @Query("current") current: String = "temperature_2m,weather_code,uv_index",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}
