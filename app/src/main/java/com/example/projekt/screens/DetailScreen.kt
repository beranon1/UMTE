package com.example.projekt.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import com.example.projekt.responses.HourlyForecastResponse
import com.example.projekt.viewModels.WeatherDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(viewModel: WeatherDetailViewModel = koinViewModel()) {
    val weather by viewModel.weatherDetailData.observeAsState()
    val hourlyForecast by viewModel.hourlyForecastData.observeAsState()
    val location by viewModel.locationKey.observeAsState()


    LaunchedEffect(Unit) {
        viewModel.updateLocation()
    }
    LaunchedEffect(location) {
        location?.let {
            viewModel.fetchHourlyForecast(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (weather == null) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        } else {
            val iconUrl = "https://developer.accuweather.com/sites/default/files/${
                weather?.weatherIcon.toString().padStart(2, '0')
            }-s.png"
            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailRow(Icons.Default.WbSunny,
                "Vlhkost vzduchu: ",
                weather?.relativeHumidity.let { "$it %" })
            WeatherDetailRow(Icons.Default.Air,
                "Rychlost větru: ",
                weather?.wind?.speed?.metric?.value.let { "$it ${weather?.wind?.speed?.metric?.unit} - ${weather?.wind?.direction?.english}" })
            WeatherDetailRow(Icons.Default.Visibility,
                "Viditelnost: ",
                weather?.visibility?.metric?.value.let { "$it ${weather?.visibility?.metric?.unit}" })
            WeatherDetailRow(Icons.Default.Cloud,
                "Oblačnost: ",
                weather?.cloudCover.let { "$it %" })
            WeatherDetailRow(Icons.Default.PanTool,
                "Tlak: ",
                weather?.pressure?.metric?.value.let { "$it ${weather?.pressure?.metric?.unit}" })
            WeatherDetailRow(Icons.Default.WaterDrop,
                "Množství srážek: ",
                weather?.precipitationSummary?.past24Hours?.metric?.value.let { "$it ${weather?.precipitationSummary?.past24Hours?.metric?.unit}" })

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Hodinová předpověď",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 4.em,
                textAlign = TextAlign.Left,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (hourlyForecast != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    hourlyForecast?.forEach { forecast ->
                        HourlyForecastRow(forecast, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastRow(forecast: HourlyForecastResponse, viewModel: WeatherDetailViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconUrl = "https://developer.accuweather.com/sites/default/files/${
                forecast.weatherIcon.toString().padStart(2, '0')
            }-s.png"
            val formattedTime = viewModel.formatDateAndTime(forecast.dateTime)

            Spacer(modifier = Modifier.width(25.dp))

            Text(text = formattedTime)

            Spacer(modifier = Modifier.width(16.dp))

            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = "${forecast.temperature.value}°C")
        }
    }
}

