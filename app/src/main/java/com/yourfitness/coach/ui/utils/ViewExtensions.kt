package com.yourfitness.coach.ui.utils

import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.yourfitness.coach.R

private class NoUnderlineSpan(url: String) : URLSpan(url) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}

fun TextView.removeLinksUnderline() {
    val text = this.text as Spannable
    val spans = text.getSpans(0, text.length, URLSpan::class.java)
    for (span in spans) {
        val start = text.getSpanStart(span)
        val end = text.getSpanEnd(span)
        text.removeSpan(span)
        text.setSpan(NoUnderlineSpan(span.url), start, end, 0)
    }
}

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.setupMapInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, _ ->
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        }

        this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        }

        WindowInsetsCompat.CONSUMED
    }
}

fun ImageView.startBackgroundAnimation() {
    val drawable = drawable
    if (drawable is AnimatedVectorDrawable) drawable.start()
}

fun View.startViewAnimation(@AnimRes animationRes: Int) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.scale_aftera_delay)
    startAnimation(animation)
}

fun RecyclerView.addOnScrollListener(
    onScrollLeft: () -> Unit = {},
    onScrollRight: () -> Unit = {},
    onScrollUp: () -> Unit = {},
    onScrollDown: () -> Unit = {}
): OnScrollListener {
    return object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dx > 0) {
                onScrollLeft()
            } else if (dx < 0) {
                onScrollRight()
            } else if (dy > 0) {
                onScrollUp()
            } else {
                onScrollDown()
            }
        }
    }.apply { addOnScrollListener(this) }
}

fun RecyclerView.addOnScrollStateChangeListener(onScrollStateChanged: (Int) -> Unit): OnScrollListener {
    return object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            onScrollStateChanged(newState)
        }
    }.apply { addOnScrollListener(this) }
}