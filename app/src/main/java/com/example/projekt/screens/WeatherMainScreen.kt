package com.example.projekt.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.projekt.responses.WeatherResponse
import com.example.projekt.viewModels.WeatherViewModel
import coil.compose.AsyncImage
import com.example.projekt.responses.DailyForecast
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherMainScreen(viewModel: WeatherViewModel = koinViewModel()) {
    val weather by viewModel.weatherData.collectAsState()
    val city by viewModel.city.observeAsState()
    val forecast by viewModel.forecastData.collectAsState()
    val location by viewModel.locationKey.observeAsState()


    LaunchedEffect(Unit) {
        viewModel.updateLocation()

    }

    LaunchedEffect(location) {
        location?.let {
            Log.d("Location key", "Spouštím denní předpověď pro: $it")
            viewModel.fetchForecast(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (weather == null) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        } else {

            Button(
                onClick = { viewModel.updateLocation() }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Získat polohu",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Získat aktuální polohu")
            }

            Spacer(modifier = Modifier.height(5.dp))

            weather?.let { city?.let { it1 -> WeatherContent(it, it1) } }

            forecast?.let { forecastData ->
                ForecastSection(forecastData.dailyForecasts) { date ->
                    if (date.contains(",")) {
                        date
                    } else {
                        viewModel.formatDate(date)
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastSection(dailyForecasts: List<DailyForecast>, formatDate: (String) -> String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        dailyForecasts.forEach { forecast ->
            ForecastCard(forecast, formatDate)
        }
    }
}

@Composable
fun ForecastCard(forecast: DailyForecast, formatDate: (String) -> String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            val iconUrl = "https://developer.accuweather.com/sites/default/files/${
                forecast.day.icon.toString().padStart(2, '0')
            }-s.png"
            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = formatDate(forecast.date),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${forecast.temperature.minimum.value}°C / ${forecast.temperature.maximum.value}°C",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = forecast.day.iconPhrase,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
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


        Text(
            text = location,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 8.em,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ikona počasí
        val iconUrl = "https://developer.accuweather.com/sites/default/files/${
            weather.weatherIcon.toString().padStart(2, '0')
        }-s.png"
        AsyncImage(
            model = iconUrl, contentDescription = "Weather Icon", modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = "${weather.temperature.metric.value}°C",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))


        WeatherDetailRow(Icons.Default.WbSunny, "UV Index: ", "${weather.uvIndex}")
        WeatherDetailRow(Icons.Default.EmojiPeople,
            "Pocitová teplota: ",
            weather.realFeelTemperature.metric.value.let { "$it °C" } ?: "N/A")
        WeatherDetailRow(Icons.Default.AcUnit,
            "Minimální teplota: ",
            weather.temperatureSummary.past24HourRange.minTemperature.metric.value.let { "$it °C" } ?: "N/A")
        WeatherDetailRow(Icons.Default.Whatshot,
            "Maximální teplota: ",
            weather.temperatureSummary.past24HourRange.maxTemperature.metric.value.let { "$it °C" } ?: "N/A")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Předpověď",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 5.em,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onBackground
        )
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

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

