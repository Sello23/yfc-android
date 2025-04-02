package com.yourfitness.common.domain

import com.yourfitness.common.data.dao.ProfileDao
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.data.mappers.toEntity
import com.yourfitness.common.network.CommonRestApi
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val commonRestApi: CommonRestApi,
    private val profileDao: ProfileDao,
) {

    suspend fun fullName() = getProfile().fullName
    suspend fun email() = getProfile().email.orEmpty()
    suspend fun phone() = getProfile().phoneNumber
    suspend fun createdAt() = getProfile().createdAt ?: 0
    suspend fun voucher() = getProfile().voucher.orEmpty()
    suspend fun gender() = getProfile().gender
    suspend fun profileId() = getProfile().id
    suspend fun isPtRole() = getProfile().personalTrainer ?: false
    suspend fun isBookable() = getProfile().isBookable ?: false
    suspend fun accessWorkoutPlans() = getProfile().accessWorkoutPlans ?: false

    suspend fun updateProfile(profile: ProfileEntity) = profileDao.saveProfile(profile)

    suspend fun getProfile(forceUpdate: Boolean = false): ProfileEntity {
        return try {
            if (forceUpdate) loadProfile() else profileDao.readProfile() ?: loadProfile()
        } catch (e: Exception) {
            ProfileEntity.empty
        }
    }

    private suspend fun loadProfile(): ProfileEntity {
        val profile = commonRestApi.profile().toEntity()
        profileDao.saveProfile(profile)
        return profile
    }
}
