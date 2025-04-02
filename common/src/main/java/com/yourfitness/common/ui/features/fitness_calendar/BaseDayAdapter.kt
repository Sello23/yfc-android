package com.yourfitness.common.ui.features.fitness_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.databinding.ItemPaymentHistoryHeaderBinding
import com.yourfitness.common.databinding.ViewFitnessCalendarDayBinding
import com.yourfitness.common.domain.date.formatEeeeMmmDd
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.models.CalendarView

abstract class BaseDayAdapter<VH : BaseDayViewHolder> : RecyclerView.Adapter<VH>() {

    val currentList = mutableListOf<CalendarView>()

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
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.size


    fun setData(newItems: List<CalendarView>) {
        currentList.clear()
        currentList.addAll(newItems)
        notifyDataSetChanged()
    }
}

class DayHeaderViewHolder(binding: ItemPaymentHistoryHeaderBinding) :
    BaseDayViewHolder(binding.root) {
    private val binding: ItemPaymentHistoryHeaderBinding = ItemPaymentHistoryHeaderBinding.bind(itemView)

    override fun bind(item: CalendarView) {
        item as CalendarView.Header
        binding.textHeaderDate.text = item.startDate.toDate().formatEeeeMmmDd()
        binding.root.setPadding(0, 0, 0, 10.toPx())
    }
}


open class BodyDayViewHolder(bindingView: ViewFitnessCalendarDayBinding) :
    BaseDayViewHolder(bindingView.root) {
    protected val binding: ViewFitnessCalendarDayBinding = ViewFitnessCalendarDayBinding.bind(itemView)

    override fun bind(item: CalendarView) {
        item as CalendarView.CalendarItem
    }
}

abstract class BaseDayViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {
    abstract fun bind(item: CalendarView)
}
