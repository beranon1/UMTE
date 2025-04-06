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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projekt.viewModels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       /* startKoin {
            androidContext(this@MainActivity)
            modules(appModule, repositoryModule, viewModelModule)
        }*/

        createNotificationChannel()
        checkAndRequestPermissions()
        setContent {
            val navController = rememberNavController()


            // 👉 Získání SettingsViewModel

            val settingsViewModel: SettingsViewModel = koinViewModel()

            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            // ✅ Předání hodnoty motivu do ProjektTheme
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
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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
fun WeatherApp( navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weather App",
                        color = MaterialTheme.colorScheme.onPrimary // kontrastní text
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary // automaticky světlé/tmavé pozadí
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Navigation(navController = navController, innerPadding = innerPadding)
    }
}


@Composable
fun Navigation(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.padding(innerPadding) // 🛠 Aplikace paddingu
    ) {
        composable("main") {
            val viewModel: WeatherViewModel = koinViewModel()
            WeatherMainScreen(viewModel, navController)
        }
        composable("detail") { DetailScreen(navController) }
        composable("favourites") { FavouriteCityScreen() }
        composable("settings") { SettingScreen(navController) }
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
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.screenRoute,
                onClick = { navController.navigate(item.screenRoute) }
            )
        }
    }
}

fun showTestNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, "weather_channel")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Tady zajisti, že máš platnou ikonu!
        .setContentTitle("Test Notifikace")
        .setContentText("Tohle je testovací notifikace o počasí.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Notifikace zmizí po kliknutí
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Využití všech výchozích signálů, jako zvuk, vibrace

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1001, builder.build())
}

data class BottomNavItem(val title: String, val icon: ImageVector, val screenRoute: String)
