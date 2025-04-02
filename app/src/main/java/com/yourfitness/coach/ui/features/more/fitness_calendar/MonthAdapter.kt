package com.yourfitness.coach.ui.features.more.fitness_calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ViewFacilityFullInfoCardBinding
import com.yourfitness.common.domain.date.TimeDifference
import com.yourfitness.common.domain.date.dateTimeDifference
import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
import com.yourfitness.common.domain.date.toDateTime
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.BaseMonthAdapter
import com.yourfitness.common.ui.features.fitness_calendar.BaseMonthViewHolder
import com.yourfitness.common.ui.utils.setTextColorRes

class MonthAdapter(
    private val onItemClick: (view: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : BaseMonthAdapter<MonthViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ViewFacilityFullInfoCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthViewHolder(binding, onItemClick)
    }
}

class MonthViewHolder(
    binding: ViewFacilityFullInfoCardBinding,
    private val onItemClick: (view: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : BaseMonthViewHolder(binding)  {

    override fun bind(item: CalendarView.CalendarItem) {
        super.bind(item)

        val difference = dateTimeDifference(item.time * 1000, today, TimeDifference.MINUTE)
        binding.actionLabel.setTextColorRes(R.color.main_active)
        when {
            difference in (-1) * item.classEntryLeadTime..item.classEntryLeadTime -> {
                binding.actionLabel.isVisible = true
                binding.viewSeparator.isVisible = true
//                binding.textGetAccess.text = itemView.context.getString(R.string.fitness_calendar_screen_get_access_text)
            }
            difference > item.classEntryLeadTime -> {
                binding.actionLabel.isVisible = true
                binding.viewSeparator.isVisible = true
//                binding.textGetAccess.text = itemView.context.getString(R.string.fitness_calendar_screen_manage_class_text)
            }
            difference < (-1) * item.classEntryLeadTime -> {
                binding.actionLabel.isVisible = false
                binding.viewSeparator.isVisible = false
            }
        }
        binding.time.text = item.time.toDateTime()
        binding.date.text = item.date.toDateDayOfWeekMonth()
        binding.actionLabel.setOnClickListener { onItemClick(binding.actionLabel, binding.actionLabel.text.toString(), item) }
    }
}