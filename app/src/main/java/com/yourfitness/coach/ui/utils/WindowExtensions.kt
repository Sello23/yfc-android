package com.yourfitness.coach.ui.utils

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

@Suppress("DEPRECATION")
fun Window.transparentStatusAndNavigation(
    systemUiScrim: Int = Color.parseColor("#40000000") // 25% black
) {
    var systemUiVisibility = 0
    // Use a dark scrim by default since light status is API 23+
    var statusBarColor = systemUiScrim
    //  Use a dark scrim by default since light nav bar is API 27+
    var navigationBarColor = systemUiScrim
    val winParams = this.attributes


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        statusBarColor = Color.TRANSPARENT
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        navigationBarColor = Color.TRANSPARENT
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        systemUiVisibility = systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        decorView.systemUiVisibility = systemUiVisibility
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        winParams.flags = winParams.flags or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        winParams.flags = winParams.flags and (
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        ).inv()
        this.statusBarColor = statusBarColor
        this.navigationBarColor = navigationBarColor
    }

    this.attributes = winParams
}