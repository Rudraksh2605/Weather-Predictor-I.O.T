package com.azureriot.sample

import com.google.gson.annotations.SerializedName

data class WeatherData(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("humidity") val humidity: Double
)
