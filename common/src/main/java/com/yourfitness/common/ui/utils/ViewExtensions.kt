package com.yourfitness.common.ui.utils

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import com.yourfitness.common.databinding.ItemAmenityBinding
import com.yourfitness.common.domain.InputMask

fun EditText.applyInputMask(
    mask: InputMask,
    extraMasks: List<String> = emptyList(),
    onTextChanged: (String) -> Unit = { },
): MaskedTextChangedListener {
    return MaskedTextChangedListener.installOn(this,
        mask.value,
        extraMasks,
        AffinityCalculationStrategy.PREFIX,
        object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(
                maskFilled: Boolean,
                extractedValue: String,
                formattedValue: String,
            ) {
                onTextChanged.invoke(formattedValue)
            }
        })
}

fun Fragment.setupTopInsets(view: View) {
    val topInset = getTopInsets()
    view.setPadding(
        view.paddingLeft, view.paddingTop + topInset, view.paddingRight, view.paddingBottom
    )
}

fun Fragment.getTopInsets(): Int {
    val decorView = requireActivity().window.decorView
    val rootInsets = ViewCompat.getRootWindowInsets(decorView) ?: WindowInsetsCompat.Builder().build()
    val insets = rootInsets.getInsets(WindowInsetsCompat.Type.statusBars())
    return insets.top
}

fun Fragment.setupBottomInsets(view: View) {
    val decorView = requireActivity().window.decorView
    val rootInsets =
        ViewCompat.getRootWindowInsets(decorView) ?: WindowInsetsCompat.Builder().build()
    val insets = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
    view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom + insets.bottom
    )
}

fun TabLayout.addOnTabSelectionListener(onTabSelected: (TabLayout.Tab) -> Unit) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) { onTabSelected(tab) }
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    })
}

fun TabLayout.selectTab(position: Int) {
    selectTab(getTabAt(position))
}

fun ChipGroup.showChips(
    layoutInflater: LayoutInflater,
    allItems: List<String>,
    selected: List<String> = emptyList(),
    limited: Boolean = false,
    onCheckedChanged: (selected: List<String>) -> Unit = {},
    images: List<String>? = null,
    updateGenders: Boolean = false
) {
    removeAllViews()
    if (images != null && images.size != allItems.size) return
    val itemsToShow = if (limited) allItems.take(10) else allItems
    for (i in itemsToShow.indices)  {
        val binding = ItemAmenityBinding.inflate(layoutInflater, this, false)
        val it = itemsToShow[i]
        binding.check.text = it
        binding.root.isSelected = it in selected
        if (images != null) {
            binding.image.isVisible = true
            Glide.with(binding.root).load(images[i].toImageUri()).into(binding.image)
        }
        binding.root.setOnClickListener {
            binding.root.isSelected = !binding.root.isSelected
            binding.check.isSelected = binding.root.isSelected
            onCheckedChanged(selected(selected, updateGenders))
        }
        addView(binding.root)
    }
}

private fun ChipGroup.selected(allSelected: List<String>, updateGenders: Boolean = false): List<String> {
    val limited = children
        .map {
            val item = (it as LinearLayoutCompat).getChildAt(1)
            (item as MaterialTextView).text.toString()
        }
    val part2 = allSelected.subtract(limited.toSet())
    val part1 = children
        .filter {
            it.isSelected
        }
        .map {
            val item = (it as LinearLayoutCompat).getChildAt(1)
            (item as MaterialTextView).text.toString()
        }
    return (part1 + part2).toList()
}

fun TextView.setCompoundDrawables(
    @DrawableRes start: Int = 0,
    @DrawableRes top: Int = 0,
    @DrawableRes end: Int = 0,
    @DrawableRes bottom: Int = 0
) {
    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, start, top, end, bottom)
}

fun TextView.setCompoundDrawables(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null
) {
    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, start, top, end, bottom)
}

fun ViewPager2.setShowSideItems(pageMarginPx : Int, offsetPx : Int) {
    clipToPadding = false
    clipChildren = false
    offscreenPageLimit = 3

    setPageTransformer { page, position ->

        val offset = position * -(2 * offsetPx + pageMarginPx)
        if (this.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                page.translationX = -offset
            } else {
                page.translationX = offset
            }
        } else {
            page.translationY = offset
        }
    }
}

