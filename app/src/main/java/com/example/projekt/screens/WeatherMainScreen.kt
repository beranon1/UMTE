package com.example.projekt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projekt.items.WeatherResponse
import com.example.projekt.viewModels.WeatherViewModel
import coil.compose.AsyncImage

@Composable
fun WeatherMainScreen(viewModel: WeatherViewModel) {
    val weather by viewModel.weatherData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        weather?.let { weather ->
            WeatherContent(weather)
        } ?: CircularProgressIndicator()
    }
}

@Composable
fun WeatherContent(weather: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Město a stát
        Text(
            text = "${weather.location.name}, ${weather.location.country}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ikona počasí
        AsyncImage(
            model = weather.current.weather_icons.firstOrNull(),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Teplota a popis
        Text(
            text = "${weather.current.temperature}°C",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = weather.current.weather_descriptions.firstOrNull() ?: "",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Další informace o počasí
        WeatherDetailRow(Icons.Default.Air, "Vítr", "${weather.current.wind_speed} km/h")
        WeatherDetailRow(Icons.Default.WaterDrop, "Vlhkost", "${weather.current.humidity}%")
        WeatherDetailRow(Icons.Default.Visibility, "Viditelnost", "${weather.current.visibility} km")
    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

