package com.example.projekt.viewModels
//64NX3BTzlZHPGtNYktXmpVOUd6wjOm9I
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.api.SetApi
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _cityWeatherMap = MutableStateFlow<Map<String, WeatherResponse>>(emptyMap())
    val cityWeatherMap: StateFlow<Map<String, WeatherResponse>> = _cityWeatherMap.asStateFlow()

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


    fun fetchWeather(locationKey: String, apiKey: String){
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

    // Funkce pro aktualizaci počasí pro jedno město
    fun updateCityWeather(cityName: String) {
        viewModelScope.launch {
            val objectJson = repository.getRawLocationResponse(cityName, SetApi.getApi)
            try {
                val locationKey = objectJson?.getString("Key")
                locationKey?.let {
                    val weatherResponse = repository.getWeather(it, SetApi.getApi)
                    _cityWeatherMap.update { currentData ->
                        currentData + (cityName to weatherResponse)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí pro město $cityName: ${e.message}")
            }
        }
    }

    // Funkce pro načtení počasí pro všechna města
    fun updateWeatherForCities(cities: List<String>) {
        cities.forEach { cityName ->
            updateCityWeather(cityName)
        }
    }
}





