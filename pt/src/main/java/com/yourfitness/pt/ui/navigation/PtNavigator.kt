package com.yourfitness.pt.ui.navigation

import androidx.lifecycle.MutableLiveData
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.network.dto.PtClientDto
import kotlinx.coroutines.delay
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PtNavigator @Inject constructor() {

    val navigation = MutableLiveData<PtNavigation?>()

    fun navigate(node: PtNavigation) {
        navigation.postValue(node)
    }

    suspend fun navigateDelayed(node: PtNavigation) {
        delay(500)
        navigate(node)
    }
}

open class PtNavigation {
    object MyTrainersList : PtNavigation()
    data class Details(val ptId: String) : PtNavigation()
    data class BuySessions(val ptId: String, val popScreen: Boolean = false) : PtNavigation()
    data class PaymentOptions(
        val sessionsData: BuyOptionData,
        val ptId: String,
        val popAmount: Int?
    ) : PtNavigation()
    data class PaymentError(val popAmount: Int = 2) : PtNavigation()

    data class PaymentSuccess(val ptId: String, val sessionsAmount: Int, val ptName: String) : PtNavigation()
    data class BackToPtList(val popAmount: Int = 3) : PtNavigation()
    data class Calendar(val ptId: String, val actionsEnabled: Boolean = true) : PtNavigation()
    data class CalendarPt(val date: Long) : PtNavigation()
    data class BookTimeSlot(
        val ptId: String,
        val ptName: String,
        val sessionDate: Date,
        val facilities: List<FacilityInfo>
    ) : PtNavigation()
    object BookingError : PtNavigation()
    object BookingSuccess : PtNavigation()

    data class ConfirmSession(val sessionId: String) : PtNavigation()
    data class ProcessConfirmSession(val sessionId: String) : PtNavigation()
    object TrainingCalendar : PtNavigation()
    data class TrainingCalendarPt(val sessionId: String? = null) : PtNavigation()
    data class StartCalendarActionFlow(val sessionId: String, val flow: Int) : PtNavigation()
    data class ConfirmCalendarActionFlow(
        val sessionId: String,
        val flow: Int,
        val needPop: Boolean = true
    ) : PtNavigation()
    data class DeleteSlotListFlow(
        val sessionIds: ArrayList<String>,
        val flow: Int,
        val needPop: Boolean = true
    ) : PtNavigation()
    data class ResultCalendarActionFlow(val flow: Int) : PtNavigation()
    object ClientsList : PtNavigation()
    object InductionsList : PtNavigation()
    data class ClientDetails(val client: PtClientDto) : PtNavigation()
    data class CompleteInduction(val client: InductionInfo) : PtNavigation()
    data class CompleteInduction2(val client: String) : PtNavigation()
    data class ConfirmCompleteInduction(val client: InductionInfo, val note: String) : PtNavigation()
    data class ConfirmCompleteInduction2(val client: String, val note: String) : PtNavigation()
    object ConfirmInductionSuccess : PtNavigation()
    object BlocTimeSlot : PtNavigation()
    data class SelectDate(val selectedDate: Long) : PtNavigation()
    object SelectWeeksNumber : PtNavigation()
    object PopScreen : PtNavigation()
    object TimeSlotsBlocked : PtNavigation()
    data class ResolveConflicts(val conflictData: SessionEntity) : PtNavigation()
    object BlockSlotsList : PtNavigation()

    object ComingSoon : PtNavigation()
}
