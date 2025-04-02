package com.yourfitness.coach.ui.features.more.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.SettingsGlobal
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.RegionSettingsEntity
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.network.dto.SettingsRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val commonRestApi: CommonRestApi,
    private val settingsManager: SettingsManager,
    private val regionSettingsManager: RegionSettingsManager,
    val navigator: Navigator,
) : MviViewModel<StoryIntent, Any>() {

    private val _bonuses: MutableLiveData<SettingsEntity> = MutableLiveData()
    val bonuses: LiveData<SettingsEntity> get() = _bonuses

    private val _packages: MutableLiveData<RegionSettingsEntity> = MutableLiveData()
    val packages: LiveData<RegionSettingsEntity> get() = _packages

    override fun handleIntent(intent: StoryIntent) {
        when (intent) {
            is StoryIntent.FAQ -> {
                navigator.navigate(Navigation.FAQ)
            }
        }
    }

    fun getSettingsRegion() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsRegion = regionSettingsManager.getSettings() ?: RegionSettingsEntity.empty
                _packages.postValue(settingsRegion)
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun getSettingsGlobal() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsGlobal = settingsManager.getSettings() ?: SettingsEntity.empty
                _bonuses.postValue(settingsGlobal)
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

open class StoryIntent {
    object FAQ : StoryIntent()
}