package com.example.projekt.items

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("WeatherIcon") val weatherIcon: Int,
    @SerializedName("Temperature") val temperature: TemperatureResponse,
    @SerializedName("Wind") val wind: WindResponse,
    @SerializedName("RelativeHumidity") val humidity: Int,
    @SerializedName("Visibility") val visibility: VisibilityResponse
)

data class TemperatureResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class MetricResponse(
    @SerializedName("Value") val value: Double
)

data class WindResponse(
    @SerializedName("Speed") val speed: SpeedResponse
)

data class SpeedResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class VisibilityResponse(
    @SerializedName("Metric") val metric: MetricResponse
)


