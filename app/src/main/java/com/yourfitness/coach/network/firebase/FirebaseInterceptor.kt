package com.yourfitness.coach.network.firebase

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class FirebaseInterceptor @Inject constructor(
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
            .header("x-spike-signature", SPIKE_SIGNATURE)
            .build()
        return chain.proceed(newRequest)
    }

    companion object {
        const val SPIKE_SIGNATURE = "8fc578ec9967f0fa3bf9a5c30f63cee8ed9ad557306d68fb7ac1c84104ac06a8"
    }
}