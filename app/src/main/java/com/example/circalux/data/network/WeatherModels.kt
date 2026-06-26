package com.example.circalux.data.network

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("current_weather") val currentWeatherLegacy: CurrentWeather?,
    val current: CurrentData?,
    val daily: DailyData?
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val time: String
)

data class CurrentData(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("uv_index") val uvIndex: Double
)

data class DailyData(
    val time: List<String>,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double>,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>,
    val weathercode: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>
)
