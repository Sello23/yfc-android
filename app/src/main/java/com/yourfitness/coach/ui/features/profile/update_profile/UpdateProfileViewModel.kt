package com.yourfitness.coach.ui.features.profile.update_profile

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.ProfileManagerException
import com.yourfitness.coach.ui.features.profile.profile.ProfileState
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val profileRepository: ProfileRepository
) : MviViewModel<Any, Any>() {

    var profile: ProfileEntity = ProfileEntity.empty

    init {
        fetchData()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is UpdateProfileIntent.Save -> updateProfile(intent)
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                state.postValue(UpdateProfileState.Loading)
                profile = profileRepository.getProfile()
                state.postValue(UpdateProfileState.Success(profile))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(UpdateProfileState.Error(error))
            }
        }
    }

    private fun updateProfile(intent: UpdateProfileIntent.Save) {
        viewModelScope.launch {
            try {
                state.postValue(UpdateProfileState.Loading)
                profileManager.updateProfile(intent.name, intent.surname, intent.email, intent.instagram, intent.birthday, intent.gender, profile)
                state.postValue(UpdateProfileState.Updated)
            } catch (error: ProfileManagerException.EmailException) {
                Timber.e(error)
                state.postValue(UpdateProfileState.EmailError(error))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(UpdateProfileState.Error(error))
            }
        }
    }
}

open class UpdateProfileState {
    object Loading : UpdateProfileIntent()
    object Updated : UpdateProfileIntent()
    data class Success(val profile: ProfileEntity) : ProfileState()
    data class Error(val error: Exception) : UpdateProfileIntent()
    data class EmailError(val error: Exception) : UpdateProfileIntent()
}

open class UpdateProfileIntent {
    data class Save(
        val name: String,
        val surname: String,
        val email: String,
        val instagram: String,
        val birthday: Long?,
        val gender: Gender?
    ) : UpdateProfileIntent()
}
