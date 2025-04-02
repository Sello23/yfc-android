package com.yourfitness.pt.ui.features.training_calendar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemCalendarBlockedWeekBinding
import com.yourfitness.common.databinding.ItemPaymentHistoryHeaderBinding
import com.yourfitness.common.databinding.ViewBlockedSlotCardBinding
import com.yourfitness.common.databinding.ViewFacilityFullInfoCardBinding
import com.yourfitness.common.databinding.ViewFitnessCalendarDayBinding
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toDateDayOfWeekMonthMs
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.domain.date.toDdMs
import com.yourfitness.common.domain.date.toEeeMs
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.*
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.ui.utils.SessionStatus

class PtDayAdapter(
    private val onItemClick: (sessionId: String, status: String) -> Unit
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
            CalendarAdapterType.BODY.type -> {
                val binding = ViewFitnessCalendarDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PtDayViewHolder(binding, onItemClick)
            }
            else -> {
                val binding = ItemCalendarBlockedWeekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DayBlockViewHolder(binding, onItemClick)
            }
        }
    }
}

class PtDayViewHolder(
    binding: ViewFitnessCalendarDayBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : BodyDayViewHolder(binding), CalendarCardBuilder {

    override fun bind(item: CalendarView) {
        super.bind(item)
        item as CalendarView.CalendarItem

        binding.emptyState.isVisible = item.status == null
        binding.card.buildCard(item, onItemClick)
    }
}

open class DayBlockViewHolder(
    binding: ItemCalendarBlockedWeekBinding,
    private val onItemClick: (sessionId: String, status: String) -> Unit
) : BaseDayViewHolder(binding.root), CalendarCardBuilder {
    val binding: ItemCalendarBlockedWeekBinding = ItemCalendarBlockedWeekBinding.bind(itemView)

    override fun bind(item: CalendarView) {
        item as CalendarView.CalendarItem
        binding.spacer.isVisible = false
        binding.buildCard(item, onItemClick)
    }
}

interface CalendarCardBuilder {
    fun ViewFacilityFullInfoCardBinding.buildCard(
        item: CalendarView.CalendarItem,
        onItemClick: ((sessionId: String, status: String) -> Unit)? = null
    ) {
        val context = root.context
        content.isVisible = item.status != null
        status.isVisible = item.statusBuilder != null
        statusImg.isVisible = item.status == SessionStatus.COMPLETED.value && item.statusBuilder != null
        if (item.status != null) {
            time.text = context.getString(
                R.string.training_calendar_screen_time_interval,
                item.time.toDateTimeMs(),
                item.timeTo.toDateTimeMs()
            )
            date.text = item.date.toDateDayOfWeekMonthMs()
            status.text = item.statusBuilder?.invoke(item.status.orEmpty(), context)?.uppercase()
            val bgColor = item.statusBg?.let { ContextCompat.getColor(context, it) }
            val drawable = ContextCompat.getDrawable(context, R.drawable.rounded_border_main_active)
            if (bgColor != null && drawable != null) {
                DrawableCompat.setTint(drawable, bgColor)
            }
            status.background = drawable
            viewSeparator.isVisible = item.actionLabel != null
            actionLabel.apply {
                isVisible = item.actionLabel != null
                if (isVisible) {
                    setTextColorRes(item.actionLabel!!.second)
                    text = context.getString(item.actionLabel!!.first).lowercase()
                        .replaceFirstChar { it.uppercase() }
                    setOnClickListener { onItemClick?.invoke(item.objectId.orEmpty(), item.status.orEmpty()) }
                }
            }
            title.text = item.objectName
            subtitle.text = item.labelBuilder?.let { it(context) }
            info.text = item.facilityName
            address.text = item.address
            Glide.with(context).load(item.icon.toImageUri()).into(imageIcon)
        }
    }

    fun ItemCalendarBlockedWeekBinding.buildCard(
        item: CalendarView.CalendarItem,
        onItemClick: ((sessionId: String, status: String) -> Unit)? = null
    ) {
        apply {
            textDay.isVisible = false
            textDayOfWeek.isVisible = false

            viewCard.buildCard(item, onItemClick)
        }
    }

    fun ViewBlockedSlotCardBinding.buildCard(
        item: CalendarView.CalendarItem,
        onItemClick: ((sessionId: String, status: String) -> Unit)? = null
    ) {
        apply {
            actionLabel.setOnClickListener {
                onItemClick?.invoke(item.objectId.orEmpty(), item.status.orEmpty())
            }

            val context = root.context
            val bgColor = ContextCompat.getColor(context, R.color.issue_red)
            val drawable = ContextCompat.getDrawable(context, R.drawable.rounded_border_main_active)
            if (drawable != null) {
                DrawableCompat.setTint(drawable, bgColor)
            }
            status.background = drawable
            status.text = context.getString(com.yourfitness.pt.R.string.status_blocked).uppercase()
            dateLabel.text = item.time.toCustomFormat()
            timeLabel.text = context.getString(
                R.string.training_calendar_screen_time_interval,
                item.time.toDateTimeMs(),
                item.timeTo.toDateTimeMs()
            )
        }
    }
}
