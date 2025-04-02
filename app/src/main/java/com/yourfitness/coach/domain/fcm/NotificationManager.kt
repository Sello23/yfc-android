package com.yourfitness.coach.domain.fcm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.yourfitness.coach.MainActivity
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.ClassEntity
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.pt.domain.pt.PtRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.yourfitness.comunity.R as community
import com.yourfitness.pt.R as pt
import com.yourfitness.shop.R as shop

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ptRepository: PtRepository
) {
    private var facilities: List<FacilityEntity> = emptyList()
    private val scope = CoroutineScope(Dispatchers.Main)

    fun sendNotification(title: String, body: String, data: Map<String, String>) {
        scope.launch {
//            loadData() // TODO temporary removed Studios
            displayNotification(title, body, data)
        }
    }

    private suspend fun loadData() {
//        try { // TODO temporary removed Studios
//            facilities = restApi.facilities().facilities?.map { it.toEntity() } ?: emptyList()
//        } catch (error: Exception) {
//            Timber.e(error)
//        }
    }

    @SuppressLint("MissingPermission")
    private fun displayNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context, context.getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_yfc)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(createAction(data))
            .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(body))
            .build()
        notificationManager.notify(0, notification)
    }

    private fun createAction(data: Map<String, String>): PendingIntent {
        return when (data["type"]) {
            "updateSchedule", "cancelSchedule" -> {
                val classId = data["classID"]
                val facilityId = data["facilityID"]
                val facility = facilities.find { it.id == facilityId } ?: FacilityEntity()
                val bookedClass = facility.customClasses?.find { it.id == classId } ?: ClassEntity()
                val args = bundleOf(
                    "class_id" to classId,
                    "class_name" to bookedClass.name,
                    "facility" to facility,
                    "is_rebook" to false,
                    "rebook_class_id" to null
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_class_calendar, args)
                    .setComponentName(MainActivity::class.java)
                    .createPendingIntent()
            }
            "failedSubscriptionPayment", "corporationDeactivationWithComplimentarySubscription" -> {
                val args = bundleOf("flow" to PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE)
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_subscription, args)
                    .createPendingIntent()
            }
            "corporationActivationWithComplimentarySubscription", "newCorporationComplimentarySubscription",
            "newCorporationNewRateSubscription" -> {
                val args = bundleOf(
                    "classification" to Classification.GYM,
                    "filters" to Filters(),
                    "notification" to true
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_map, args)
                    .createPendingIntent()
            }
            "manualCreditAddition" -> {
                val args = bundleOf(
                    "classification" to Classification.STUDIO,
                    "filters" to Filters(),
                    "notification" to true
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_map, args)
                    .createPendingIntent()
            }
            "orderProductDelivered", "orderProductNotAvailable", "orderProductRefunded", "orderRefunded", "voucherRefunded" -> {
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .addDestination(R.id.fragment_progress)
                    .addDestination(shop.id.fragment_orders_history)
                    .createPendingIntent()
            }
            "personalTrainerSessionRequested" -> {
                val args = bundleOf(
                    "session_id" to data["personalTrainerSessionID"],
                    "my_pt" to true,
                    "force" to "1"
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setArguments(args)
                    .setDestination(R.id.fragment_dashboard)
                    .addDestination(R.id.fragment_pt_role_training_calendar)
                    .createPendingIntent()
            }
            "personalTrainerSessionApproved" -> {
                val args = bundleOf(
                    "session_id" to data["personalTrainerSessionID"],
                    "my_pt" to true
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setArguments(args)
                    .setDestination(R.id.fragment_progress)
                    .addDestination(pt.id.fragment_training_calendar)
                    .setComponentName(MainActivity::class.java)
                    .createPendingIntent()
            }
            "personalTrainerSessionDeclined", "completedPersonalTrainerSessionCanceled" -> {
                scope.launch(Dispatchers.IO) {
                    ptRepository.downloadPtBalanceList()
                }
                val args = bundleOf(
                    "pt_id" to data["personalTrainerID"],
                    "my_pt" to true
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_progress)
                    .addDestination(pt.id.fragment_calendar, args)
                    .setComponentName(MainActivity::class.java)
                    .createPendingIntent()
            }
            "friendRequest" -> {
                val args = bundleOf(
                    "tab_position" to 1,
                )
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_progress)
                    .addDestination(community.id.fragment_friends, args)
                    .setComponentName(MainActivity::class.java)
                    .createPendingIntent()
            }
            else -> {
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.fragment_splash_screen)
                    .setComponentName(MainActivity::class.java)
                    .createPendingIntent()
            }
        }
    }
}