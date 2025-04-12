package com.example.projekt.viewModels

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projekt.notification.NotificationWorker
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
    TEPLOTA, VICE_INFORMACI
}

class SettingsViewModel(
    context: Context,
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val appContext = context.applicationContext
    private val dataStore = context.settingsDataStore

    private val locationKey = stringPreferencesKey("locationKey")


    val isDarkTheme = dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: false
        }.stateIn(
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


    val notificationsEnabled = dataStore.data.map { preferences ->
            preferences[notificationsEnabledKey] ?: false
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val notificationInterval = dataStore.data.map { preferences ->
            preferences[notificationIntervalKey] ?: 1
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 1
        )

    val notificationContent = dataStore.data.map { preferences ->
            val contentValue =
                preferences[notificationContentKey] ?: NotificationContent.TEPLOTA.name
            NotificationContent.valueOf(contentValue)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationContent.TEPLOTA
        )

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

    fun updateNotificationInterval(interval: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationIntervalKey] = interval
            }
        }

    }

    // Funkce pro nastavení obsahu notifikací
    fun updateNotificationContent(content: NotificationContent) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationContentKey] = content.name
            }
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
            locationKey?.let { saveLocationKey(it) }
        }
    }

    suspend fun saveLocationKey(locationKey: String) {
        dataStore.edit { preferences ->
            preferences[this.locationKey] = locationKey
        }
    }
}

