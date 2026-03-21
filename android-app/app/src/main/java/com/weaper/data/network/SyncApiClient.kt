package com.weaper.data.network

import com.weaper.data.preferences.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncApiClient @Inject constructor(
    private val preferences: AppPreferences
) {
    private var cachedService: SyncApiService? = null
    private var cachedBaseUrl: String? = null

    fun getService(): SyncApiService {
        val currentUrl = preferences.syncServerUrl
        if (cachedService == null || cachedBaseUrl != currentUrl) {
            cachedService = buildService(currentUrl)
            cachedBaseUrl = currentUrl
        }
        return cachedService!!
    }

    private fun buildService(baseUrl: String): SyncApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SyncApiService::class.java)
    }
}
