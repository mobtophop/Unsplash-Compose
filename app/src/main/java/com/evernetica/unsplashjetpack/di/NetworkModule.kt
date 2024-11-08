package com.evernetica.unsplashjetpack.di

import android.util.Log
import com.evernetica.unsplashjetpack.BuildConfig
import com.evernetica.unsplashjetpack.api.UnsplashApiService
import com.evernetica.unsplashjetpack.utils.Constants.AUTH
import com.evernetica.unsplashjetpack.utils.Constants.BASE_URL
import com.evernetica.unsplashjetpack.utils.Constants.NETWORK
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private val json by lazy {
    Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = false
        allowStructuredMapKeys = true
        prettyPrint = false
        coerceInputValues = true
        useArrayPolymorphism = false
    }
}

val contentType = "application/json".toMediaType()

fun provideHttpClient(
    logging: LoggingInterceptor,
    requestInterceptor: Interceptor,
): OkHttpClient {
    return OkHttpClient
        .Builder()
        .addInterceptor(logging)
        .addInterceptor(requestInterceptor)
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()
}

fun provideRequestInterceptor() = Interceptor {
    val builder = it.request().newBuilder().url(it.request().url)
    builder.header(AUTH, "Client-ID ${BuildConfig.TOKEN}")
    it.proceed(builder.build())
}

fun loggingInterceptor(): LoggingInterceptor {
    return LoggingInterceptor.Builder()
        .setLevel(Level.BASIC)
        .log(Log.INFO)
        .request(NETWORK)
        .response(NETWORK)
        .build()
}

fun provideRetrofit(
    okHttpClient: OkHttpClient,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)

        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}

fun provideService(retrofit: Retrofit): UnsplashApiService =
    retrofit.create(UnsplashApiService::class.java)


val networkModule= module {
    single { provideHttpClient(get(), get()) }
    single { provideRequestInterceptor() }
    single { provideRetrofit(get()) }
    single { provideService(get()) }
    single { loggingInterceptor() }
}