package com.yourfitness.coach.domain.session

import java.net.UnknownHostException

suspend fun <T> handleOfflineErrors(block: suspend () -> T): T {
    try {
        return block()
    } catch (error: UnknownHostException) {
        throw error.toOfflineException()
    }
}

class OfflineException(message: String, error: Throwable? = null) : Exception(message, error)

fun Exception.toOfflineException(): OfflineException {
    return OfflineException("You are offline. Please, check your internet connection.", this)
}
