package com.yourfitness.pt.ui.features.training_calendar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemCalendarBlockedWeekBinding
import com.yourfitness.common.databinding.ItemCalendarWeekBinding
import com.yourfitness.common.databinding.ItemCalendarWeekHeaderBinding
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.domain.date.toDd
import com.yourfitness.common.domain.date.toDdMs
import com.yourfitness.common.domain.date.toEee
import com.yourfitness.common.domain.date.toEeeMs
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.*

open class PtWeekAdapter(
    private val onItemClick: (sessionId: String, status: String) -> Unit
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
            CalendarAdapterType.BODY.type -> {
                val binding = ItemCalendarWeekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PtWeekViewHolder(binding, onItemClick)
            }
            else -> {
                val binding = ItemCalendarBlockedWeekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                WeekBlockViewHolder(binding, onItemClick)
            }
        }
    }
}


class PtWeekViewHolder(
    binding: ItemCalendarWeekBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : StudioWeekViewHolder(binding.root), CalendarCardBuilder {

    override fun bind(
        item: CalendarView,
        tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        super.bind(item, tempDate, updateTempDate)
        item as CalendarView.CalendarItem

        binding.apply {
            textDay.isVisible = item.day != null
            textDayOfWeek.isVisible = item.day != null

            viewEmptyDay.isVisible = item.status == null
            viewCard.root.isVisible = item.status != null

            if (item.day != null) {
                textDay.text = item.day?.toDdMs()
                textDayOfWeek.text = item.day?.toEeeMs()
            }

            viewCard.buildCard(item, onItemClick)
        }
    }
}

class WeekBlockViewHolder(
    binding: ItemCalendarBlockedWeekBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : BaseWeekViewHolder(binding.root), CalendarCardBuilder {
    val binding: ItemCalendarBlockedWeekBinding = ItemCalendarBlockedWeekBinding.bind(itemView)

    override fun bind(
        item: CalendarView,
        tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        item as CalendarView.CalendarItem

        binding.buildCard(item, onItemClick)
        binding.apply {
            textDay.isVisible = item.day != null
            textDayOfWeek.isVisible = item.day != null

            if (item.day != null) {
                textDay.text = item.day?.toDdMs()
                textDayOfWeek.text = item.day?.toEeeMs()
            }
        }
    }
}

