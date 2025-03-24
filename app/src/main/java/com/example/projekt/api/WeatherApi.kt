package com.example.projekt.api

import com.example.projekt.items.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current")
    suspend fun getCurrentWeather(
        @Query("access_key") apiKey: String,
        @Query("query") city: String
    ): WeatherResponse
}