package com.example.projekt.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.api.SetApi
import com.example.projekt.location.LocationProvider
import com.example.projekt.responses.WeatherDetailResponse
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.responses.HourlyForecastResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherDetailViewModel (
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _weatherDetailData = MutableLiveData<WeatherDetailResponse?>()
    val weatherDetailData: LiveData<WeatherDetailResponse?> = _weatherDetailData

    private val _hourlyForecastData = MutableLiveData<List<HourlyForecastResponse>>()
    val hourlyForecastData: LiveData<List<HourlyForecastResponse>> = _hourlyForecastData

    private val _city = MutableLiveData(savedStateHandle.get<String>("city") ?: "Pardubice")
    val city: LiveData<String> = _city

    private val _locationKey = MutableLiveData<String>()
    val locationKey: LiveData<String> = _locationKey

    fun fetchLocationKey(city: String, apiKey: String) {
        viewModelScope.launch {
            val objectJson = repository.getRawLocationResponse(city, apiKey)

            try {
                val locationKey = objectJson?.getString("Key")
                _locationKey.value = locationKey!!
                locationKey?.let { fetchWeatherDetail(it, apiKey) }

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při zpracování JSON: ${e.message}")
            }
        }
    }

    fun fetchWeatherDetail(locationKey: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val weatherDetailResponse = repository.getWeatherDetail(locationKey, apiKey)
                //Log.d("WeatherViewModel", "Získaný locationKey: $locationKey")
                _weatherDetailData.value = weatherDetailResponse
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí: ${e.message}")
            }
        }
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
        savedStateHandle["city"] = newCity // Uloží město do SavedStateHandle
        fetchLocationKey(newCity, SetApi.getApi)
    }

    fun updateLocation() {
        viewModelScope.launch {
            val currentCity = locationProvider.getCityName() // ✅ Získání názvu města
            if (currentCity != null) {
                updateCity(currentCity)
            }
        }
    }

    fun fetchHourlyForecast(locationKey: String) {
        viewModelScope.launch {
            try {
                val hourlyForecastResponse = repository.getHourlyForecast(locationKey, SetApi.getApi)
                //Log.d("WeatherViewModel", "Hodinová předpověď: $hourlyForecastResponse")
                _hourlyForecastData.value = hourlyForecastResponse
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání hodinové předpovědi: ${e.message}")
            }
        }
    }

    // 📅 Funkce pro přeformátování data a času
    fun formatDateAndTime(dateTimeString: String): String {
        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val targetFormat = SimpleDateFormat("HH:mm", Locale("cs", "CZ"))
            val date: Date? = originalFormat.parse(dateTimeString)
            date?.let { targetFormat.format(it) } ?: dateTimeString
        } catch (e: Exception) {
            Log.e("WeatherDetailViewModel", "Chyba při konverzi data a času: ${e.message}")
            dateTimeString
        }
    }


}