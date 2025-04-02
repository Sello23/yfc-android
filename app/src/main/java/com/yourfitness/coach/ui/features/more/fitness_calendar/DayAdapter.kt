package com.yourfitness.coach.ui.features.more.fitness_calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.common.databinding.ItemPaymentHistoryHeaderBinding
import com.yourfitness.common.databinding.ViewFitnessCalendarDayBinding
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.*
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri

class DayAdapter(
    private val onItemClick: (textView: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : BaseDayAdapter<BaseDayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDayViewHolder {
        return when (viewType) {
            CalendarAdapterType.HEADER.type -> {
                val binding = ItemPaymentHistoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DayHeaderViewHolder(binding)
            }
            else -> {
                val binding = ViewFitnessCalendarDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DayViewHolder(binding, onItemClick)
            }
        }
    }
}

class DayViewHolder(
    containerView: ViewFitnessCalendarDayBinding,
    private val onItemClick: (textView: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : BodyDayViewHolder(containerView) {

    override fun bind(item: CalendarView) {
        item as CalendarView.CalendarItem
        binding.card.apply {
            if (item.day == 0L) {
                root.isVisible = true
                binding.emptyState.isVisible = false
                val difference = dateTimeDifference(
                    item.time?.times(1000) ?: 1,
                    today().time,
                    TimeDifference.MINUTE
                )
                actionLabel.setTextColorRes(com.yourfitness.common.R.color.main_active)
                when {
                    difference in (-1) * (item.classEntryLeadTime
                        ?: 15)..(item.classEntryLeadTime
                        ?: 0) -> {
                        actionLabel.text =
                            itemView.context.getString(R.string.fitness_calendar_screen_get_access_text)
                        actionLabel.isVisible = true
                    }
                    difference > (item.classEntryLeadTime ?: 15) -> {
                        actionLabel.text =
                            itemView.context.getString(R.string.fitness_calendar_screen_manage_class_text)
                        actionLabel.isVisible = true
                    }
                    difference < (-1) * (item.classEntryLeadTime ?: 15) -> {
                        actionLabel.isVisible = false
                    }
                }
//                        getAccess.setOnClickListener { onItemClick(getAccess, getAccess.text.toString(), item.toBookedClass()) }
                title.text = item.objectName
                time.text = item.time.toDateTime()
                title.text = item.coachName
                date.text = item.date.toDateDayOfWeekMonth()
                address.text = item.address
                info.text = item.facilityName
                Glide.with(itemView.context).load(item.icon.toImageUri()).into(imageIcon)
            } else {
                binding.emptyState.isVisible = true
                root.isVisible = false
            }
        }
    }
}
