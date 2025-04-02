package com.yourfitness.common.ui.features.fitness_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.databinding.ViewFacilityFullInfoCardBinding
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.utils.toImageUri
import java.util.*

enum class MonthAdapterType(val type: Int) {
    STUDIO(0),
    PT(1)
}

abstract class BaseMonthAdapter<VH: AbstractMonthViewHolder>: RecyclerView.Adapter<VH>() {

    private val bookedClasses = mutableListOf<CalendarView.CalendarItem>()

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(bookedClasses[position])
    }

    override fun getItemCount(): Int = bookedClasses.size

    override fun getItemViewType(position: Int): Int {
        return if (bookedClasses[position].status != null) {
            val item = bookedClasses[position] as CalendarView.CalendarItem
            if (item.status == "blocked_slot") CalendarAdapterType.BLOCK.type
            else CalendarAdapterType.BODY.type
        } else MonthAdapterType.STUDIO.type
    }

    fun setData(newItems: List<CalendarView.CalendarItem>) {
        bookedClasses.clear()
        bookedClasses.addAll(newItems)
        notifyDataSetChanged()
    }
}

open class BaseMonthViewHolder(
    binding: ViewFacilityFullInfoCardBinding,
) : AbstractMonthViewHolder(binding.root) {

    protected val binding: ViewFacilityFullInfoCardBinding = ViewFacilityFullInfoCardBinding.bind(itemView)
    protected val today = Calendar.getInstance().timeInMillis
    override fun bind(item: CalendarView.CalendarItem) {
        binding.apply {
            title.text = item.objectName
            subtitle.text = item.coachName.ifEmpty { item.labelBuilder?.let { it(binding.root.context) } }
            info.text = item.facilityName
            address.text = item.address
            Glide.with(root.context).load(item.icon.toImageUri()).into(imageIcon)
        }
    }
}

abstract class AbstractMonthViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {

    abstract fun bind(item: CalendarView.CalendarItem)
}
