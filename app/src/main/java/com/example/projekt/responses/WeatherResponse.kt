package com.example.projekt.responses

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val location: LocationResponse,
    @SerializedName("WeatherIcon") val weatherIcon: Int,
    @SerializedName("Temperature") val temperature: TemperatureResponse,
    @SerializedName("RealFeelTemperature") val realFeelTemperature: TemperatureResponse,
    @SerializedName("UVIndex") val uvIndex: Int,
    @SerializedName("TemperatureSummary") val temperatureSummary: TemperatureSummaryResponse
)

data class TemperatureResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class TemperatureSummaryResponse(
    @SerializedName("Past24HourRange") val past24HourRange: Past24HourRangeResponse
)

data class Past24HourRangeResponse(
    @SerializedName("Minimum") val minTemperature: TemperatureResponse,
    @SerializedName("Maximum") val maxTemperature: TemperatureResponse
)










