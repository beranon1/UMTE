package com.example.projekt.viewModels

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projekt.NotificationWorker
import com.example.projekt.api.SetApi
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
val Context.settingsDataStore by preferencesDataStore(name = "settings")

val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
val notificationIntervalKey = intPreferencesKey("notification_interval")
val notificationContentKey = stringPreferencesKey("notification_content")

enum class NotificationContent {
    TEPLOTA,
    VICE_INFORMACI
}

class SettingsViewModel(context: Context,
                        private val repository: WeatherRepository,
                        private val locationProvider: LocationProvider
) : ViewModel() {

    private val appContext = context.applicationContext
    private val dataStore = context.settingsDataStore

    private val locationKey = stringPreferencesKey("locationKey")


    val isDarkTheme = dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: false // výchozí: světlé téma
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[THEME_KEY] = enabled
            }
        }
    }

    // Správa notifikací
    val notificationsEnabled = dataStore.data
        .map { preferences ->
            preferences[notificationsEnabledKey] ?: false // výchozí: vypnuto
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val notificationInterval = dataStore.data
        .map { preferences ->
            preferences[notificationIntervalKey] ?: 1 // výchozí: 1 hodina
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1
        )

    val notificationContent = dataStore.data
        .map { preferences ->
            // Načteme hodnotu z DataStore a vrátíme odpovídající enum
            val contentValue = preferences[notificationContentKey] ?: NotificationContent.TEPLOTA.name
            NotificationContent.valueOf(contentValue) // Převádíme text zpět na enum
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationContent.TEPLOTA // Výchozí hodnota: teplota
        )

    // Funkce pro zapnutí/vypnutí notifikací
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationsEnabledKey] = enabled
            }
            if (enabled) {
                scheduleNotification()
            } else {
                cancelNotifications()
            }
        }
    }

    // Funkce pro nastavení intervalu notifikací
    fun updateNotificationInterval(interval: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationIntervalKey] = interval
                Log.d("SettingsViewModel", "Interval notifikací: $interval") // Log pro kontrolu
            }
        }

    }

    // Funkce pro nastavení obsahu notifikací
    fun updateNotificationContent(content: NotificationContent) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationContentKey] = content.name // Uložíme název enumu (např. "TEMPERATURE")
            }
            Log.d("SettingsViewModel", "Notifikace uloženy: ${content.name}") // Log pro kontrolu

        }
    }

    fun scheduleNotification() {
        viewModelScope.launch {
            val interval = dataStore.data.map { preferences ->
                preferences[notificationIntervalKey] ?: 1
            }.first()

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                interval.toLong(), TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(appContext).cancelAllWork()
            WorkManager.getInstance(appContext).enqueue(workRequest)
        }
    }


    fun cancelNotifications() {
        WorkManager.getInstance(appContext).cancelAllWork()
    }

    fun fetchLocationKey() {
        viewModelScope.launch {
            val city = locationProvider.getCityName()
            val objectJson = city?.let { repository.getRawLocationResponse(it, SetApi.getApi) }

            val locationKey = objectJson?.getString("Key")
            Log.d("WeatherViewModel", "Získaný locationKey: $locationKey")
            locationKey?.let { saveLocationKey(it) }
        }
    }

    suspend fun saveLocationKey(locationKey: String) {
        dataStore.edit { preferences ->
            preferences[this.locationKey] = locationKey
        }
    }
}

