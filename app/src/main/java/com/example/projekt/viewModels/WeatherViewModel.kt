package com.example.projekt.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.items.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    init {
        // Zadej své skutečné hodnoty – API klíč a město
        fetchWeather("Karlovy", "4e53ee647544a1b260e36b8131f61f1d")
    }

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            Log.d("WeatherViewModel", "Volání fetchWeather pro město: $city") // ✅ Přidá logování
            try {
                val response = repository.getWeather(city, apiKey)
                Log.d("WeatherViewModel", "Úspěšně načteno: $response") // ✅ Přidá logování
                _weatherData.value = response
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při získávání dat", e)
            }
        }
    }
}



