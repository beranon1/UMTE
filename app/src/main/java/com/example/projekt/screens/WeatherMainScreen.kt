package com.example.projekt.screens

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projekt.items.WeatherResponse
import com.example.projekt.viewModels.WeatherViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import java.util.Locale

@Composable
fun WeatherMainScreen(viewModel: WeatherViewModel, navController: NavHostController) {
    val weather by viewModel.weatherData.observeAsState()
    val location by viewModel.city.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){

        IconButton(onClick = {
            getCurrentLocation(navController.context, viewModel)
        }) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Získat polohu", tint = Color.Black)
        }

        weather?.let { location?.let { it1 -> WeatherContent(it, it1, navController, viewModel) } }


    }
}

@Composable
fun WeatherContent(weather: WeatherResponse, location: String, navController: NavHostController, viewModel: WeatherViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Město a stát
        Text(
            text = "${location} ",
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
        WeatherDetailRow(Icons.Default.Air, "Vítr", "${weather.wind.speed.metric.value} km/h")
        WeatherDetailRow(Icons.Default.WaterDrop, "Vlhkost", "${weather.humidity}%")
        WeatherDetailRow(Icons.Default.Visibility, "Viditelnost", "${weather.visibility.metric.value} km")
    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

@SuppressLint("MissingPermission") // Potřebuješ předtím ověřit oprávnění
fun getCurrentLocation(context: Context, viewModel: WeatherViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val cityName = addresses?.firstOrNull()?.locality ?: "Neznámé město"
                viewModel.updateCity(cityName)
                // Zde potřebuji zavolat WeatherMainScreen
            }
        }
    } catch (e: SecurityException) {
        Log.e("Location", "Přístup k poloze odepřen", e)
    }
}

