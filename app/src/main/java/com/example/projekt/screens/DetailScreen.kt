package com.example.projekt.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projekt.viewModels.WeatherDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(navController: NavController, viewModel: WeatherDetailViewModel = koinViewModel()) {
    val weather by viewModel.weatherDetailData.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.updateLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pokud data nejsou ještě načtena, zobrazíme indikátor načítání
        if (weather == null) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        } else {
            val iconUrl = "https://developer.accuweather.com/sites/default/files/${weather?.weatherIcon.toString().padStart(2, '0')}-s.png"
            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailRow(Icons.Default.WbSunny, "Vlhkost vzduchu: ", weather?.relativeHumidity.let { "$it %" })
            WeatherDetailRow(Icons.Default.Air, "Rychlost větru: ", weather?.wind?.speed?.metric?.value.let { "$it ${weather?.wind?.speed?.metric?.unit} - ${weather?.wind?.direction?.english}" })
            WeatherDetailRow(Icons.Default.Visibility, "Viditelnost: ", weather?.visibility?.metric?.value.let { "$it ${weather?.visibility?.metric?.unit}" })
            WeatherDetailRow(Icons.Default.Cloud, "Oblačnost: ", weather?.cloudCover.let { "$it %" })
            WeatherDetailRow(Icons.Default.PanTool, "Tlak: ", weather?.pressure?.metric?.value.let { "$it ${weather?.pressure?.metric?.unit}" })
            WeatherDetailRow(Icons.Default.WaterDrop, "Množství srážek: ", weather?.precipitationSummary?.past24Hours?.metric?.value.let { "$it ${weather?.precipitationSummary?.past24Hours?.metric?.unit}" })
        }
    }
}
