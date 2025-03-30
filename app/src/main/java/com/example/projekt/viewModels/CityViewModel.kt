package com.example.projekt.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.responses.CityResponse
import kotlinx.coroutines.launch

class CityViewModel (
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _cityData = MutableLiveData<List<CityResponse>>()
    val cityData: LiveData<List<CityResponse>> = _cityData

    fun fetchCityKey() {
        viewModelScope.launch {

            try {
                val response = repository.getCities("CZ", "n9PG3GhsqjHzpf9obRKuZoAxtvHM0iev")
                Log.d("WeatherViewModel", "Získaná města: $response")
                _cityData.value = response
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí: ${e.message}")
            }
        }
    }
}