package com.basesource.base.di

import coil.ImageLoader
import coil.request.CachePolicy
import com.basesource.base.BuildConfig
import com.basesource.base.network.AuthInterceptor
import com.basesource.base.network.NetworkUtils
import com.basesource.base.network.mock.MockInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.basesource.base.network.CollageApiService

val networkModule = module {

    single<ImageLoader> {
        ImageLoader.Builder(get())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    single<Gson> {
        GsonBuilder()
            .setLenient()
            .create()
    }

    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .addInterceptor(AuthInterceptor { null })
            .addInterceptor(MockInterceptor(get()))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://proxy-future-self.footballtv.info/") // Replace with your base URL
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single<CollageApiService> {
        get<Retrofit>().create(CollageApiService::class.java)
    }

}

