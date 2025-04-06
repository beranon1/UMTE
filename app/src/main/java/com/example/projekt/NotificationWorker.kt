package com.example.projekt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.projekt.api.SetApi
import com.example.projekt.api.WeatherApi
import com.example.projekt.viewModels.NotificationContent
import com.example.projekt.viewModels.notificationContentKey
import com.example.projekt.viewModels.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val dataStore = context.settingsDataStore

        val locationKeyFlow = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("locationKey")] ?: "default_location_key" // Výchozí hodnota
        }

        val locationKey = runBlocking { locationKeyFlow.first() }
        Log.d("Worker","LocationKey - Worker ==> ${locationKey}")

        if (locationKey == "default_location_key") {
            return Result.failure() // Pokud není locationKey dostupný, ukončíme worker s chybou
        }

        // Vytvoření Retrofit instance pro API volání
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dataservice.accuweather.com/") // základní URL
            .addConverterFactory(GsonConverterFactory.create()) // pro deserializaci odpovědi
            .build()

        val weatherApiService = retrofit.create(WeatherApi::class.java)

        // Získání hodnoty interval notifikací a obsahu notifikace z DataStore
        val intervalFlow = dataStore.data.map { preferences ->
            preferences[notificationContentKey] ?: NotificationContent.TEPLOTA.name
        }

        // Načítáme interval pro notifikace
        val notificationContent = runBlocking {
            val content = intervalFlow.first() // načteme první hodnotu z Flow
            val weatherInfo = try {
                // API volání pro počasí
                val apiKey = SetApi.getApi
                val response = weatherApiService.getWeather(locationKey, apiKey)

                // Pokud dostaneme data, přetvoříme je podle požadavku
                response.firstOrNull()?.let { weather ->
                    when (NotificationContent.valueOf(content)) {
                        NotificationContent.TEPLOTA ->
                            "Aktuální teplota: ${weather.temperature.metric.value}°C"
                        NotificationContent.VICE_INFORMACI ->
                            "Teplota: ${weather.temperature.metric.value}°C\n" +
                                    "Pocitová teplota: ${weather.realFeelTemperature.metric.value}°C\n" +
                                    "UV Index: ${weather.uvIndex}"
                        else -> "Aktuální teplota: ${weather.temperature.metric.value}°C"
                    }
                } ?: "Počasí nedostupné"
            } catch (e: Exception) {
                e.printStackTrace()
                "Chyba při načítání počasí"
            }

            weatherInfo
        }

        // Vytvoření Intentu pro otevření aplikace při kliknutí na notifikaci
        val intent = Intent(context, MainActivity::class.java)
        // Můžeš přidat další data do intentu, pokud chceš, např. aktuální město nebo teplotu
        intent.putExtra("extra_key", "extra_value")

        // Vytvoření PendingIntent, který spustí aplikaci při kliknutí na notifikaci
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Vytvoření notifikace
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Vytvoření notifikačního kanálu pro Android O a novější
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_channel",
                "Weather Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val briefContent = "Aktuální teplota: ${notificationContent.substringAfter(": ").substringBefore("°C")}"
        val bigText = notificationContent // Obsah pro zobrazení v notifikaci

        // Sestavení notifikace
        val notification: Notification = NotificationCompat.Builder(context, "weather_channel")
            .setContentTitle("Počasí")
            .setContentText(briefContent) // Stručné informace pro neroztaženou notifikaci
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText)) // Roztažené zobrazení s detailními informacemi
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Nastavení PendingIntent pro otevření aplikace při kliknutí
            .setAutoCancel(true) // Po kliknutí se notifikace automaticky odstraní
            .build()

        // Zobrazení notifikace
        NotificationManagerCompat.from(context).notify(1, notification)

        // Vrátí SUCCESS, pokud úkol byl úspěšně dokončen
        return Result.success()
    }
}
