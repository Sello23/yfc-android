package com.yourfitness.common.ui.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

fun TextView.setSpannableText(spannable: SpannableString, withLinks: Boolean = false) {
    text = spannable
    if (withLinks) {
        movementMethod = LinkMovementMethod.getInstance()
    }
}

fun Spannable.setCustomFont(context: Context, startIndex: Int, endIndex: Int, @FontRes font: Int) {
    if (startIndex >= 0 && endIndex >= 0) {
        val typeface = ResourcesCompat.getFont(context, font) ?: return
        setSpan(FontSpan(typeface), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun Spannable.setUnderline(startIndex: Int, endIndex: Int) {
    if (startIndex >= 0 && endIndex >= 0) {
        setSpan(UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun Spannable.setColor(color: Int, startIndex: Int, endIndex: Int) {
    if (startIndex >= 0 && endIndex >= 0) {
        setSpan(
            ForegroundColorSpan(color),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun Spannable.setSize(sizePx: Int, startIndex: Int, endIndex: Int) {
    if (startIndex >= 0 && endIndex >= 0) {
        setSpan(
            AbsoluteSizeSpan(sizePx),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun Spannable.setOnClickListener(
    linkColor: Int,
    underlineClickableText: Boolean,
    startIndex: Int,
    endIndex: Int,
    onClick: () -> Unit
) {
    if (startIndex >= 0 && endIndex >= 0) {
        setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick.invoke()
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = linkColor
                    ds.isUnderlineText = underlineClickableText
                }
            },
            startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    // should be add textView.movementMethod = LinkMovementMethod.getInstance()
}

open class FontSpan(private val font: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, font)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, font)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = tf
    }
}
