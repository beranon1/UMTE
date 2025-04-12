package com.example.projekt.notification

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
import com.example.projekt.MainActivity
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

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val dataStore = context.settingsDataStore

        val locationKeyFlow = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("locationKey")] ?: "default_location_key"
        }

        val locationKey = runBlocking { locationKeyFlow.first() }
        Log.d("Worker", "LocationKey - Worker ==> ${locationKey}")

        if (locationKey == "default_location_key") {
            return Result.failure()
        }


        val retrofit = Retrofit.Builder()
            .baseUrl("https://dataservice.accuweather.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApiService = retrofit.create(WeatherApi::class.java)

        val intervalFlow = dataStore.data.map { preferences ->
            preferences[notificationContentKey] ?: NotificationContent.TEPLOTA.name
        }


        val notificationContent = runBlocking {
            val content = intervalFlow.first()
            val weatherInfo = try {

                val apiKey = SetApi.getApi
                val response = weatherApiService.getWeather(locationKey, apiKey)

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

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("extra_key", "extra_value")

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_channel",
                "Weather Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val briefContent =
            "Aktuální teplota: ${notificationContent.substringAfter(": ").substringBefore("°C")}"
        val bigText = notificationContent // Obsah pro zobrazení v notifikaci

        val notification: Notification = NotificationCompat.Builder(context, "weather_channel")
            .setContentTitle("Počasí")
            .setContentText(briefContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()


        NotificationManagerCompat.from(context).notify(1, notification)


        return Result.success()
    }
}
