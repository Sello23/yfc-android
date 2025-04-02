package com.yourfitness.coach.domain.fcm

import com.yourfitness.coach.domain.PLATFORM_ANDROID
import com.yourfitness.coach.domain.REGION_UAE
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.UpdateProfileRequest
import com.yourfitness.common.domain.ProfileRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudMessagingManager @Inject constructor(
    private val cloudMessagingRepository: CloudMessagingRepository,
    private val profileRepository: ProfileRepository,
    private val restApi: YFCRestApi,
    private val sessionManager: SessionManager
) {
    private val mutex = Mutex()

    suspend fun sendToken(token: String? = null) = mutex.withLock {
        Timber.d("sendToken: $token")
       if (!sessionManager.isLoggedIn()) return
        val profile = profileRepository.getProfile()
        val pushToken = profile.pushToken
        if (pushToken != token) {
            val request = UpdateProfileRequest(
                email = profile.email,
                name = profile.name,
                surname = profile.surname,
                phoneNumber = profile.phoneNumber,
                mediaId = profile.mediaId,
                instagram = profile.instagram,
                gender = profile.gender?.ordinal,
                birthday = profile.birthday,
                region = REGION_UAE,
                platform = PLATFORM_ANDROID,
                pushToken = token,
                voucher = profile.voucher
            )
            restApi.updateProfile(request)
            profileRepository.updateProfile(profile.copy(pushToken = token))
        }
    }

    suspend fun sendToken() {
        try {
            val token = cloudMessagingRepository.fetchToken()
            sendToken(token)
        } catch (error: Exception) {
            Timber.e(error)
        }
    }
}
