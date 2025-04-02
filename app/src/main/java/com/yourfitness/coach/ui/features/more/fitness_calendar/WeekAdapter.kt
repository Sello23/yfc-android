package com.yourfitness.coach.ui.features.more.fitness_calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.common.databinding.ItemCalendarWeekBinding
import com.yourfitness.common.databinding.ItemCalendarWeekHeaderBinding
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.*
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri

class WeekAdapter(
    private val onItemClick: (textView: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : BaseWeekAdapter<BaseWeekViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseWeekViewHolder {
        return when (viewType) {
            CalendarAdapterType.HEADER.type -> {
                val binding = ItemCalendarWeekHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                WeekHeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemCalendarWeekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                WeekViewHolder(binding, onItemClick)
            }
        }
    }
}

class WeekViewHolder(
    binding: ItemCalendarWeekBinding,
    private val onItemClick: (textView: TextView, text: String, bookedClass: CalendarView.CalendarItem) -> Unit
) : StudioWeekViewHolder(binding.root) {

    override fun bind(
        item: CalendarView,
        tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        super.bind(item, tempDate, updateTempDate)

        item as CalendarView.CalendarItem

        if (item.day == 0L) {
            binding.viewEmptyDay.isVisible = false
            binding.viewCard.root.isVisible = true
            val difference = dateTimeDifference(
                item.time.times(1000) ?: 1,
                today().time,
                TimeDifference.MINUTE
            )
            binding.viewCard.actionLabel.setTextColorRes(com.yourfitness.common.R.color.main_active)
            when {
                difference in (-1) * (item.classEntryLeadTime
                    ?: 15)..(item.classEntryLeadTime ?: 15) -> {
                    binding.viewCard.actionLabel.text =
                        itemView.context.getString(R.string.fitness_calendar_screen_get_access_text)
                    binding.viewCard.actionLabel.isVisible = true
                    binding.viewEmptyDay.isVisible = true
                }
                difference > (item.classEntryLeadTime ?: 15) -> {
                    binding.viewCard.actionLabel.text =
                        itemView.context.getString(R.string.fitness_calendar_screen_manage_class_text)
                    binding.viewCard.actionLabel.isVisible = true
                    binding.viewEmptyDay.isVisible = true
                }
                difference < (-1) * (item.classEntryLeadTime ?: 15) -> {
                    binding.viewCard.actionLabel.isVisible = false
                    binding.viewEmptyDay.isVisible = false
                }
            }
            if (tempDate == item.date.toDd()) {
                binding.textDay.visibility = View.INVISIBLE
                binding.textDayOfWeek.visibility = View.INVISIBLE
            } else {
                binding.textDay.visibility = View.VISIBLE
                binding.textDayOfWeek.visibility = View.VISIBLE
                updateTempDate(item.date.toDd() ?: "")
            }
            binding.viewCard.actionLabel.setOnClickListener {
//                onItemClick(
//                    binding.textGetAccess,
//                    binding.textGetAccess.text.toString(),
//                    itemData.toBookedClass()
//                )
            }
            binding.textDay.text = item.date.toDd()
            binding.textDayOfWeek.text = item.date.toEee()
            binding.viewCard.title.text = item.objectName
            binding.viewCard.time.text = item.time.toDateTime()
            binding.viewCard.title.text = item.coachName
            binding.viewCard.date.text = item.date.toDateDayOfWeekMonth()
            binding.viewCard.address.text = item.address
            binding.viewCard.info.text = item.facilityName
            Glide.with(itemView.context).load(item.icon.toImageUri()).into(binding.viewCard.imageIcon)
        } else {
            binding.textDay.text = item.day?.toDd()
            binding.textDayOfWeek.text = item.day?.toEee()
            binding.viewEmptyDay.isVisible = true
            binding.viewCard.root.isVisible = false
            if (tempDate == item.date.toDd()) {
                binding.textDay.visibility = View.INVISIBLE
                binding.textDayOfWeek.visibility = View.INVISIBLE
            } else {
                binding.textDay.visibility = View.VISIBLE
                binding.textDayOfWeek.visibility = View.VISIBLE
            }
        }
    }
}
