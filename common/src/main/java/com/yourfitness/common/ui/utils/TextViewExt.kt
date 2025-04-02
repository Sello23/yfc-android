package com.yourfitness.common.ui.utils

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView

fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun MaterialTextView.applyTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)
