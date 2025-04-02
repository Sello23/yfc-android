package com.yourfitness.coach.network.firebase

import com.yourfitness.common.network.YFCAuthenticator
import com.yourfitness.common.network.YFCNetworkConfig.BASE_FIREBASE_URL
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
class FirebaseNetworkModule {

    @Provides
    fun provideRestApi(
        loggingInterceptor: HttpLoggingInterceptor,
        firebaseInterceptor: FirebaseInterceptor,
        authenticator: YFCAuthenticator,
        secureInterceptor: YFCSecureInterceptor
    ): FirebaseRestApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(firebaseInterceptor)
            .addInterceptor(secureInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(FirebaseRestApi::class.java)
    }
}
