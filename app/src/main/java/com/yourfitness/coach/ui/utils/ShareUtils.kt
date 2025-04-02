package com.yourfitness.coach.ui.utils

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.yourfitness.coach.BuildConfig
import com.yourfitness.coach.R
import com.yourfitness.common.domain.analytics.CrashlyticsManager
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun Fragment.shareReferralCode(referralCode: String?) {
    if (referralCode.isNullOrBlank()) return
    try {
        val uri = getImageUri()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.my_referral_screen_your_referral_code_text, referralCode)
            )
            type = "image/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    } catch (e: Exception) {
        Timber.e(e)
        CrashlyticsManager.trackException(e)
    }
}

private fun Fragment.getImageUri(): Uri? {
    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image_gift_intent)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return getBitmapUri(bitmap)
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val resolver = requireActivity().contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    val fos: OutputStream? = imageUri?.let { resolver.openOutputStream(it) }
    fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    contentValues.clear()
    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)

    return if (imageUri != null) {
        resolver.update(imageUri, contentValues, null, null)
         imageUri
    } else {
        getBitmapUri(bitmap)
    }
}

private fun Fragment.getBitmapUri(bitmap: Bitmap?): Uri? {
    val cachePath = File(requireActivity().cacheDir, "images")
    cachePath.mkdirs()
    val imageFile = File(cachePath, "image.png")
    val imageUri = FileProvider.getUriForFile(
        requireContext(),
        "${BuildConfig.APPLICATION_ID}.provider",
        imageFile
    )
    val outputStream = FileOutputStream(imageFile)
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
    return imageUri
}