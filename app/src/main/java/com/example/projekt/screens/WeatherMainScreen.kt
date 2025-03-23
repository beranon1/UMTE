package com.example.projekt.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.projekt.R
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController



@Composable
fun WeatherMainScreen(navController: NavController) {
    val city = "Pardubice"
    val temperature = "15°C"
    val weatherDescription = "Slunečno"
    val windSpeed = "10 km/h"
    val humidity = "60%"
    val weatherIcon = painterResource(id = R.drawable.weather_icon)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = city, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Image(painter = weatherIcon, contentDescription = "Weather Icon", modifier = Modifier.size(100.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = temperature, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = weatherDescription, fontSize = 24.sp, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            WeatherDetail("🌬️ Vítr", windSpeed)
            WeatherDetail("💧 Vlhkost", humidity)
        }
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 18.sp, color = Color.White)
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
