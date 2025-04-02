package com.yourfitness.pt.ui.features.training_calendar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemCalendarWeekHeaderBinding
import com.yourfitness.common.databinding.ItemPaymentHistoryHeaderBinding
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.*
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.pt.ui.utils.SessionStatus

class PtScheduleAdapter(
    onItemClick: (sessionId: String, status: String) -> Unit
) : PtWeekAdapter(onItemClick) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseWeekViewHolder {
        return when (viewType) {
            CalendarAdapterType.SEPARATOR.type -> {
                val binding = ItemPaymentHistoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ScheduleMonthHeaderViewHolder(binding)
            }
            CalendarAdapterType.HEADER.type -> {
                val binding = ItemCalendarWeekHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                IntervalHeaderViewHolder(binding)
            }
            else -> {
                super.onCreateViewHolder(parent, viewType)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = currentList[position]) {
            is CalendarView.Header -> {
                if (item.endDate == null) CalendarAdapterType.SEPARATOR.type
                else CalendarAdapterType.HEADER.type
            }
            is CalendarView.CalendarItem -> {
                if (item.status == SessionStatus.BLOCKED_SLOT.value) CalendarAdapterType.BLOCK.type
                else CalendarAdapterType.BODY.type
            }
            else -> super.getItemViewType(position)
        }
    }
}

class ScheduleMonthHeaderViewHolder(
    binding: ItemPaymentHistoryHeaderBinding
) : BaseWeekViewHolder(binding.root) {
    private val binding: ItemPaymentHistoryHeaderBinding = ItemPaymentHistoryHeaderBinding.bind(itemView)

    override fun bind(
        item: CalendarView,
        tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        item as CalendarView.Header
        binding.textHeaderDate.text = item.startDate?.toMmmmYyyy()
    }
}

class IntervalHeaderViewHolder(binding: ItemCalendarWeekHeaderBinding) : WeekHeaderViewHolder(binding) {

    override fun bind(
        item: CalendarView, tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        binding.textWeekHeader.setTextColorRes(R.color.text_gray)
        super.bind(item, tempDate, updateTempDate)
    }
}
