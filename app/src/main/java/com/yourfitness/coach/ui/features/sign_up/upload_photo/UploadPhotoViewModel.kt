package com.yourfitness.coach.ui.features.sign_up.upload_photo

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.fcm.CloudMessagingManager
import com.yourfitness.coach.domain.progress.points.PointsManager
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.analytics.AnalyticsManager
import com.yourfitness.common.domain.analytics.RegistrationEvents
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UploadPhotoViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val profileManager: ProfileManager,
    private val pointsManager: PointsManager,
    private val cloudMessagingManager: CloudMessagingManager,
    private val analyticsManager: AnalyticsManager,
    private val settingsManager: SettingsManager,
    private val regionSettingsManager: RegionSettingsManager,
    private val navigator: Navigator,
    savedState: SavedStateHandle
): MviViewModel<Any, Any>() {

    val user: User = savedState.get<User>("user") ?: User()

    override fun handleIntent(intent: Any) {
        when (intent) {
            is UploadPhotoIntent.Next -> createAccount(intent.media)
        }
    }

    private fun createAccount(media: Uri) {
        viewModelScope.launch {
            try {
                state.postValue(UploadPhotoState.Loading)
                sessionManager.createAccount(user)
                val mediaId = profileManager.createMedia(media)
                profileManager.updateProfile(user.copy(mediaId = mediaId))
                analyticsManager.trackEvent(RegistrationEvents.success())
                profileManager.loadUserRole()
                regionSettingsManager.getSettings(true)
                settingsManager.getSettings(true)
                cloudMessagingManager.sendToken()
                pointsManager.updateLastPoints()
                state.postValue(UploadPhotoState.Success)
                navigator.navigate(Navigation.EnterCorporateCode(user))
            } catch (error: Exception) {
                state.postValue(UploadPhotoState.Error(error))
                Timber.e(error)
            }
        }
    }
}

open class UploadPhotoState {
    object Loading: UploadPhotoState()
    object Success: UploadPhotoState()
    data class Error(val error: Exception): UploadPhotoState()
}

open class UploadPhotoIntent {
    data class Next(val media: Uri): UploadPhotoIntent()
}