package com.example.projekt.responses

import com.google.gson.annotations.SerializedName

data class WeatherDetailResponse(
    val location: LocationResponse,
    @SerializedName("WeatherIcon") val weatherIcon: Int,
    @SerializedName("RelativeHumidity") val relativeHumidity: Int, // Vlhkost
    @SerializedName("Wind") val wind: WindResponse, // Vítr rychllost a směr
    @SerializedName("Visibility") val visibility: VisibilityResponse,//Viditelnost
    @SerializedName("CloudCover") val cloudCover: Int, // procento oblačnosti
    @SerializedName("Pressure") val pressure: PressureResponse, // Tlak
    @SerializedName("PrecipitationSummary") val precipitationSummary: PrecipitationSummaryResponse // Množství srážek za 24 hodin
)

data class WindResponse(
    @SerializedName("Direction") val direction: WindDirectionResponse,
    @SerializedName("Speed") val speed: SpeedResponse
)

data class WindDirectionResponse(
    @SerializedName("Localized") val english: String
)

data class SpeedResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class MetricResponse(
    @SerializedName("Value") val value: Double,
    @SerializedName("Unit") val unit: String
)

data class VisibilityResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class PressureResponse(
    @SerializedName("Metric") val metric: MetricResponse
)

data class PrecipitationSummaryResponse(
    @SerializedName("Past24Hours") val past24Hours: PrecipitationResponse
)

data class PrecipitationResponse(
    @SerializedName("Metric") val metric: MetricResponse
)
