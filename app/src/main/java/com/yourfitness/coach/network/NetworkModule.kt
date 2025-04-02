package com.yourfitness.coach.network

import com.yourfitness.common.network.YFCNetworkConfig.BASE_URL
import com.yourfitness.common.network.AuthInterceptor
import com.yourfitness.common.network.YFCAuthenticator
import com.yourfitness.common.network.YFCErrorInterceptor
import com.yourfitness.common.network.YFCSecureInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideRestApi(
        authInterceptor: AuthInterceptor,
        authenticator: YFCAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        secureInterceptor: YFCSecureInterceptor,
        errorInterceptor: YFCErrorInterceptor
    ): YFCRestApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(secureInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .callTimeout(Duration.ofSeconds(90))
            .readTimeout(Duration.ofSeconds(90))
            .writeTimeout(Duration.ofSeconds(90))
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(YFCRestApi::class.java)
    }
}
