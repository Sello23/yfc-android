package com.yourfitness.coach.network.spike

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
class SpikeNetworkModule {

    @Provides
    fun provideRestApi(
        loggingInterceptor: HttpLoggingInterceptor,
        customInterceptor: CustomInterceptor,
    ): SpikeRestApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(customInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.spikeapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SpikeRestApi::class.java)
    }
}
