package com.yourfitness.common.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.lang.Exception
import java.util.Random
import javax.inject.Inject

class YFCErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        var tryCount = 1
        while (response.code == 429 && tryCount < 5) {
            tryCount++
            val delay = (1..tryCount * 2).random()
            Thread.sleep(delay * 1000L)
            try {
                response.close()
            } catch (e: Exception) {
                Timber.e(e)
            }

            response = chain.proceed(request)
        }

        return response
    }

    private fun IntRange.random() = Random().nextInt((endInclusive + 1) - start) + start
}
