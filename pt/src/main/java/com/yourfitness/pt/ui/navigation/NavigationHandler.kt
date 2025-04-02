package com.yourfitness.pt.ui.navigation

import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.values.ACTION_FLOW
import com.yourfitness.pt.domain.values.CLIENT
import com.yourfitness.pt.domain.values.CONFLICTS
import com.yourfitness.pt.domain.values.INDUCTION
import com.yourfitness.pt.domain.values.NOTE
import com.yourfitness.pt.domain.values.POP_AMOUNT
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.domain.values.PT_NAME
import com.yourfitness.pt.domain.values.SESSIONS_AMOUNT
import com.yourfitness.pt.domain.values.SESSIONS_PACKAGE
import com.yourfitness.pt.domain.values.SESSION_DATE
import com.yourfitness.pt.domain.values.SESSION_ID
import com.yourfitness.pt.domain.values.SESSION_ID_LIST
import com.yourfitness.pt.domain.values.SESSION_LIST
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class PtNavigationHandler constructor(
    private val navController: NavController,
    private val navigator: PtNavigator
) {

    fun observeNavigation(owner: LifecycleOwner) {
        navigator.navigation.observe(owner) {
            if (it != null) {
                navigator.navigation.value = null
                navigate(it)
            }
        }
    }

    private fun navigate(node: PtNavigation) {
        when (node) {
            is PtNavigation.MyTrainersList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_pt_list/true/@null".toUri())
                    .build()

                navController.navigate(request)
            }
            is PtNavigation.Details -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_pt_details/${node.ptId}".toUri())
                    .build()

                navController.navigate(request)
            }
            is PtNavigation.BuySessions -> {
                if (node.popScreen) {
                    navController.popBackStack()
                }
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_buy_sessions/${node.ptId}/4".toUri())
                    .build()

                navController.navigate(request)
            }
            is PtNavigation.PaymentOptions -> {
                val args = bundleOf(
                    SESSIONS_PACKAGE to node.sessionsData,
                    PT_ID to node.ptId,
                    POP_AMOUNT to node.popAmount
                )
                navController.navigate(R.id.fragment_payment_options, args)
            }
            is PtNavigation.PaymentError -> {
                val args = bundleOf(POP_AMOUNT to node.popAmount)
                navController.navigate(R.id.dialog_payment_error, args)
            }
            is PtNavigation.PaymentSuccess -> {
                navController.popBackStack()
                navController.popBackStack()

                openUserCalendarDeepLink(node.ptId)

                runBlocking {
                    delay(300L)
                    val args = bundleOf(
                        SESSIONS_AMOUNT to node.sessionsAmount,
                        PT_NAME to node.ptName
                    )
                    navController.navigate(R.id.dialog_pt_payment_success, args)
                }
            }
            is PtNavigation.BackToPtList -> {
                for (i in 0 until node.popAmount) {
                    navController.popBackStack()
                }
            }
            is PtNavigation.Calendar -> {
                openUserCalendarDeepLink(node.ptId, node.actionsEnabled)
            }
            is PtNavigation.CalendarPt -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_calendar_pt/${node.date}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.BookTimeSlot -> {
                val args = bundleOf(
                    PT_ID to node.ptId,
                    PT_NAME to node.ptName,
                    SESSION_DATE to node.sessionDate,
                    SESSION_LIST to node.facilities
                )
                navController.navigate(R.id.dialog_book_time_slot, args)
            }
            is PtNavigation.BookingError -> {
                navController.popBackStack()
                navController.navigate(R.id.dialog_booking_error)
            }
            is PtNavigation.BookingSuccess -> {
                navController.popBackStack()
                navController.navigate(R.id.dialog_booking_success)
            }
            is PtNavigation.ConfirmSession -> {
                val args = bundleOf(SESSION_ID to node.sessionId)
                navController.navigate(R.id.dialog_confirm_session, args)
            }
            is PtNavigation.ProcessConfirmSession -> {
                navController.popBackStack()
                val args = bundleOf(SESSION_ID to node.sessionId)
                navController.navigate(R.id.dialog_process_confirm_session, args)
            }
            is PtNavigation.TrainingCalendar -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_training_calendar".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.TrainingCalendarPt -> {
                val params = if (node.sessionId == null) "none" else "${node.sessionId}"
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_training_calendar_pt/$params/false".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.StartCalendarActionFlow -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_user_profile_action/${node.sessionId}/${node.flow}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ClientDetails -> {
                val args = bundleOf(CLIENT to node.client)
                navController.navigate(R.id.dialog_user_profile_client, args)
            }
            is PtNavigation.CompleteInduction -> {
                val args = bundleOf(CLIENT to node.client)
                navController.navigate(R.id.dialog_user_profile_induction, args)
            }
            is PtNavigation.CompleteInduction2 -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_user_profile_induction/${node.client}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ConfirmCompleteInduction -> {
                navController.popBackStack()
                val args = bundleOf(INDUCTION to node.client, NOTE to node.note)
                navController.navigate(R.id.dialog_confirm_induction_complete, args)
            }
            is PtNavigation.ConfirmCompleteInduction2 -> {
                navController.popBackStack()
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_confirm_induction_complete/${node.client}/${node.note.ifBlank { null }}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ConfirmInductionSuccess -> {
                navController.popBackStack()
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_confirm_induction_success".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.BlocTimeSlot -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_block_time_slot".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.SelectDate -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_select_date/${node.selectedDate}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.SelectWeeksNumber -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_select_weeks_number".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ConfirmCalendarActionFlow -> {
                if (node.needPop) {
                    navController.popBackStack()
                }
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_user_profile_confirm/${node.sessionId}/${node.flow}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.DeleteSlotListFlow -> {
                if (node.needPop) {
                    navController.popBackStack()
                }
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_block_slots_list, false)
                    .build()
                val args = bundleOf(
                    SESSION_ID_LIST to node.sessionIds,
                    ACTION_FLOW to node.flow
                )
                navController.navigate(R.id.dialog_user_profile_confirm, args, options)
            }
            is PtNavigation.ResultCalendarActionFlow -> {
                navController.popBackStack()
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_user_profile_result/${node.flow}".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ClientsList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_clients_list".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.InductionsList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_inductions_list".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.BlockSlotsList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_block_slots_list".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.TimeSlotsBlocked -> {
                navController.popBackStack()
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_time_slot_blocked".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.ResolveConflicts -> {
                val args = bundleOf(CONFLICTS to node.conflictData)
                navController.navigate(R.id.dialog_resolve_conflicts, args)
            }
            is PtNavigation.ComingSoon -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/dialog_pt_coming_soon".toUri())
                    .build()
                navController.navigate(request)
            }
            is PtNavigation.PopScreen -> navController.popBackStack()
            else -> Toast.makeText(
                navController.context,
                "Navigation destination not implemented",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openUserCalendarDeepLink(ptId: String, actionsEnabled: Boolean = true) {
        val request = NavDeepLinkRequest.Builder
            .fromUri("android-app://com.yourfitness.pt/fragment_calendar/$ptId/$actionsEnabled".toUri())
            .build()
        navController.navigate(request)
    }
}
