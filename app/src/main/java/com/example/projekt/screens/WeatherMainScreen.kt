package com.example.projekt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.viewModels.WeatherViewModel
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherMainScreen(viewModel: WeatherViewModel= koinViewModel(), navController: NavHostController) {
    val weather by viewModel.weatherData.observeAsState()
    val location by viewModel.city.observeAsState()

    LaunchedEffect (Unit) {
        viewModel.updateLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        // Tlačítko pro získání polohy
        Button(
            //onClick = { getCurrentLocation(navController.context, viewModel) },
            onClick = { viewModel.updateLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Získat polohu", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Získat aktuální polohu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        weather?.let { location?.let { it1 -> WeatherContent(it, it1) } }
    }
}

@Composable
fun WeatherContent(weather: WeatherResponse, location: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Město a stát
        Text(
            text = location,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ikona počasí
        val iconUrl = "https://developer.accuweather.com/sites/default/files/${weather.weatherIcon.toString().padStart(2, '0')}-s.png"
        AsyncImage(
            model = iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Teplota
        Text(
            text = "${weather.temperature.metric.value}°C",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Další informace o počasí
        WeatherDetailRow(Icons.Default.WbSunny, "UV Index: ", "${weather.uvIndex}")
        WeatherDetailRow(Icons.Default.EmojiPeople, "Pocitová teplota: ", weather.realFeelTemperature.metric.value.let { "$it °C" } ?: "N/A")
        WeatherDetailRow(Icons.Default.AcUnit, "Minimální teplota: ", weather.temperatureSummary.past24HourRange.minTemperature.metric.value.let { "$it °C" } ?: "N/A")
        WeatherDetailRow(Icons.Default.Whatshot, "Maximální teplota: ", weather.temperatureSummary.past24HourRange.maxTemperature.metric.value.let { "$it °C" } ?: "N/A")

    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp)) // Mezera mezi ikonou a textem

        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f) // Zajišťuje zarovnání popisku doleva
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f), // Zajišťuje zarovnání hodnoty doprava
            textAlign = TextAlign.End // Zarovnání textu na pravou stranu
        )
    }
}

