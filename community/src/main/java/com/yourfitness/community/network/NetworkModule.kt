package com.yourfitness.community.network

import com.yourfitness.common.network.AuthInterceptor
import com.yourfitness.common.network.YFCAuthenticator
import com.yourfitness.common.network.YFCNetworkConfig.BASE_URL
import com.yourfitness.common.network.YFCSecureInterceptor
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
    fun provideRestApi(
        authInterceptor: AuthInterceptor,
        authenticator: YFCAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        secureInterceptor: YFCSecureInterceptor
    ): CommunityRestApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(secureInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CommunityRestApi::class.java)
    }
}
