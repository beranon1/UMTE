package com.example.projekt.repository

import android.util.Log
import com.example.projekt.api.WeatherApi
import com.example.projekt.responses.CityResponse
import com.example.projekt.responses.DayForecastResponse
import com.example.projekt.responses.HourlyForecastResponse
import com.example.projekt.responses.WeatherDetailResponse
import com.example.projekt.responses.WeatherResponse
import org.json.JSONArray
import org.json.JSONObject

class WeatherRepository(private val apiService: WeatherApi) {

    suspend fun getWeather(locationKey: String, apiKey: String): WeatherResponse {
        val response = apiService.getWeather(locationKey, apiKey)
        //Log.d("WeatherRepository", "Výpis response:  $response")

        // Pokud API vrací pole, vezmeme první objekt
        return if (response.isNotEmpty()) {
            response[0]  // Vrátíme první objekt v poli
        } else {
            throw Exception("Prázdná odpověď z API")
        }
    }


    suspend fun getRawLocationResponse(city: String, apiKey: String): JSONObject? {
        val response = apiService.getLocation(city, apiKey)
        val rawJson = response.body()?.string() // Převod ResponseBody na String
        //Log.d("WeatherRepository", "Muj výpis rawJSON: $rawJson")
        return try {
            val jsonArray = JSONArray(rawJson) // Převod na JSON pole
                jsonArray.getJSONObject(0) // Vrácení prvního objektu v poli
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Chyba při parsování JSON: ${e.message}")
            null
        }
    }

    suspend fun getWeatherDetail(locationKey: String, apiKey: String): WeatherDetailResponse {
        val response = apiService.getWeatherDetail(locationKey, apiKey)
        return if (response.isNotEmpty()) {
            response[0]  // Vrátíme první objekt v poli
        } else {
            throw Exception("Prázdná odpověď z API Detail")
        }
    }

    suspend fun getCities(countryCode: String, apiKey: String): List<CityResponse> {
        val response = apiService.getCities(countryCode, apiKey)
        //Log.d("WeatherRepository", "Výpis response:  $response")
        return if (response.isNotEmpty()) {
            response  // Vrátíme první objekt v poli
        } else {
            throw Exception("Prázdná odpověď z API Detail")
        }
    }

    suspend fun getFiveDayForecast(locationKey: String, apiKey: String): DayForecastResponse {
        return try {
            val response = apiService.getFiveDayForecast(locationKey, apiKey)
            //Log.d("WeatherRepository", "Předpověď získána: $response")
            response
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Chyba při načítání předpovědi: ${e.message}")
            throw e
        }
    }

    suspend fun getHourlyForecast(locationKey: String, apiKey: String): List<HourlyForecastResponse> {
        return try {
            val response = apiService.getHourlyForecast(locationKey, apiKey)
            //Log.d("WeatherRepository", "Hodinová předpověď získána: $response")
            response // Předpokládáme, že apiService vrátí seznam objektů HourlyForecastResponse
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Chyba při načítání hodinové předpovědi: ${e.message}")
            throw e
        }
    }

}