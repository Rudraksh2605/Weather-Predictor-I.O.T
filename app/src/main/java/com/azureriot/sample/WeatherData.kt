package com.azureriot.sample

import com.google.gson.annotations.SerializedName

data class WeatherData(

    @SerializedName("temperature") val temperature: Double,
    @SerializedName("humidity") val humidity: Double
)
