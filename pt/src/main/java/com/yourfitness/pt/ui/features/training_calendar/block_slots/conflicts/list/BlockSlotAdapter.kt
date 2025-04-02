package com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.databinding.ItemCalendarBlockedWeekBinding
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.BaseDayViewHolder
import com.yourfitness.pt.R
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder
import com.yourfitness.pt.ui.features.training_calendar.adapters.DayBlockViewHolder

class BlockSlotAdapter(
    private val onClick: ((String, String) -> Unit)? = null,
    private val onCheckboxClick: ((sessionId: String, selected: Boolean, pos: Int) -> Unit)? = null,
    private val hideAction: Boolean = false,
) : RecyclerView.Adapter<BlockSlotViewHolder>() {

    private val items: MutableList<Pair<Boolean, CalendarView.CalendarItem>> = mutableListOf()
    private var multiselectEnabled = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockSlotViewHolder {
        val binding = ItemCalendarBlockedWeekBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlockSlotViewHolder(binding, onClick, onCheckboxClick, hideAction)
    }

    override fun onBindViewHolder(holder: BlockSlotViewHolder, position: Int) {
        holder.bind(items[position], position, multiselectEnabled)
    }

    override fun getItemCount(): Int = items.size

    fun setData(items: List<Pair<Boolean, CalendarView.CalendarItem>>) {
        val diffCallBack = BlockSLotsDiffCallback(this.items.map { it.second }, items.map { it.second })
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateMultiselectMode(enabled: Boolean) {
        multiselectEnabled = enabled
    }
}

private class BlockSLotsDiffCallback(
    private val oldList: List<CalendarView.CalendarItem>,
    private val newList: List<CalendarView.CalendarItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].objectId == newList[newItemPosition].objectId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

class BlockSlotViewHolder(
    binding: ItemCalendarBlockedWeekBinding,
    onItemClick: ((sessionId: String, status: String) -> Unit)? = null,
    private val onCheckboxClick: ((sessionId: String, selected: Boolean, pos: Int) -> Unit)? = null,
    private val hideAction: Boolean,
) : DayBlockViewHolder(binding, onItemClick ?: { _, _ -> }) {

    fun bind(itemData: Pair<Boolean, CalendarView>, position: Int, isMultiselect: Boolean) {
        binding.viewCard.selectionCheckbox.isVisible = isMultiselect
        binding.viewCard.selectionCheckbox.isChecked = itemData.first
        binding.viewCard.selectionBg.isVisible = isMultiselect
        binding.viewCard.selectionBg.isSelected = itemData.first

        val item = itemData.second as CalendarView.CalendarItem
        super.bind(item)
        binding.viewCard.repeatLabel.isVisible = item.repeats > 1
        if (item.repeats > 1) {
            binding.viewCard.repeatLabel.text = binding.root.context.resources.getQuantityString(
                R.plurals.repeat_weeks,
                item.repeats,
                item.repeats
            )
        }

        binding.viewCard.selectionBg.setOnClickListener {
            onCheckboxClick?.invoke(item.objectId.orEmpty(), !itemData.first, position)
        }
        binding.viewCard.selectionCheckbox.setOnClickListener {
            onCheckboxClick?.invoke(item.objectId.orEmpty(), !itemData.first, position)
        }

        binding.viewCard.actionLabel.isVisible = !hideAction
        binding.viewCard.viewSeparator.isVisible = !hideAction
    }
}
