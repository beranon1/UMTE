package com.example.projekt.api

import com.example.projekt.responses.CityResponse
import com.example.projekt.responses.DayForecastResponse
import com.example.projekt.responses.HourlyForecastResponse
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.responses.WeatherDetailResponse
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
        "Accept: application/json", "User-Agent: MyWeatherApp/1.0"
    )
    suspend fun getLocation(
        @Query("q") city: String,
        @Query("apikey") apiKey: String,
    ): Response<ResponseBody>

    @GET("currentconditions/v1/{locationKey}")
    suspend fun getWeatherDetail(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String,
        @Query("details") details: Boolean = true
    ): List<WeatherDetailResponse>

    @GET("locations/v1/cities/{countryCode}")
    suspend fun getCities(
        @Path("countryCode") countryCode: String,
        @Query("apikey") apiKey: String,
        @Query("language") language: String = "cs"
    ): List<CityResponse>

    @GET("forecasts/v1/daily/5day/{locationKey}")
    suspend fun getFiveDayForecast(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String,
        @Query("language") language: String = "cs",
        @Query("details") details: Boolean = false,
        @Query("metric") metric: Boolean = true
    ): DayForecastResponse

    @GET("forecasts/v1/hourly/12hour/{locationKey}")
    suspend fun getHourlyForecast(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String,
        @Query("language") language: String = "cs",
        @Query("metric") metric: Boolean = true
    ): List<HourlyForecastResponse>
}