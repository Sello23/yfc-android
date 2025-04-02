package com.yourfitness.coach.ui.features.sign_up.enter_phone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mukesh.countrypicker.Country
import com.mukesh.countrypicker.CountryPicker
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.domain.session.SessionManagerException
import com.yourfitness.coach.ui.features.sign_up.confirm_phone.OtpSender
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterPhoneViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<EnterPhoneIntent, Any>() {

    private val user: User = savedState.get<User>("user") ?: User()
    val country: Country = CountryPicker.Builder().build().getCountryByISO("AE")
    val flow: Flow = savedState.get<Flow>("flow") ?: Flow.SIGN_UP

    override fun handleIntent(intent: EnterPhoneIntent) {
        when (intent) {
            is EnterPhoneIntent.Next -> next(intent.region,intent.countryCode + intent.phone)
        }
    }

    private suspend fun checkPhoneAndCreateOtp(region: String, phone: String) {
        try {
            state.postValue(EnterPhoneState.Loading)
            sessionManager.checkPhone(phone, flow)
            createOtpCode(region, phone)
        } catch (error: SessionManagerException.PhoneException) {
            state.postValue(EnterPhoneState.PhoneError(error))
            Timber.e(error)
        } catch (error: Exception) {
            state.postValue(EnterPhoneState.Error(error))
            Timber.e(error)
        }
    }

    private suspend fun createOtpCode(region: String, phone: String) {
        try {
            val otpId = sessionManager.createOtpCode(phone, OtpSender.SMS.value)
            val user = user.copy(otpId = otpId, region = region, phone = phone)
            state.postValue(EnterPhoneState.Initial)
            navigator.navigate(Navigation.ConfirmPhone(user, flow))
        } catch (error: Exception) {
            state.postValue(EnterPhoneState.Error(error))
            Timber.e(error)
        }
    }

    private fun next(region: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            checkPhoneAndCreateOtp(region, phone)
        }
    }
}

open class EnterPhoneIntent {
    data class Next(val countryCode: String, val phone: String, val region: String): EnterPhoneIntent()
}

open class EnterPhoneState {
    object Initial: EnterPhoneState()
    object Loading : EnterPhoneState()
    data class Error(val error: Throwable): EnterPhoneState()
    data class PhoneError(val error: Throwable): EnterPhoneState()
}