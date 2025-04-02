package com.yourfitness.coach.domain.fcm

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.BackgroundMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yourfitness.coach.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CloudMessagingService : BackgroundMessagingService() {

    @Inject lateinit var cloudMessagingManager: CloudMessagingManager
    @Inject lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onNewToken(token: String) {
        Timber.d("onNewToken: $token")
        sendToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("onMessageReceived: $message")
        val title = message.notification?.title
        val body = message.notification?.body
        val data = message.data
        if (title != null && body != null) {
            notificationManager.sendNotification(title, body, data)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendToken(token: String) {
        GlobalScope.launch {
            try {
                cloudMessagingManager.sendToken(token)
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun createChannel() {
        val notificationManager = NotificationManagerCompat.from(this)
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_HIGH).setName(channelName).build()
        notificationManager.createNotificationChannel(channel)
    }
}