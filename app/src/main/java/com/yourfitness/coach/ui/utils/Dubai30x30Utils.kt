package com.yourfitness.coach.ui.utils

import android.os.Parcelable
import androidx.annotation.StringRes
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.ui.features.more.challenges.ChallengeFlow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.network.dto.Challenge
import kotlinx.parcelize.Parcelize

suspend fun Navigator.openDubai30x30Details(
    settingsManager: SettingsManager,
    storage: CommonPreferencesStorage
) {
    if (storage.dubaiStart == null || storage.dubaiEnd == null) {
        settingsManager.getSettings()
    }
    val challenge = Challenge(
        startDate = storage.dubaiStart.toSeconds(),
        endDate = storage.dubaiEnd.toSeconds(),
    )
    navigate(
        Navigation.ChallengeDetails(
            challenge,
            ChallengeFlow.GLOBAL,
            DubaiDetailsData(
                R.string.dubai_30x30_details_title,
                R.string.dubai_30x30_details_description,
                R.string.dubai_30x30_details_rules
            )
        )
    )
}

@Parcelize
data class DubaiDetailsData(
    @StringRes val nameId: Int,
    @StringRes val descriptionId: Int,
    @StringRes val rulesId: Int,
) : Parcelable
