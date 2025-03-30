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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule, repositoryModule, viewModelModule)
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Oprávnění bylo uděleno – můžeš získat polohu
                } else {
                    // Oprávnění bylo zamítnuto – můžeš zobrazit zprávu
                }
            }

        setContent {
            val permissionStatus = remember {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            val navController = rememberNavController()
            ProjektTheme {
                WeatherApp(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp( navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
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



data class BottomNavItem(val title: String, val icon: ImageVector, val screenRoute: String)
