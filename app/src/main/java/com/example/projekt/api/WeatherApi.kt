package com.example.projekt.api

import com.example.projekt.items.WeatherResponse
import okhttp3.ResponseBody
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {

    @GET("currentconditions/v1/{locationKey}")
    suspend fun getWeather(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String,
        @Query("details") details: Boolean = true
    ): List<WeatherResponse>

    @GET("locations/v1/cities/search")
    @Headers(
        "Accept: application/json",
        "User-Agent: MyWeatherApp/1.0"
    )
    suspend fun getLocation(
        @Query("q") city: String,
        @Query("apikey") apiKey: String,
    ): Response<ResponseBody>
}