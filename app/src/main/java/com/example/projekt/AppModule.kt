package com.example.projekt

import androidx.lifecycle.SavedStateHandle
import com.example.projekt.api.WeatherApi
import com.example.projekt.location.LocationProvider
import com.example.projekt.repository.WeatherRepository
import com.example.projekt.viewModels.CityViewModel
import com.example.projekt.viewModels.SettingsViewModel
import com.example.projekt.viewModels.WeatherDetailViewModel
import com.example.projekt.viewModels.WeatherViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit


val repositoryModule = module {
    single { WeatherRepository(get()) }
}

val viewModelModule = module {
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { WeatherDetailViewModel(get(), get()) }
    viewModel { CityViewModel(get(), get()) }
    single { LocationProvider(androidContext()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}

val appModule = module {
    single<WeatherApi> {
        Retrofit.Builder().baseUrl("https://dataservice.accuweather.com/")
            .addConverterFactory(GsonConverterFactory.create()).client(
                OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request =
                            chain.request().newBuilder().addHeader("User-Agent", "MyWeatherApp/1.0")
                                .addHeader("Accept", "application/json").build()
                        chain.proceed(request)
                    }.build()
            ).build().create(WeatherApi::class.java)
    }
}


