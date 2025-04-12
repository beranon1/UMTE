package com.example.projekt.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.projekt.viewModels.CityViewModel
import com.example.projekt.viewModels.WeatherViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


data class City(val name: String, val temperature: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteCityScreen(
    cityViewModel: CityViewModel = koinViewModel(),
    weatherViewModel: WeatherViewModel = koinViewModel()
) {

    var selectedCity by remember { mutableStateOf<String?>(null) }
    var favouriteCities by remember { mutableStateOf<List<City>>(emptyList()) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val location by cityViewModel.cityData.observeAsState(emptyList())
    val allCities = location.map { it.localizedName }

    val weather by weatherViewModel.weatherData.collectAsState()

    LaunchedEffect(Unit) {
        cityViewModel.fetchCityKey()

        cityViewModel.getFavouriteCitiesFromDataStore { cities ->
            favouriteCities = cities.map { City(it, "Načítání...") }

            weatherViewModel.updateWeatherForCities(favouriteCities.map { it.name })
        }

        launch {
            weatherViewModel.cityWeatherMap.collectLatest { cityWeatherMap ->
                favouriteCities = favouriteCities.map { city ->
                    val weather = cityWeatherMap[city.name]
                    weather?.let {
                        val temperature = it.temperature.metric.value.let { "$it°C" } ?: "N/A"
                        city.copy(temperature = temperature)
                    } ?: city
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        ExposedDropdownMenuBox(
            expanded = isMenuExpanded,
            onExpandedChange = { isMenuExpanded = !isMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(value = selectedCity ?: "",
                onValueChange = {},
                label = { Text("Vyber město") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { isMenuExpanded = !isMenuExpanded })
            ExposedDropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                allCities.forEach { cityName ->
                    DropdownMenuItem(text = { Text(cityName) }, onClick = {
                        weatherViewModel.updateCity(cityName)
                        selectedCity = cityName
                        isMenuExpanded = false
                    }, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        Button(
            onClick = {
                selectedCity?.let { cityName ->
                    val city = City(cityName, "${weather?.temperature?.metric?.value}°C")
                    favouriteCities = favouriteCities + city
                    cityViewModel.saveCityToDataStore(cityName)
                    selectedCity = null
                }
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Přidat do oblíbených")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(favouriteCities.size) { index ->
                val city = favouriteCities[index]
                FavouriteCityItem(city) {
                    favouriteCities = favouriteCities.filterNot { it.name == city.name }

                    cityViewModel.removeCityFromDataStore(city.name)
                }
            }
        }
    }
}

@Composable
fun FavouriteCityItem(city: City, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = city.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium
        )
        Text(
            text = city.temperature,
            modifier = Modifier.padding(end = 16.dp),
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Odstranit město",
                tint = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FavouriteCityScreen()
}
