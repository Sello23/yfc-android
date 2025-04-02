package com.yourfitness.common.ui.utils

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.yourfitness.common.R

class PagerIndicator {

    fun setupViewpager(indicatorsView: LinearLayout, viewPager: ViewPager2) {
        setupIndicators(indicatorsView, viewPager)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { updateIndicators(position, indicatorsView, viewPager) }
        })
    }

    private fun setupIndicators(indicatorsView: LinearLayout, viewPager: ViewPager2) {
        val adapter = viewPager.adapter!!
        val context = viewPager.context
        val indicators = arrayOfNulls<ImageView>(adapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(16, 0, 16, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(context)
            indicators[i].apply {
                this?.setImageDrawable(getDrawable(context, R.drawable.ic_ellipse_671_inactive))
                this?.layoutParams = layoutParams
            }
            indicatorsView.addView(indicators[i])
        }
    }

    private fun updateIndicators(index: Int, indicatorsView: LinearLayout, viewPager: ViewPager2) {
        val context = viewPager.context
        val childCount = indicatorsView.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorsView[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(getDrawable(context, R.drawable.ic_ellipse_670_active))
            } else {
                imageView.setImageDrawable(getDrawable(context, R.drawable.ic_ellipse_671_inactive))
            }
        }
    }
}
