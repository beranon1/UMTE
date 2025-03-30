package com.example.projekt.viewModels
//64NX3BTzlZHPGtNYktXmpVOUd6wjOm9I
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _city = MutableLiveData(savedStateHandle.get<String>("city") ?: "Pardubice")
    val city: LiveData<String> = _city

   init {
       Log.d("_city.value", "${_city.value}")
    }

    fun fetchLocationKey(city: String, apiKey: String) {
        viewModelScope.launch {
                val objectJson = repository.getRawLocationResponse(city, apiKey)

            try {
                    val locationKey = objectJson?.getString("Key")
                    locationKey?.let { fetchWeather(it, apiKey) }

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při zpracování JSON: ${e.message}")
            }
        }
    }


    fun fetchWeather(locationKey: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val weatherResponse = repository.getWeather(locationKey, apiKey)
                Log.d("WeatherViewModel", "Získaný locationKey: $locationKey")
                _weatherData.value = weatherResponse
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí: ${e.message}")
            }
        }
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
        savedStateHandle["city"] = newCity // Uloží město do SavedStateHandle
        fetchLocationKey(newCity, "n9PG3GhsqjHzpf9obRKuZoAxtvHM0iev")
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





