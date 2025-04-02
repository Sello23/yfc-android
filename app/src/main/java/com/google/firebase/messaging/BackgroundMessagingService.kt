package com.google.firebase.messaging

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.firebase.messaging.Constants.MessagePayloadKeys
import com.google.firebase.messaging.Constants.MessageTypes
import java.util.ArrayDeque

@SuppressLint("LogNotTimber", "MissingFirebaseInstanceTokenRefresh")
open class BackgroundMessagingService : FirebaseMessagingService() {

    companion object {
        private const val RECENTLY_RECEIVED_MESSAGE_IDS_MAX_SIZE = 10
        private val RECENTLY_RECEIVED_MESSAGE_IDS = ArrayDeque<String>(RECENTLY_RECEIVED_MESSAGE_IDS_MAX_SIZE)
    }

    override fun handleIntent(intent: Intent) {
        val action = intent.action
        if (ACTION_REMOTE_INTENT == action || ACTION_DIRECT_BOOT_REMOTE_INTENT == action) {
            handleMessageIntent(intent)
        } else if (ACTION_NEW_TOKEN == action) {
            onNewToken(intent.getStringExtra(EXTRA_TOKEN)!!)
        } else {
            Log.d(Constants.TAG, "Unknown intent action: " + intent.action)
        }
    }

    private fun handleMessageIntent(intent: Intent) {
        val messageId = intent.getStringExtra(MessagePayloadKeys.MSGID)
        if (!alreadyReceivedMessage(messageId)) {
            passMessageIntentToSdk(intent)
        }
    }

    private fun passMessageIntentToSdk(intent: Intent) {
        var messageType = intent.getStringExtra(MessagePayloadKeys.MESSAGE_TYPE)
        if (messageType == null) {
            messageType = MessageTypes.MESSAGE
        }
        when (messageType) {
            MessageTypes.MESSAGE -> {
                MessagingAnalytics.logNotificationReceived(intent)
                dispatchMessage(intent)
            }

            MessageTypes.DELETED -> onDeletedMessages()
            MessageTypes.SEND_EVENT -> onMessageSent(intent.getStringExtra(MessagePayloadKeys.MSGID)!!)
            MessageTypes.SEND_ERROR -> onSendError(
                getMessageId(intent)!!,
                SendException(intent.getStringExtra(Constants.IPC_BUNDLE_KEY_SEND_ERROR))
            )

            else -> Log.w(Constants.TAG, "Received message with unknown type: $messageType")
        }
    }

    private fun dispatchMessage(intent: Intent) {
        var data = intent.extras
        if (data == null) {
            data = Bundle()
        }
        data.remove("androidx.content.wakelockid")
        if (NotificationParams.isNotification(data)) {
//            val params = NotificationParams(data)
//            val executor = FcmExecutors.newNetworkIOExecutor()
//            val displayNotification = DisplayNotification(this, params, executor)
//            try {
//                if (displayNotification.handleNotification()) {
//                    return
//                }
//            } finally {
//                executor.shutdown()
//            }
            if (MessagingAnalytics.shouldUploadScionMetrics(intent)) {
                MessagingAnalytics.logNotificationForeground(intent)
            }
        }
        onMessageReceived(RemoteMessage(data))
    }

    private fun alreadyReceivedMessage(messageId: String?): Boolean {
        if (TextUtils.isEmpty(messageId)) {
            return false
        }
        if (RECENTLY_RECEIVED_MESSAGE_IDS.contains(messageId)) {
            if (Log.isLoggable(Constants.TAG, Log.DEBUG)) {
                Log.d(Constants.TAG, "Received duplicate message: $messageId")
            }
            return true
        }
        if (RECENTLY_RECEIVED_MESSAGE_IDS.size >= RECENTLY_RECEIVED_MESSAGE_IDS_MAX_SIZE) {
            RECENTLY_RECEIVED_MESSAGE_IDS.remove()
        }
        RECENTLY_RECEIVED_MESSAGE_IDS.add(messageId)
        return false
    }

    private fun getMessageId(intent: Intent): String? {
        var messageId = intent.getStringExtra(MessagePayloadKeys.MSGID)
        if (messageId == null) {
            messageId = intent.getStringExtra(MessagePayloadKeys.MSGID_SERVER)
        }
        return messageId
    }
}