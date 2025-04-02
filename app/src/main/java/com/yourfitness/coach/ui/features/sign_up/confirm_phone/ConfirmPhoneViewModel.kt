package com.yourfitness.coach.ui.features.sign_up.confirm_phone

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.fcm.CloudMessagingManager
import com.yourfitness.coach.domain.progress.points.PointsManager
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.dto.ServerException
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfirmPhoneViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val pointsManager: PointsManager,
    private val profileManager: ProfileManager,
    private val cloudMessagingManager: CloudMessagingManager,
    private val navigator: Navigator,
    private val subscriptionManager: SubscriptionManager,
    private val settingsManager: SettingsManager,
    private val regionSettingsManager: RegionSettingsManager,
    savedState: SavedStateHandle
) : MviViewModel<ConfirmPhoneIntent, ConfirmPhoneState>() {

    private val user: User = savedState.get<User>("user") ?: User()
    val flow: Flow = savedState.get<Flow>("flow") ?: Flow.SIGN_UP

    var activeOtp: OtpSender = OtpSender.SMS
        private set
    private val _resendTimer: MutableLiveData<Long?> = MutableLiveData()
    val resendTimer: LiveData<Long?> get() = _resendTimer
    private var countdownTimer: CountDownTimer? = null

    init {
        processCodeSent(true)
    }

    override fun handleIntent(intent: ConfirmPhoneIntent) {
        when (intent) {
            is ConfirmPhoneIntent.SubmitCode -> when (flow) {
                Flow.SIGN_UP -> confirmCode(intent.code)
                Flow.SIGN_IN -> login(intent.code)
            }
            is ConfirmPhoneIntent.ResendCode -> resendCode()
            is ConfirmPhoneIntent.ResendCodeAnotherOption -> updateSenderAndResendCode()
            is ConfirmPhoneIntent.OtpSenderUpdated -> activeOtp = intent.sender
        }
    }

    private fun confirmCode(code: String) {
        viewModelScope.launch {
            try {
                state.postValue(ConfirmPhoneState.Loading)
                sessionManager.confirmCode(code, user.otpId, user.phone)
                state.postValue(ConfirmPhoneState.Success)
                navigator.navigate(Navigation.SelectBirthday(user))
            } catch (error: ServerException) {
                state.postValue(ConfirmPhoneState.SubmitError(error))
                Timber.e(error)
            } catch (error: Exception) {
                state.postValue(ConfirmPhoneState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun login(code: String) {
        viewModelScope.launch {
            try {
                state.postValue(ConfirmPhoneState.Loading)
                sessionManager.login(user.phone, code, user.otpId)
                subscriptionManager.getSubscription(true)
                regionSettingsManager.getSettings(true)
                val isPtRole = profileManager.loadUserRole()
                val isBookable = profileManager.isBookable()
                val accessWorkoutPlans = profileManager.accessWorkoutPlans()
                settingsManager.getSettings(true)
                cloudMessagingManager.sendToken()
                pointsManager.updateLastPoints()
                state.postValue(ConfirmPhoneState.Success)
                navigator.navigate(Navigation.Progress(isPtRole,isBookable,accessWorkoutPlans))
            } catch (error: ServerException) {
                state.postValue(ConfirmPhoneState.SubmitError(error))
                Timber.e(error)
            } catch (error: Exception) {
                state.postValue(ConfirmPhoneState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun updateSenderAndResendCode() {
        activeOtp = if (activeOtp == OtpSender.SMS) OtpSender.WHATSAPP else OtpSender.SMS
        stopTimer()
        resendCode()
    }

    private fun resendCode() {
        viewModelScope.launch {
            try {
                state.postValue(ConfirmPhoneState.Loading)
                sessionManager.resendCode(user.otpId, user.phone, activeOtp.value)
                processCodeSent()
            } catch (error: ServerException) {
                state.postValue(ConfirmPhoneState.ResendError(error))
                Timber.e(error)
            } catch (error: Exception) {
                state.postValue(ConfirmPhoneState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun processCodeSent(init: Boolean = false) {
        state.postValue(
            ConfirmPhoneState.OtpSenderUpdated(
                activeOtp,
                true,
                if (init) null else user.phone
            )
        )
        startResendCodeTimer()
    }

    private fun startResendCodeTimer() {
        countdownTimer = object: CountDownTimer(59000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _resendTimer.postValue(millisUntilFinished.toSeconds())
            }
            override fun onFinish() {
                _resendTimer.postValue(null)
                state.postValue(ConfirmPhoneState.OtpSenderUpdated(activeOtp, false))
            }
        }
        countdownTimer?.start()
    }

    private fun stopTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        _resendTimer.postValue(null)
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}

open class ConfirmPhoneIntent {
    data class SubmitCode(val code: String): ConfirmPhoneIntent()
    object ResendCode: ConfirmPhoneIntent()
    object ResendCodeAnotherOption: ConfirmPhoneIntent()
    data class OtpSenderUpdated(val sender: OtpSender): ConfirmPhoneIntent()
}

open class ConfirmPhoneState {
    object Loading: ConfirmPhoneState()
    object Success: ConfirmPhoneState()
    data class Error(val error: Exception) : ConfirmPhoneState()
    data class SubmitError(val error: Exception) : ConfirmPhoneState()
    data class ResendError(val error: Exception) : ConfirmPhoneState()
    data class OtpSenderUpdated(
        val sender: OtpSender,
        val timerActive: Boolean,
        val phone: String? = null
    ) : ConfirmPhoneState()
}

enum class OtpSender(val value: String) {
    SMS("sms"),
    WHATSAPP("whatsapp")
}