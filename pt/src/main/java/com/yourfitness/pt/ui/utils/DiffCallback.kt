package com.yourfitness.pt.ui.utils

import androidx.recyclerview.widget.DiffUtil
import com.yourfitness.pt.domain.models.CalendarData

object CalendarDiffCallback : DiffUtil.ItemCallback<CalendarData>() {
    override fun areItemsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean {
        return oldItem == newItem
    }
}

class CalendarListDiffCallback(
    private val oldList: List<CalendarData>,
    private val newList: List<CalendarData>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
