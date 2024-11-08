package com.evernetica.unsplashjetpack.di

import com.evernetica.unsplashjetpack.use_case.UnsplashUseCase
import org.koin.dsl.module

val useCase = module {
    single { UnsplashUseCase(get()) }
}