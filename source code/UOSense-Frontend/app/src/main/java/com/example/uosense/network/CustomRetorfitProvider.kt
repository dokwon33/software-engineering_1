package com.example.uosense.network

import AuthInterceptor
import RestaurantApi
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomRetrofitProvider(private val context: Context) {

    private val BASE_URL = "http://3.36.51.32:8080"

    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor(context)) // AuthInterceptor 추가
            .build()

    fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getRestaurantApi(): RestaurantApi {
        return getRetrofitInstance().create(RestaurantApi::class.java)
    }
}
