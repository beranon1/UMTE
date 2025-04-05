package com.example.projekt.responses

import com.google.gson.annotations.SerializedName

data class DayForecastResponse(
    @SerializedName("DailyForecasts") val dailyForecasts: List<DailyForecast>
)

data class DailyForecast(
    @SerializedName("Date") val date: String,
    @SerializedName("EpochDate") val epochDate: Long,
    @SerializedName("Temperature") val temperature: Temperature,
    @SerializedName("Day") val day: WeatherDetail,
    @SerializedName("Night") val night: WeatherDetail
)

data class Temperature(
    @SerializedName("Minimum") val minimum: TempDetail,
    @SerializedName("Maximum") val maximum: TempDetail
)

data class TempDetail(
    @SerializedName("Value") val value: Double,
    @SerializedName("Unit") val unit: String,
    @SerializedName("UnitType") val unitType: Int
)

data class WeatherDetail(
    @SerializedName("Icon") val icon: Int,
    @SerializedName("IconPhrase") val iconPhrase: String,
    @SerializedName("HasPrecipitation") val hasPrecipitation: Boolean
)