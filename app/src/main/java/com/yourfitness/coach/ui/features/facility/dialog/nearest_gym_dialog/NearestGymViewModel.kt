package com.yourfitness.coach.ui.features.facility.dialog.nearest_gym_dialog

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.FacilityVisitInfo
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NearestGymViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val facilityDao: FacilityDao
) : MviViewModel<Any, Any>() {

    fun sendFacilityVisitInfo(facilityId: String?, userId: String?) {
        viewModelScope.launch {
            try {
                restApi.sendFacilityVisitIngo(FacilityVisitInfo(facilityId, userId))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun writeVisitFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            try {
                val facilities = facilityDao.readVisitedFacility().distinctBy { it.id }
                if (facilities.isEmpty()) {
                    facilityDao.writeFacility(facility.copy(isVisit = true))
                } else {
                    val sameFacility = checkIsWrite(facilities, facility)
                    if (!sameFacility) facilityDao.writeFacility(facility.copy(isVisit = true))
                }
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun checkIsWrite(facilities: List<FacilityEntity>, facility: FacilityEntity): Boolean {
        var sameFacility = false
        facilities.forEach {
            sameFacility = facilities.contains(facility.copy(_id = it._id, isVisit = true, customClasses = null, licensedClasses = null))
            if (sameFacility) return true
        }
        return sameFacility
    }
}
