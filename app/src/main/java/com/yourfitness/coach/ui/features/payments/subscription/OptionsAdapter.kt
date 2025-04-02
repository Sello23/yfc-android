package com.yourfitness.coach.ui.features.payments.subscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.core.view.isVisible
import com.yourfitness.coach.databinding.ItemExpandableChildBinding
import com.yourfitness.coach.databinding.ItemExpandableRootBinding

class OptionsAdapter(val data: List<OptionData>) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int = data.size

    override fun getChildrenCount(p0: Int): Int = 1

    override fun getGroup(p0: Int): OptionData = data[p0]

    override fun getChild(p0: Int, p1: Int): List<String> = data[p0].details

    override fun getGroupId(p0: Int): Long = p0.toLong()

    override fun getChildId(p0: Int, p1: Int): Long = p1.toLong()

    override fun hasStableIds(): Boolean = true

    override fun isChildSelectable(p0: Int, p1: Int): Boolean = false

    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, parent: ViewGroup?): View {
        val context = p2?.context ?: parent?.context
        val itemView = ItemExpandableRootBinding.inflate(LayoutInflater.from(context), parent, false)
        itemView.apply {
        val info = data[p0]
            optionTitle.text = info.title
            optionSubtitle.text = info.subTitle
            price.text = info.price
            priceOld.text = info.oldPrice
            priceOld.isVisible = info.oldPrice != null
            root.isSelected = true
            cardContent.isSelected = true
        }
        return itemView.root
    }

    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, parent: ViewGroup?): View {
        val context = p3?.context ?: parent?.context
        val itemView = ItemExpandableChildBinding.inflate(LayoutInflater.from(context), parent, false)
        val details = data[p0].details
        itemView.apply {
            val info1 = details.getOrNull(0)
            tvOption1.text = info1
            tvOption1.isVisible = info1 != null
            ivOption1.isVisible = info1 != null

            val info2 = details.getOrNull(1)
            tvOption2.text = info2
            tvOption2.isVisible = info2 != null
            ivOption2.isVisible = info2 != null

            val info3 = details.getOrNull(2)
            tvOption3.text = info3
            tvOption3.isVisible = info3 != null
            ivOption3.isVisible = info3 != null
        }
        return itemView.root
    }

    override fun onGroupExpanded(groupPosition: Int) {
        println()
    }

    override fun onGroupCollapsed(groupPosition: Int) {
        println()
    }
}

data class OptionData(
    val title: String,
    val subTitle: String,
    val price: String,
    val oldPrice: String?,
    val details: List<String>
)
