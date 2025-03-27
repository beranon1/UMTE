package com.example.projekt.viewModels
//JDsZdUGrYMNEUrByjITuEebDtGdTkG62
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.items.WeatherResponse
import com.example.projekt.screens.getCurrentLocation
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _city = MutableLiveData<String>()
    val city: LiveData<String> = _city

    private val _locationKey = MutableStateFlow<String?>(null)

   init {
        fetchLocationKey("Pardubice", "RgnmT55MmLDFGkAujGydfpOC9oTza5OA")
    }

    fun fetchLocationKey(city: String, apiKey: String) {
        viewModelScope.launch {

                val objectJson = repository.getRawLocationResponse(city, apiKey)

            try {
                    val locationKey = objectJson?.getString("Key")
                    _locationKey.value = locationKey
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
        fetchLocationKey(newCity, "RgnmT55MmLDFGkAujGydfpOC9oTza5OA")
    }
}





