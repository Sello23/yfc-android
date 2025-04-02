package com.yourfitness.common.network

import com.yourfitness.common.network.YFCNetworkConfig.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    fun provideCommonRestApi(
        authInterceptor: AuthInterceptor,
        authenticator: YFCAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        secureInterceptor: YFCSecureInterceptor,
        errorInterceptor: YFCErrorInterceptor
    ): CommonRestApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(secureInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CommonRestApi::class.java)
    }

    @Provides
    fun provideAuthApi(
        loggingInterceptor: HttpLoggingInterceptor,
        secureInterceptor: YFCSecureInterceptor
    ): YFCAuthApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(secureInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(YFCAuthApi::class.java)
    }
}
