package com.example.projekt

import com.example.projekt.api.WeatherApi
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.viewModels.WeatherViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<WeatherApi> {
        Retrofit.Builder()
            .baseUrl("http://api.weatherstack.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
            .create(WeatherApi::class.java)
    }
    single { WeatherRepository(get()) }
    viewModel { WeatherViewModel(get()) }
}
