package com.yourfitness.pt.ui.features.training_calendar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemCalendarBlockedWeekBinding
import com.yourfitness.common.databinding.ViewFacilityFullInfoCardBinding
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.domain.date.toDdMs
import com.yourfitness.common.domain.date.toEeeMs
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.AbstractMonthViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.BaseMonthAdapter
import com.yourfitness.common.ui.features.fitness_calendar.BaseMonthViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.BaseWeekViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.CalendarAdapterType

class PtMonthAdapter(
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : BaseMonthAdapter<AbstractMonthViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractMonthViewHolder {
        return when (viewType) {
            CalendarAdapterType.BODY.type -> {
                val binding = ViewFacilityFullInfoCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PtMonthViewHolder(binding, onItemClick)
            }
            else -> {
                val binding = ItemCalendarBlockedWeekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MonthBlockViewHolder(binding, onItemClick)
            }
        }
    }
}

class PtMonthViewHolder(
    binding: ViewFacilityFullInfoCardBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : BaseMonthViewHolder(binding), CalendarCardBuilder {
    override fun bind(item: CalendarView.CalendarItem) {
        super.bind(item)

        binding.buildCard(item, onItemClick)
    }
}

class MonthBlockViewHolder(
    binding: ItemCalendarBlockedWeekBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : AbstractMonthViewHolder(binding.root), CalendarCardBuilder {
    val binding: ItemCalendarBlockedWeekBinding = ItemCalendarBlockedWeekBinding.bind(itemView)

    override fun bind(item: CalendarView.CalendarItem) {
        binding.spacer.isVisible = false

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 0)
        binding.viewCard.root.layoutParams = params
        binding.buildCard(item, onItemClick)
    }
}
