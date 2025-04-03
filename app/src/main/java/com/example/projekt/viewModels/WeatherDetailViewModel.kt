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
import kotlinx.coroutines.launch

class WeatherDetailViewModel (
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _weatherDetailData = MutableLiveData<WeatherDetailResponse?>()
    val weatherDetailData: LiveData<WeatherDetailResponse?> = _weatherDetailData

    private val _city = MutableLiveData(savedStateHandle.get<String>("city") ?: "Pardubice")

    fun fetchLocationKey(city: String, apiKey: String) {
        viewModelScope.launch {
            val objectJson = repository.getRawLocationResponse(city, apiKey)

            try {
                val locationKey = objectJson?.getString("Key")
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
                Log.d("WeatherViewModel", "Získaný locationKey: $locationKey")
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
            savedStateHandle["city"] = currentCity
            if (currentCity != null) {
                updateCity(currentCity)
            }
        }
    }
}