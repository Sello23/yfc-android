package com.yourfitness.coach.domain.session

import com.yourfitness.coach.BannerState
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.YFCDatabase
import com.yourfitness.coach.domain.PLATFORM_ANDROID
import com.yourfitness.coach.domain.REGION_UAE
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.fcm.CloudMessagingRepository
import com.yourfitness.coach.domain.spike.SpikeSdkRepository
import com.yourfitness.coach.domain.spike.SpikeApiRepository
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.LoginRequest
import com.yourfitness.coach.network.dto.LoginResponse
import com.yourfitness.coach.network.dto.LoginWithCorpRequest
import com.yourfitness.coach.network.dto.toException
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.CommonDatabase
import com.yourfitness.common.data.TokenStorage
import com.yourfitness.common.data.dao.ProfileDao
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.validation.Validation
import com.yourfitness.community.data.CommunityDatabase
import com.yourfitness.community.data.CommunityStorage
import com.yourfitness.pt.data.PtDatabase
import com.yourfitness.pt.data.PtStorage
import com.yourfitness.shop.data.ShopDatabase
import com.yourfitness.shop.data.ShopStorage
import retrofit2.HttpException
import javax.inject.Inject

class SessionManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val tokenStorage: TokenStorage,
    private val preferencesStorage: PreferencesStorage,
    private val commonStorage: CommonPreferencesStorage,
    private val cloudMessagingRepository: CloudMessagingRepository,
    private val db: YFCDatabase,
    private val commonDb: CommonDatabase,
    private val shopDb: ShopDatabase,
    private val ptDb: PtDatabase,
    private val communityDb: CommunityDatabase,
    private val communityStorage: CommunityStorage,
    private val shopStorage: ShopStorage,
    private val ptStorage: PtStorage,
    private val profileDao: ProfileDao,
    private val spikeSdkRepository: SpikeSdkRepository,
    private val spikeApiRepository: SpikeApiRepository,
) {

    fun isLoggedIn(): Boolean {
        return tokenStorage.accessToken != null
    }

    fun isFirstStart(): Boolean {
        val firstStart = tokenStorage.firstStart
        tokenStorage.firstStart = false
        return firstStart
    }

    suspend fun logout() {
        BannerState.setDefaultValue()
        spikeSdkRepository.disconnect()
        spikeApiRepository.disconnect()

        db.clearDb()
        commonDb.clearDb()
        shopDb.clearDb()
        ptDb.clearDb()
        communityDb.clearDb()

        preferencesStorage.clear()
        commonStorage.clear()
        shopStorage.clearShopData()
        ptStorage.clearPtData()
        communityStorage.clearCommunityData()

        tokenStorage.clearTokens()
        cloudMessagingRepository.deleteToken()
    }

    suspend fun checkPhone(phone: String, flow: Flow) = handleOfflineErrors {
        val normalizedPhone = Validation.normalizePhone(phone)
        profileDao.saveProfile(ProfileEntity.empty.copy(phoneNumber = normalizedPhone))
        val isAvailable = restApi.checkPhone(normalizedPhone)
        if (flow == Flow.SIGN_UP && !isAvailable) {
            throw SessionManagerException.PhoneException("User registered")
        } else if (flow == Flow.SIGN_IN && isAvailable) {
            throw SessionManagerException.PhoneException("User not registered")
        }
    }

    suspend fun createOtpCode(phone: String, sender: String): String {
        try {
            val normalizedPhone = Validation.normalizePhone(phone)
            return restApi.createOtpCode(normalizedPhone, sender)
        } catch (error: HttpException) {
            throw error.toException()
        }
    }

    suspend fun confirmCode(code: String, otpId: String, phone: String) = handleOfflineErrors {
        try {
            restApi.verifyOtpCode(code, otpId, Validation.normalizePhone(phone))
        } catch (error: HttpException) {
            throw error.toException()
        }
    }

    suspend fun resendCode(otpId: String, phone: String, sender: String) {
        try {
            restApi.resendCode(otpId, Validation.normalizePhone(phone), sender)
        } catch (error: HttpException) {
            throw error.toException()
        }
    }

    suspend fun login(phone: String, code: String, otpId: String) {
        confirmCode(code, otpId, phone)
        try {
            val request = LoginRequest(
                phoneNumber = Validation.normalizePhone(phone),
                otpId = otpId,
                region = REGION_UAE,
                platform = PLATFORM_ANDROID
            )
            val response = restApi.login(request)
            saveTokens(response)
        } catch (error: HttpException) {
            throw error.toException()
        }
    }

    suspend fun createAccount(user: User) = handleOfflineErrors {
        val loginResponse = if (user.corporationId.isEmpty()) {
            restApi.login(LoginRequest(
                otpId = user.otpId,
                phoneNumber = Validation.normalizePhone(user.phone),
                email = user.email,
                name = user.name,
                surname = user.surname,
                region = REGION_UAE,
                platform = PLATFORM_ANDROID,
            ))
        } else {
            restApi.loginCorp(LoginWithCorpRequest(
                otpId = user.otpId,
                phoneNumber = Validation.normalizePhone(user.phone),
                email = user.email,
                name = user.name,
                surname = user.surname,
                region = REGION_UAE,
                platform = PLATFORM_ANDROID,
                corporationId = user.corporationId
            ))
        }
        saveTokens(loginResponse)
    }

    private fun saveTokens(response: LoginResponse) {
        tokenStorage.accessToken = response.accessToken
        tokenStorage.refreshToken = response.refreshToken
    }
}

open class SessionManagerException(message: String, error: Throwable?) : Exception(message, error) {
    class PhoneException(message: String) : SessionManagerException(message, null)
}