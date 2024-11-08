package com.evernetica.unsplashjetpack

import android.app.Application
import com.evernetica.unsplashjetpack.di.networkModule
import com.evernetica.unsplashjetpack.di.remoteDataSourceModule
import com.evernetica.unsplashjetpack.di.useCase
import com.evernetica.unsplashjetpack.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(networkModule, remoteDataSourceModule, useCase, viewModelModule)
        }
    }
}