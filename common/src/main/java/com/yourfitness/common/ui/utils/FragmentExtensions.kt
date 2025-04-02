package com.yourfitness.common.ui.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import timber.log.Timber

fun Fragment.hideKeyboard() {
    val imeManager =
        requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = requireActivity().currentFocus
    if (view == null) {
        view = View(activity)
    }
    imeManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.openGetHelp() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=971800932277")
        })
    } catch (e: Exception) {
        Timber.e(e)
    }
}
