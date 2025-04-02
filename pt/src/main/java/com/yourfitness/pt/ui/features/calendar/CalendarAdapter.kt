package com.yourfitness.pt.ui.features.calendar

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yourfitness.common.domain.date.addMs
import com.yourfitness.common.domain.date.dayOfWeekFormatted
import com.yourfitness.common.domain.date.timeFormatted
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.databinding.ItemCalendarDayTitleBinding
import com.yourfitness.pt.databinding.ItemCalendarFilledBinding
import com.yourfitness.pt.databinding.ItemCalendarSelectedBinding
import com.yourfitness.pt.databinding.ItemTimeSlotBinding
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.timeSlotDurationMs
import com.yourfitness.pt.domain.models.*
import com.yourfitness.pt.ui.utils.CalendarDiffCallback
import com.yourfitness.pt.ui.utils.CalendarListDiffCallback

class CalendarPagingAdapter(
    private val onCardClick: (slot: FilledTimeSlot) -> Unit,
    private val onTimeSlotClick: (slot: TimeSlot) -> Unit,
    private val onSelectedTimeSlotClick: (slot: SelectedTimeSlot) -> Unit
) : PagingDataAdapter<CalendarData, RecyclerView.ViewHolder>(CalendarDiffCallback) {

    private val calendarData = arrayListOf<CalendarData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding = ItemCalendarDayTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemCalendarDayHolder(binding)
            }
            1 -> {
                val binding = ItemTimeSlotBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemTimeSlotHolder(binding, onTimeSlotClick)
            }
            2 -> {
                val binding = ItemCalendarSelectedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemCalendarSelectedHolder(binding, onSelectedTimeSlotClick)
            }
            else -> {
                val binding = ItemCalendarFilledBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemCalendarFilledHolder(binding, onCardClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = calendarData[position]
        when (holder.itemViewType) {
            0 -> {
                (holder as ItemCalendarDayHolder).bind(item)
            }
            1 -> {
                (holder as ItemTimeSlotHolder).bind(item)
            }
            2 -> {
                (holder as ItemCalendarSelectedHolder).bind(item)
            }
            else -> {
                (holder as ItemCalendarFilledHolder).bind(item)
            }
        }
    }

    override fun getItemCount(): Int = calendarData.size

    override fun getItemViewType(position: Int): Int = calendarData[position].type

    fun setData(data: List<CalendarData>) {
        val diffCallBack = CalendarListDiffCallback(calendarData, data)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        calendarData.clear()
        calendarData.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }
}

class ItemCalendarDayHolder(
    binding: ItemCalendarDayTitleBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemCalendarDayTitleBinding = ItemCalendarDayTitleBinding.bind(itemView)

    fun bind(card: CalendarData) {
        val data = card as DayRow
        binding.labelDay.text = data.date.dayOfWeekFormatted()
    }
}

class ItemTimeSlotHolder(
    binding: ItemTimeSlotBinding,
    private val onTimeSlotClick: (slot: TimeSlot) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemTimeSlotBinding = ItemTimeSlotBinding.bind(itemView)

    fun bind(card: CalendarData) {
        val data = card as TimeSlot
        binding.timeSlot.text = data.time.timeFormatted()
        binding.timeSlot.isEnabled = data.enabled
        if (!data.enabled) return
        binding.root.setOnClickListener { onTimeSlotClick(data) }
    }
}

class ItemCalendarSelectedHolder(
    binding: ItemCalendarSelectedBinding,
    private val onSelectedTimeSlotClick: (slot: SelectedTimeSlot) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemCalendarSelectedBinding = ItemCalendarSelectedBinding.bind(itemView)

    fun bind(card: CalendarData) {
        val data = card as SelectedTimeSlot
        binding.time.text = "${data.time.timeFormatted()} - ${data.timeTo.timeFormatted()}"
        binding.firstTimeSlot.timeSlot.text = data.time.timeFormatted()
        binding.secondTimeSlot.timeSlot.text = data.timeSecondSlot.timeFormatted()
        binding.root.setOnClickListener { onSelectedTimeSlotClick(data) }
    }
}

class ItemCalendarFilledHolder(
    binding: ItemCalendarFilledBinding,
    private val onCardClick: (slot: FilledTimeSlot) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemCalendarFilledBinding = ItemCalendarFilledBinding.bind(itemView)

    fun bind(card: CalendarData) {
        val data = card as FilledTimeSlot
        val context = binding.root.context
        val textColor =
            if (data.uiData.isLight) com.yourfitness.common.R.color.white else com.yourfitness.common.R.color.black
        binding.facility.isVisible = data.facility.isNotBlank()
        binding.facility.text = data.facility
        binding.facility.setTextColorRes(textColor)
        binding.status.text = context.getString(data.uiData.status).uppercase()
        binding.status.setTextColorRes(textColor)
        binding.bgContainer.background = ContextCompat.getDrawable(context, data.uiData.bgColor)

        binding.statusInfo.apply {
            if (data.uiData.statusInfo == null) {
                isVisible = false
            } else {
                isVisible = true
                setTextColorRes(textColor)
                text = context.getString(data.uiData.statusInfo)
            }
        }

        binding.statusImage.setImageDrawable(
            if (data.uiData.statusIcon == null) null
            else ContextCompat.getDrawable(context, data.uiData.statusIcon)
        )
        binding.statusImage.isVisible = data.uiData.statusIcon != null

        val startTime = data.time.timeFormatted()
        val ensTime = data.time.addMs(timeSlotDurationMs * 2).timeFormatted()
        binding.time.setTextColorRes(textColor)
        binding.time.text = "$startTime - $ensTime"

        Glide.with(context)
            .load(data.facilityImg.toImageUri())
            .circleCrop()
            .into(object : CustomTarget<Drawable?>(19.toPx(), 19.toPx()) {

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    binding.facility.setCompoundDrawables(start = resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        binding.root.setOnClickListener { onCardClick(data) }
    }
}

class SpacesItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = 4.toPx()
        outRect.bottom = 4.toPx()
    }
}

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
