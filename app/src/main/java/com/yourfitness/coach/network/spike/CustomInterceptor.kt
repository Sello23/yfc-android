package com.yourfitness.coach.network.spike

import com.yourfitness.coach.BuildConfig.SPIKE_AUTH_TOKEN
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CustomInterceptor @Inject constructor(
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
            .header("User-Agent", "Chrome/62.0.3202.94")
            .header("x-spike-auth", SPIKE_AUTH_TOKEN)
            .build()
        return chain.proceed(newRequest)
    }
}