package com.example.projekt

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Zajištění, že Koin je inicializován pouze jednou
        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MyApplication)
                modules(appModule, repositoryModule, viewModelModule) // Tvé moduly
            }
        }
    }
}
