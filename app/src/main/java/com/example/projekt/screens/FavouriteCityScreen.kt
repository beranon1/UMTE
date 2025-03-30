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
import com.example.projekt.viewModels.CityViewModel
import org.koin.androidx.compose.koinViewModel


data class City(val name: String, val temperature: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteCityScreen(viewModel: CityViewModel = koinViewModel()) {

    LaunchedEffect (Unit) {
        viewModel.fetchCityKey()
    }

    var selectedCity by remember { mutableStateOf<String?>(null) }
    var favouriteCities by remember { mutableStateOf<List<City>>(emptyList()) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val weather by viewModel.cityData.observeAsState(emptyList())
    val allCities = weather.map { it.localizedName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Výběr města z nabídky
        ExposedDropdownMenuBox(
            expanded = isMenuExpanded,
            onExpandedChange = { isMenuExpanded = !isMenuExpanded }, // Změna stavu při otevření/zavření menu
            modifier = Modifier
                .fillMaxWidth() // Zajistí plnou šířku
        ) {
            OutlinedTextField(
                value = selectedCity ?: "",
                onValueChange = {},
                label = { Text("Vyber město") },
                readOnly = true, // Zabrání psaní do textového pole
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // Tento modifikátor umožní, aby menu bylo správně umístěno
                    .clickable { isMenuExpanded = !isMenuExpanded } // Otevře/zavře menu při kliknutí na textové pole // Posunutí níže
            )
            ExposedDropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier
                    .fillMaxWidth() // Zajistí plnou šířku
            ) {
                allCities.forEach { cityName ->
                    DropdownMenuItem(
                        text = { Text(cityName) },
                        onClick = {
                            selectedCity = cityName // Nastaví vybrané město
                            isMenuExpanded = false // Zavře menu po výběru města
                        },
                        modifier = Modifier
                            .fillMaxWidth() // Zajistí plnou šířku
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tlačítko pro přidání města do oblíbených
        Button(
            onClick = {
                selectedCity?.let { cityName ->
                    val city = City(cityName, "25°C") // Město s teplotou
                    favouriteCities = favouriteCities + city
                    selectedCity = null // Vymaže vybrané město po přidání
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Přidat do oblíbených")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zobrazení oblíbených měst
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(favouriteCities.size) { index ->
                val city = favouriteCities[index]
                FavouriteCityItem(city) {
                    // Odstranění města z oblíbených
                    favouriteCities = favouriteCities.filterNot { it.name == city.name }
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
            text = city.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = city.temperature,
            modifier = Modifier.padding(end = 16.dp),
            style = MaterialTheme.typography.bodySmall
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
