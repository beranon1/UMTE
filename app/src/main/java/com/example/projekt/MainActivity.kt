package com.example.projekt

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.projekt.screens.*
import com.example.projekt.ui.theme.ProjektTheme
import com.example.projekt.viewModels.WeatherViewModel
import org.koin.androidx.compose.koinViewModel
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.projekt.viewModels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        checkAndRequestPermissions()
        setContent {
            val navController = rememberNavController()

            val settingsViewModel: SettingsViewModel = koinViewModel()

            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            ProjektTheme(darkTheme = isDarkTheme) {
                WeatherApp(navController)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Notifications"
            val descriptionText = "Notifikace o počasí"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("weather_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestPermissions() {
        val locationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Oprávnění pro polohu bylo uděleno
            } else {
                // Oprávnění pro polohu bylo zamítnuto
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(navController: NavHostController) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Weather App", color = MaterialTheme.colorScheme.onPrimary
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )
    }, bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->
        Navigation(navController = navController, innerPadding = innerPadding)
    }
}


@Composable
fun Navigation(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("main") {
            val viewModel: WeatherViewModel = koinViewModel()
            WeatherMainScreen(viewModel)
        }
        composable("detail") { DetailScreen() }
        composable("favourites") { FavouriteCityScreen() }
        composable("settings") { SettingScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Hlavní", Icons.Default.Home, "main"),
        BottomNavItem("Detail", Icons.Default.Info, "detail"),
        BottomNavItem("Oblíbené", Icons.Default.Favorite, "favourites"),
        BottomNavItem("Nastavení", Icons.Default.Settings, "settings")
    )
    NavigationBar {
        val currentRoute = navController.currentDestination?.route
        items.forEach { item ->
            NavigationBarItem(icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.screenRoute,
                onClick = { navController.navigate(item.screenRoute) })
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val screenRoute: String)
