package com.yourfitness.common.ui.features.fitness_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemCalendarWeekBinding
import com.yourfitness.common.databinding.ItemCalendarWeekHeaderBinding
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.utils.setTextColorRes

enum class CalendarAdapterType(val type: Int) {
    HEADER(0),
    BODY(1),
    SEPARATOR(2),
    BLOCK(3),
}

abstract class BaseWeekAdapter<VH : BaseWeekViewHolder> : RecyclerView.Adapter<VH>() {

    private var tempDate = ""
    protected val currentList = mutableListOf<CalendarView>()

    override fun getItemViewType(position: Int): Int {
        return when (val item = currentList[position]) {
            is CalendarView.Header -> CalendarAdapterType.HEADER.type
            else -> {
                item as CalendarView.CalendarItem
                if (item.status == "blocked_slot") CalendarAdapterType.BLOCK.type
                else CalendarAdapterType.BODY.type
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position], tempDate, ::updateTempDate)
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.size

    fun submitList(list: List<CalendarView>) {
        currentList.clear()
        currentList.addAll(list)
        notifyDataSetChanged()
    }

    private fun updateTempDate(date: String) {
        tempDate = date
    }
}

open class WeekHeaderViewHolder(binding: ItemCalendarWeekHeaderBinding) :
    BaseWeekViewHolder(binding.root) {
    protected val binding: ItemCalendarWeekHeaderBinding =
        ItemCalendarWeekHeaderBinding.bind(itemView)

    override fun bind(
        item: CalendarView, tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        item as CalendarView.Header
        val startDate = if (item.startDate.sameMonthAs(item.endDate)) item.startDate?.toDay()
        else item.startDate?.toDMmmmYyyy()
        if (item.startDate == item.endDate) {
            binding.textWeekHeader.text = item.startDate?.toDMmmmYyyy()
        } else {
            binding.textWeekHeader.text = binding.root.context.getString(
                R.string.fitness_calendar_screen_start_end_week_text,
                startDate,
                item.endDate?.toDMmmmYyyy(),
            )
        }
    }
}

open class StudioWeekViewHolder(bindingView: View) : BaseWeekViewHolder(bindingView) {
    protected val binding: ItemCalendarWeekBinding = ItemCalendarWeekBinding.bind(itemView)

    override fun bind(
        item: CalendarView,
        tempDate: String,
        updateTempDate: (date: String) -> Unit
    ) {
        item as CalendarView.CalendarItem

        val today = today().setDayStart()

        binding.apply {
            if (item.day.toDate()?.setDayStart() == today) {
                textDay.setTextColorRes(R.color.blue)
                textDayOfWeek.setTextColorRes(R.color.blue)
            } else {
                textDay.setTextColorRes(R.color.black)
                textDayOfWeek.setTextColorRes(R.color.gray_light)
            }
        }
    }
}

abstract class BaseWeekViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {

    abstract fun bind(
        item: CalendarView,
        tempDate: String = "",
        updateTempDate: (date: String) -> Unit = { _ -> }
    )
}
