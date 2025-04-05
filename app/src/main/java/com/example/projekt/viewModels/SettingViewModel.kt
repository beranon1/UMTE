package com.example.projekt.viewModels

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
val Context.settingsDataStore by preferencesDataStore(name = "settings")

val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
val notificationIntervalKey = intPreferencesKey("notification_interval")
val notificationContentKey = stringPreferencesKey("notification_content")


class SettingsViewModel(context: Context) : ViewModel() {

    private val dataStore = context.settingsDataStore

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
            preferences[notificationContentKey] ?: "Temperature" // výchozí: teplota
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Temperature"
        )

    // Funkce pro zapnutí/vypnutí notifikací
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationsEnabledKey] = enabled
            }
        }
    }

    // Funkce pro nastavení intervalu notifikací
    fun updateNotificationInterval(interval: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationIntervalKey] = interval
            }
        }
    }

    // Funkce pro nastavení obsahu notifikací
    fun updateNotificationContent(content: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[notificationContentKey] = content
            }
        }
    }
}
