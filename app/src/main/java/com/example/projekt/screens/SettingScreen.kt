package com.example.projekt.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projekt.ui.theme.Blue80
import com.example.projekt.ui.theme.Purple40
import com.example.projekt.viewModels.NotificationContent
import com.example.projekt.viewModels.SettingsViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.LaunchedEffect
import com.example.projekt.viewModels.WeatherDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(navController: NavController, viewModel: SettingsViewModel = koinViewModel()) {

    LaunchedEffect(Unit) {
        viewModel.fetchLocationKey()
    }
    // Observe the states for Dark Theme and Notifications
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationInterval by viewModel.notificationInterval.collectAsState()
    val notificationContent by viewModel.notificationContent.collectAsState()

    val selectedOption = remember { mutableStateOf("Teplota") }  // Začíná na "Teplota"
    val showDialog = remember { mutableStateOf(false) }


    val contextForPermission = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.updateNotificationsEnabled(true)
                viewModel.scheduleNotification()
            } else {
                viewModel.updateNotificationsEnabled(false)
                viewModel.cancelNotifications()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Nastavení aplikace",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Dark Theme Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tmavý režim", modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { viewModel.toggleTheme(it) }
            )
        }

        // Switch for enabling/disabling notifications
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Zapnout notifikace", modifier = Modifier.weight(1f))
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                contextForPermission,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                viewModel.updateNotificationsEnabled(true)
                                viewModel.scheduleNotification()
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.updateNotificationsEnabled(true)
                        }
                    } else {
                        viewModel.updateNotificationsEnabled(false)
                        viewModel.cancelNotifications()
                    }
                }
            )
        }

        // Interval for notifications (if enabled)
        if (notificationsEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Interval:", modifier = Modifier.weight(1f))

                // Slider pro výběr mezi 1 a 24 hodinami
                Slider(
                    value = notificationInterval.toFloat(),
                    onValueChange = { newValue ->
                        viewModel.updateNotificationInterval(newValue.toInt())
                    },
                    valueRange = 1f..24f,  // Nastavujeme rozsah hodnot od 1 do 24
                    steps = 23,  // 23 kroky mezi 1 a 24
                    modifier = Modifier.fillMaxWidth(0.5f)
                )

                Text(text = "${notificationInterval} hodin", modifier = Modifier.padding(start = 8.dp))
            }

        }

        // Content for notifications
        if (notificationsEnabled) {
            // Clickable row to show dialog for selecting notification content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tlačítko pro otevření dialogu
                Button(
                    onClick = { showDialog.value = true },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            if (isDarkTheme) Blue80 // Tmavé téma - modrá
                            else Purple40 // Světlé téma - fialová
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Blue80 else Purple40
                    )
                ) {
                    Text(text = "Obsah notifikací")
                }

                // Zobrazení aktuálně vybrané možnosti vedle tlačítka
                Text(
                    text = notificationContent.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 40.dp)
                )
            }
        }

        // Dialog for selecting the notification content
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Vyberte typ notifikace") },
                text = {
                    Column {
                        TextButton(onClick = {
                            selectedOption.value = NotificationContent.TEPLOTA.name
                            viewModel.updateNotificationContent(NotificationContent.TEPLOTA)
                            showDialog.value = false
                        }) {
                            Text("Teplota")
                        }
                        TextButton(onClick = {
                            selectedOption.value = NotificationContent.VICE_INFORMACI.name
                            viewModel.updateNotificationContent(NotificationContent.VICE_INFORMACI)
                            showDialog.value = false
                        }) {
                            Text("Více informací")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Zavřít")
                    }
                }
            )
        }
    }
}


