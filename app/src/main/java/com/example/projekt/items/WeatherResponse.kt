package com.example.projekt.items

data class WeatherResponse(
    val location: Location,
    val current: CurrentWeather
)

data class Location(
    val name: String,
    val country: String
)

data class CurrentWeather(
    val temperature: Int,
    val weather_descriptions: List<String>,
    val wind_speed: Int,
    val humidity: Int,
    val visibility: Int,
    val weather_icons: List<String>
)
