package com.example.projekt.repository

import android.util.Log
import com.example.projekt.api.WeatherApi
import com.example.projekt.items.WeatherResponse

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getWeather(city: String, apiKey: String): WeatherResponse {
        Log.d("WeatherRepository", "Volání API pro: $city") // ✅ Přidá logování
        return api.getCurrentWeather(apiKey, city).also {
            Log.d("WeatherRepository", "Odpověď API: $it") // ✅ Přidá logování
        }
    }
}