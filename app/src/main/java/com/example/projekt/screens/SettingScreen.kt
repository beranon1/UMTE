package com.example.projekt.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projekt.ui.theme.Blue80
import com.example.projekt.ui.theme.Purple40
import com.example.projekt.ui.theme.PurpleGrey40
import com.example.projekt.viewModels.SettingsViewModel

@Composable
fun SettingScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val settingsViewModel = viewModel<SettingsViewModel>(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(context) as T
        }
    })

    // Observe the states for Dark Theme and Notifications
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val notificationInterval by settingsViewModel.notificationInterval.collectAsState()
    val notificationContent by settingsViewModel.notificationContent.collectAsState()

    val selectedOption = remember { mutableStateOf("Teplota") }  // Začíná na "Teplota"
    val showDialog = remember { mutableStateOf(false) }

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
                onCheckedChange = { settingsViewModel.toggleTheme(it) }
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
                onCheckedChange = { settingsViewModel.updateNotificationsEnabled(it) }
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
                        settingsViewModel.updateNotificationInterval(newValue.toInt())
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
                    text = selectedOption.value,
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
                            selectedOption.value = "Pouze teplota"
                            settingsViewModel.updateNotificationContent("Teplota")
                            showDialog.value = false
                        }) {
                            Text("Teplota")
                        }
                        TextButton(onClick = {
                            selectedOption.value = "Více informací"
                            settingsViewModel.updateNotificationContent("Více informací")
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


