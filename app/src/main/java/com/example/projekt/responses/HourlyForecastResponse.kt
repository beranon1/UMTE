package com.example.projekt.responses

import com.google.gson.annotations.SerializedName


data class HourlyForecastResponse(
    @SerializedName("DateTime") val dateTime: String,
    @SerializedName("EpochDateTime") val epochDateTime: Long,
    @SerializedName("WeatherIcon") val weatherIcon: Int,
    @SerializedName("IconPhrase") val iconPhrase: String,
    @SerializedName("HasPrecipitation") val hasPrecipitation: Boolean,
    @SerializedName("IsDaylight") val isDaylight: Boolean,
    @SerializedName("Temperature") val temperature: TemperatureHour
)

data class TemperatureHour(
     @SerializedName("Value") val value: Double,
     @SerializedName("Unit") val unit: String,
     @SerializedName("UnitType") val unitType: Int
)

