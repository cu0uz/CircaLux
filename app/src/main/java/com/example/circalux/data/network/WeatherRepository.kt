package com.example.circalux.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse? {
        return try {
            api.getForecast(lat, lon)
        } catch (e: Exception) {
            null
        }
    }
}
