package com.yourfitness.common.domain.analytics

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.todayZoned
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

data class AnalyticsEvent(
    val name: String,
    val params: Bundle = Bundle()
)

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val logger by lazy { AppEventsLogger.newLogger(context) }
    private val fbLogger: FirebaseAnalytics by lazy { Firebase.analytics }

    fun trackEvent(event: AnalyticsEvent, trackFb: Boolean = false) {
        scope.launch {
            val profile = profileRepository.getProfile()
            val username = profile.fullName
            event.params.putString("user_name", username)
            event.params.putString("user_id", profile.id)
            event.params.putString("platform", "Android ${Build.VERSION.RELEASE}")
            logger.logEvent(event.name, event.params)

            if (trackFb) fbLogger.logEvent(event.name) { param("user_id", profile.id) }
        }
    }
}

@Singleton
class SpoofAnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger by lazy { AppEventsLogger.newLogger(context) }

    fun trackEvent(event: AnalyticsEvent) {
        scope.launch {
            logger.logEvent(event.name, event.params)

        }
    }
}

sealed class RegistrationEvents {
    companion object {
        fun success(): AnalyticsEvent {
            return AnalyticsEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION)
        }
    }
}

sealed class SubscriptionEvents {
    companion object {
        fun payed(amount: Long?, currency: String?): AnalyticsEvent {
            val bundle = Bundle()
            bundle.putLong("value", amount ?: -1)
            bundle.putString("currency", currency)
            return AnalyticsEvent(AppEventsConstants.EVENT_NAME_SUBSCRIBE, bundle)
        }
    }
}

sealed class ShopEvents {
    companion object {
        fun open(): AnalyticsEvent {
            return AnalyticsEvent("ShopOpen")
        }
    }
}

sealed class SpoofCheckEvents {
    companion object {
        fun phoneMissed(
            url: String,
            headers: String,
            phone: String?,
            isDbEmpty: Boolean,
            isJwt: Boolean = false,
        ): AnalyticsEvent {
            val eventName = if (isJwt) "PhoneMissedJwt" else "PhoneMissedDb"
            val bundle = Bundle()
            bundle.putString("url", url)
            bundle.putString("headers", headers)
            bundle.putString("phone", phone)
            bundle.putBoolean("dbEmpty", isDbEmpty)
            bundle.putString("trackedAt", todayZoned(ZoneOffset.UTC).toString())
            bundle.putString("platform", "Android ${Build.VERSION.RELEASE}")
            return AnalyticsEvent(eventName, bundle)
        }
    }
}

class CrashlyticsManager {
    companion object {
        @JvmStatic
        fun trackException(error: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(error)
        }
    }
}
