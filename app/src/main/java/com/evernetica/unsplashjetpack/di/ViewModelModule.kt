package com.evernetica.unsplashjetpack.di

import com.evernetica.unsplashjetpack.view_model.PhotosListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { PhotosListViewModel(get(), get()) }
}