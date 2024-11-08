package com.evernetica.unsplashjetpack.di

import com.evernetica.unsplashjetpack.data.RemoteDataSource
import com.evernetica.unsplashjetpack.data.UnsplashRepository
import org.koin.dsl.module

val remoteDataSourceModule= module {
    factory {  RemoteDataSource(get()) }
    factory {  UnsplashRepository(get()) }
}