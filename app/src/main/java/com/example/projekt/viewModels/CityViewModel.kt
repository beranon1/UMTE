package com.example.projekt.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.api.SetApi
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.responses.CityResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore by preferencesDataStore(name = "favourite_cities")
val FAVOURITE_CITIES_KEY = stringSetPreferencesKey("favourite_cities")


class CityViewModel (
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _cityData = MutableLiveData<List<CityResponse>>()
    val cityData: LiveData<List<CityResponse>> = _cityData

    fun fetchCityKey() {
        viewModelScope.launch {

            try {
                val response = repository.getCities("CZ", SetApi.getApi)
                Log.d("WeatherViewModel", "Získaná města: $response")
                _cityData.value = response
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí: ${e.message}")
            }
        }
    }

    // Uložení města do DataStore
    fun saveCityToDataStore(cityName: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val currentCities = preferences[FAVOURITE_CITIES_KEY] ?: emptySet()
                preferences[FAVOURITE_CITIES_KEY] = currentCities + cityName
            }
        }
    }

    // Načtení oblíbených měst z DataStore
    fun getFavouriteCitiesFromDataStore(onResult: (Set<String>) -> Unit) {
        viewModelScope.launch {
            val cities = context.dataStore.data
                .map { preferences -> preferences[FAVOURITE_CITIES_KEY] ?: emptySet() }
                .first()
            onResult(cities)
        }
    }

    // Funkce pro odstranění města z DataStore
    fun removeCityFromDataStore(cityName: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val currentCities = preferences[FAVOURITE_CITIES_KEY] ?: emptySet()
                preferences[FAVOURITE_CITIES_KEY] = currentCities - cityName  // Odstraní město
            }
        }
    }

}

