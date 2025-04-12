package com.example.projekt.viewModels
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.api.SetApi
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.responses.DayForecastResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherViewModel(
    private val repository: WeatherRepository, private val locationProvider: LocationProvider
) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _cityWeatherMap = MutableStateFlow<Map<String, WeatherResponse>>(emptyMap())
    val cityWeatherMap: StateFlow<Map<String, WeatherResponse>> = _cityWeatherMap.asStateFlow()

    private val _forecastData = MutableStateFlow<DayForecastResponse?>(null)
    val forecastData: StateFlow<DayForecastResponse?> = _forecastData.asStateFlow()

    private val _city = MutableLiveData<String>("Pardubice") // výchozí město
    val city: LiveData<String> = _city

    private val _locationKey = MutableLiveData<String>()
    val locationKey: LiveData<String> = _locationKey

    fun fetchLocationKey(city: String, apiKey: String) {
        viewModelScope.launch {
            val objectJson = repository.getRawLocationResponse(city, apiKey)

            try {
                val locationKey = objectJson?.getString("Key")
                _locationKey.value = locationKey!!
                locationKey.let { fetchWeather(it, apiKey) }

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při zpracování JSON: ${e.message}")
            }
        }
    }


    fun fetchWeather(locationKey: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val weatherResponse = repository.getWeather(locationKey, apiKey)
                _weatherData.value = weatherResponse

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání počasí: ${e.message}")
            }
        }
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
        fetchLocationKey(newCity, SetApi.getApi)
    }

    fun updateLocation() {
        viewModelScope.launch {
            val currentCity = locationProvider.getCityName()
            if (currentCity != null) {
                updateCity(currentCity)
            }
        }
    }

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

    fun updateWeatherForCities(cities: List<String>) {
        cities.forEach { cityName ->
            updateCityWeather(cityName)
        }
    }

    fun fetchForecast(locationKey: String) {
        viewModelScope.launch {

            try {
                val forecastResponse = repository.getFiveDayForecast(locationKey, SetApi.getApi)
                val formattedForecast =
                    forecastResponse.copy(dailyForecasts = forecastResponse.dailyForecasts.map { forecast ->
                        forecast.copy(date = formatDate(forecast.date))
                    })

                _forecastData.value = formattedForecast
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Chyba při načítání předpovědi: ${e.message}")
            }
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            if (dateString.contains("(") && dateString.contains(")") || dateString.contains("Dnes") || dateString.contains(
                    "Včera"
                )
            ) {
                return dateString
            }

            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val targetFormat = SimpleDateFormat("d. MMMM (EEEE)", Locale("cs", "CZ"))
            val date: Date? = originalFormat.parse(dateString)

            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it

                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)

                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DAY_OF_YEAR, -1)
                yesterday.set(Calendar.HOUR_OF_DAY, 0)
                yesterday.set(Calendar.MINUTE, 0)
                yesterday.set(Calendar.SECOND, 0)
                yesterday.set(Calendar.MILLISECOND, 0)

                if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(
                        Calendar.DAY_OF_YEAR
                    )
                ) {
                    "Dnes"
                } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(
                        Calendar.DAY_OF_YEAR
                    ) == yesterday.get(Calendar.DAY_OF_YEAR)
                ) {
                    "Včera"
                } else {
                    targetFormat.format(it)
                }
            } ?: dateString
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Chyba při konverzi data: ${e.message}")
            dateString
        }
    }

}





