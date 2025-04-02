package com.yourfitness.coach.domain.subscription

import com.yourfitness.coach.data.dao.SubscriptionDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.network.dto.CreateOneTimeSubscriptionRequest
import com.yourfitness.coach.network.dto.CreateSubscriptionRequest
import com.yourfitness.coach.network.dto.CreateSubscriptionResponse
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.network.dto.Gender
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val profileRepository: ProfileRepository,
    private val subscriptionDao: SubscriptionDao
) {
    private val mutex = Mutex()

    suspend fun hasSubscription(): Boolean = subscriptionDao.read()?.isActive() == true

    private suspend fun canEnterFacility(facility: FacilityEntity): Boolean {
        val gender = profileRepository.gender()
        val femaleOnly = facility.femaleOnly ?: false
        return !(gender == Gender.MALE && femaleOnly)
    }

    suspend fun hasGymAccess(facility: FacilityEntity, classification: Classification): Boolean {
        return classification == Classification.GYM && canEnterFacility(facility) && !hasSubscription()
    }

    suspend fun getSubscription(forceUpdate: Boolean = false): SubscriptionEntity? = mutex.withLock {
        var savedSubscription = subscriptionDao.read()

        if ((savedSubscription?.expiredDate ?: 0L) < today().time.toSeconds() &&
            savedSubscription?.complimentaryAccess == false) {
            subscriptionDao.delete()
            savedSubscription = null
        }

        val needUpdate = forceUpdate || savedSubscription == null ||
                today().time - savedSubscription.lastUpdateTime > MIN_SUBSCRIPTION_UPDATE_INTERVAL
        if (!needUpdate) return savedSubscription
        else {
            try {
                savedSubscription = restApi.getSubscription().toEntity()
            } catch (e: Exception) {
                Timber.e(e)
                subscriptionDao.delete()
                return savedSubscription
            }
            subscriptionDao.saveSubscription(savedSubscription)
        }

        return savedSubscription
    }

    suspend fun createSubscription(price: Long, type: String, voucher: String?): CreateSubscriptionResponse {
        val request = CreateSubscriptionRequest(price, type, voucher)
        return restApi.createSubscription(request)
    }

    suspend fun createOneTimeSubscription(
        duration: Int?,
        price: Long,
        type: String,
        voucher: String?
    ): CreateSubscriptionResponse {
        val request = CreateOneTimeSubscriptionRequest(duration, duration != null, price, type, voucher)
        return restApi.createOneTimeSubscription(request)
    }

    suspend fun cancelSubscription() {
        restApi.cancelSubscription()
    }

    suspend fun resubscribeSubscription() {
        restApi.resubscribeSubscription()
    }

    companion object {
        const val MIN_SUBSCRIPTION_UPDATE_INTERVAL = 10 * 60 * 1000
    }
}

fun SubscriptionEntity.isActive(): Boolean {
    val isExpired = expiredDate != null && expiredDate > Date().time / 1000
    return isExpired || complimentaryAccess
}