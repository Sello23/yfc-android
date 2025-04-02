package com.yourfitness.common.domain.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yourfitness.common.data.TokenStorage
import com.yourfitness.common.network.YFCAuthApi
import com.yourfitness.common.network.dto.TokenData
import com.yourfitness.common.ui.navigation.CommonNavigator
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val restApi: YFCAuthApi
) {
    private val _logoutState = MutableLiveData<Boolean>()
    val logoutState: LiveData<Boolean> = _logoutState

    fun refreshToken(): String? = synchronized(this) {
        _logoutState.postValue(false)
        val tokens = TokenData(tokenStorage.accessToken, tokenStorage.refreshToken)
        Timber.d("Start refreshing tokens: ${tokens.accessToken}, ${tokens.refreshToken}, $this")
        val tokenResponse = restApi.refreshToken(tokens).execute().body()
        if (tokenResponse != null) {
            tokenStorage.accessToken = tokenResponse.accessToken
            tokenStorage.refreshToken = tokenResponse.refreshToken
        } else {
            Timber.w("Failed to refresh token")
            logout()
        }
        Timber.d("End refreshing tokens ${tokenResponse?.accessToken}, ${tokenResponse?.refreshToken}, $this")
        return tokenResponse?.accessToken
    }

    fun logout() {
        tokenStorage.clearTokens()
        _logoutState.postValue(true)
    }

    fun reset() {
        _logoutState.postValue(false)
    }
}