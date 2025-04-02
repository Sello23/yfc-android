package com.yourfitness.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import timber.log.Timber


fun Context.dialPhoneNumber(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNumber")
    startSafe(intent)
}

fun Context.emailTo(email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$email") // only email apps should handle this
        putExtra(Intent.EXTRA_EMAIL, arrayListOf(email))
    }
    startSafe(intent)
}

fun Context.openUserInstagram(instagramUrl: String) {
    val instagramUser = instagramUrl.split("/").last()
    val uri = Uri.parse("http://instagram.com/_u/$instagramUser")
    val instagram = Intent(Intent.ACTION_VIEW, uri)
    instagram.setPackage("com.instagram.android")

    try {
        startSafe(instagram)
    } catch (e: ActivityNotFoundException) {
        startSafe(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://instagram.com/$instagramUser")
            )
        )
    }
}

private fun Context.startSafe(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}