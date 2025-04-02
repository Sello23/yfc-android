package com.yourfitness.coach.domain

import android.content.Context
import android.net.Uri
import com.yourfitness.coach.YFCApplication
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.session.handleOfflineErrors
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.UpdateProfileRequest
import com.yourfitness.coach.network.dto.toException
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.session.TokenManager
import com.yourfitness.common.domain.validation.Validation
import com.yourfitness.common.domain.validation.Validation.isValidEmail
import com.yourfitness.common.network.dto.Gender
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

const val EMPTY_UUID = "00000000-0000-0000-0000-000000000000"
const val REGION_UAE = "UAE"
const val PLATFORM_ANDROID = "android"

class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val restApi: YFCRestApi,
    private val profileRepository: ProfileRepository,
    private val commonStorage: CommonPreferencesStorage
) {

    suspend fun updateProfile(
        name: String, surname: String, email: String, instagram: String, birthday: Long?, gender: Gender?,
        profile: ProfileEntity
    ) {
        if (email != profile.email) {
            checkEmail(email)
        }
        val request = UpdateProfileRequest(
            email = ifNotTheSame(email, profile.email),
            name = ifNotTheSame(name, profile.name),
            surname = ifNotTheSame(surname, profile.surname),
            mediaId = profile.mediaId,
            instagram = ifNotTheSame(instagram, profile.instagram)?.ifEmpty { null },
            birthday = ifNotTheSame(birthday, profile.birthday),
            gender = ifNotTheSame(gender, profile.gender)?.ordinal,
            phoneNumber = profile.phoneNumber,
            region = REGION_UAE,
            platform = PLATFORM_ANDROID,
            voucher = profile.voucher
        )
        restApi.updateProfile(request)
        profileRepository.getProfile(true)
        refreshToken()
    }

    suspend fun updateProfile(user: User) {
        val profileRequest = UpdateProfileRequest(
            email = user.email,
            name = user.name,
            surname = user.surname,
            mediaId = user.mediaId,
            birthday = user.birthday,
            gender = user.gender.ordinal,
            phoneNumber = Validation.normalizePhone(user.phone),
            region = REGION_UAE,
            platform = PLATFORM_ANDROID
        )
        restApi.updateProfile(profileRequest)
        profileRepository.getProfile(true)
    }

    suspend fun updatePhoto(uri: Uri, profile: ProfileEntity): String {
        val mediaId = createMedia(uri)
        val baseRequest = profile.createBaseProfileRequest()
        val request = baseRequest.copy(mediaId = ifNotTheSame(mediaId, profile.mediaId))
        restApi.updateProfile(request)
        val updatedProfile = profileRepository.getProfile().copy(mediaId = mediaId)
        profileRepository.updateProfile(updatedProfile)
        return mediaId
    }

    suspend fun createMedia(uri: Uri): String {
        val type = context.contentResolver.getType(uri)
        val stream = context.contentResolver.openInputStream(uri)!!
        val requestBody = stream.readBytes().toRequestBody(type?.toMediaType())
        stream.close()
        val body = MultipartBody.Part.createFormData("media", "image.jpg", requestBody)
        return restApi.createMedia(body)
    }

    suspend fun checkEmail(email: String) = handleOfflineErrors {
        try {
            val isValid = isValidEmail(email)
            if (!isValid) {
                throw ProfileManagerException.EmailException("Wrong email address")
            }
            val isAvailable = restApi.checkEmail(email)
            if (!isAvailable) {
                throw ProfileManagerException.EmailException("This email is already used")
            }
        } catch (error: HttpException) {
            throw error.toException()
        }
    }

    suspend fun updateGender(gender: Gender = Gender.FEMALE) {
        val profile = profileRepository.getProfile()
        val baseRequest = profile.createBaseProfileRequest()
        val request = baseRequest.copy(gender = gender.ordinal)
        restApi.updateProfile(request)
        profileRepository.updateProfile(profile.copy(gender = gender))
    }

    private fun ProfileEntity.createBaseProfileRequest(): UpdateProfileRequest {
        return UpdateProfileRequest(
            email = email,
            name = name,
            surname = surname,
            mediaId = mediaId,
            instagram = instagram,
            birthday = birthday,
            gender = gender?.ordinal,
            phoneNumber = phoneNumber,
            region = REGION_UAE,
            platform = PLATFORM_ANDROID,
            voucher = voucher
        )
    }

    suspend fun refreshToken() {
        withContext(Dispatchers.IO) { tokenManager.refreshToken() }
    }

    suspend fun loadUserRole(): Boolean {
        val isPtRole = profileRepository.getProfile(true).personalTrainer ?: false
        commonStorage.isPtRole = isPtRole
        return isPtRole
    }

    suspend fun accessWorkoutPlans(): Boolean {
        val accessWorkoutPlans = profileRepository.getProfile(true).accessWorkoutPlans ?: false
        commonStorage.accessWorkoutPlans = accessWorkoutPlans
        return accessWorkoutPlans
    }

    suspend fun isBookable(): Boolean {
        val isBookable = profileRepository.getProfile(true).isBookable ?: false
        commonStorage.isBookable = isBookable
        return isBookable
    }
}

fun <T> ifNotTheSame(one: T?, other: T?): T? {
    return if (one != null && one != other) one else other
}

open class ProfileManagerException(message: String, error: Throwable?) : Exception(message, error) {
    class EmailException(message: String) : ProfileManagerException(message, null)
}