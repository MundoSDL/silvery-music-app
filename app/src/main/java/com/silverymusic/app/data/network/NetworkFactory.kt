package com.silverymusic.app.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.silverymusic.app.BuildConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Hand-rolled wiring for the two remote sources. Everything is lazy and shares
 * one [OkHttpClient] so the connection pool and dispatcher are not duplicated.
 */
internal object NetworkFactory {

    private const val TIMEOUT_SECONDS = 15L
    private const val USER_AGENT = "SilveryMusic/${BuildConfig.VERSION_NAME} (Hochschule Trier course demo)"

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        // Jamendo occasionally quotes numeric fields; lenient keeps that from
        // failing a whole page of results.
        isLenient = true
    }

    private val baseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor(USER_AGENT))
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun jamendoService(clientId: String): JamendoService {
        val client = baseClient.newBuilder()
            .addInterceptor(JamendoCredentialsInterceptor(clientId))
            .build()
        return retrofit(JamendoService.BASE_URL, client).create(JamendoService::class.java)
    }

    fun lrcLibService(): LrcLibService =
        retrofit(LrcLibService.BASE_URL, baseClient).create(LrcLibService::class.java)

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
}

/** Adds the two query params every Jamendo endpoint requires. */
private class JamendoCredentialsInterceptor(private val clientId: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .setQueryParameter("client_id", clientId)
            .setQueryParameter("format", "json")
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}

/** LRCLIB rejects requests without one; Jamendo doesn't mind. */
private class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder().header("User-Agent", userAgent).build(),
    )
}
