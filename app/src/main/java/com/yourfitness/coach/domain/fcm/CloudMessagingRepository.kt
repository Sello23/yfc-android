package com.yourfitness.coach.domain.fcm

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudMessagingRepository @Inject constructor() {

    private val firebaseMessaging = FirebaseMessaging.getInstance()

    suspend fun fetchToken() = suspendCoroutine<String> {
        firebaseMessaging.token.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                it.resume(result.result)
            } else {
                it.resumeWithException(result.exception!!)
            }
        }
    }

    suspend fun deleteToken() = suspendCoroutine<Void> {
        firebaseMessaging.deleteToken().addOnCompleteListener { result ->
            if (result.isSuccessful) {
                it.resume(result.result)
            } else {
                it.resumeWithException(result.exception!!)
            }
        }
    }
}