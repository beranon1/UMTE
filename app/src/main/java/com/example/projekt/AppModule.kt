package com.example.projekt

import com.example.projekt.api.WeatherApi
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.viewModels.WeatherViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<WeatherApi> {
        Retrofit.Builder()
            .baseUrl("https://dataservice.accuweather.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("User-Agent", "MyWeatherApp/1.0") // Přidání User-Agent
                            .addHeader("Accept", "application/json") // Přidání hlavičky Accept
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()
            .create(WeatherApi::class.java)
    }
    single { WeatherRepository(get()) }
    single { LocationProvider(androidContext()) }
    viewModel { WeatherViewModel(get()) }
}

