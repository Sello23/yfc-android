package com.yourfitness.coach.ui.features.facility.dialog.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.R
import com.yourfitness.common.R as common
import com.yourfitness.coach.data.entity.Timetable
import com.yourfitness.coach.databinding.DialogWorkHoursBinding
import com.yourfitness.coach.databinding.ItemWorkHoursBinding
import com.yourfitness.coach.domain.date.todayWeekday
import com.yourfitness.coach.ui.utils.getLocalizedWeekday
import com.yourfitness.coach.ui.utils.getWeekdayValue
import com.yourfitness.common.domain.date.toDateTimeUtc0
import com.yourfitness.common.domain.models.WorkTimeDto
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.ui.constants.Constants.Companion.TIMETABLE

class WorkHoursDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogWorkHoursBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(common.string.access_times),
            dismissId = R.id.close
        )
        val timetable = arguments?.get(TIMETABLE) as Timetable?
        binding.timetable.adapter = TimetableAdapter(timetable, todayWeekday())
        binding.buttonAction.setOnClickListener { dismiss() }
    }

    inner class TimetableAdapter(
        private val items: List<WorkTimeDto>?,
        private val nowWeekday: Int
    ) : RecyclerView.Adapter<BindingHolder<ItemWorkHoursBinding>>() {

        override fun getItemCount() = items?.size ?: 0

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BindingHolder<ItemWorkHoursBinding> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemWorkHoursBinding.inflate(inflater, parent, false)
            return BindingHolder(binding)
        }

        override fun onBindViewHolder(holder: BindingHolder<ItemWorkHoursBinding>, position: Int) {
            val binding = holder.binding
            val workTime = items?.get(holder.absoluteAdapterPosition)
            val from = workTime?.from
            val to = workTime?.to
            binding.weekday.text =
                workTime?.weekDay?.let { binding.root.context.getLocalizedWeekday(it) }
            binding.timeInterval.text =
                if (from != null && to != null) {
                    "${from.toDateTimeUtc0()} - ${to.toDateTimeUtc0()}"
                } else {
                    getString(common.string.no_access)
                }
            if (nowWeekday == workTime?.weekDay.getWeekdayValue()) {
                binding.weekday.setTextAppearance(
                    requireContext(),
                    com.yourfitness.common.R.style.TextAppearance_YFC_Heading2
                )
                binding.timeInterval.setTextAppearance(
                    requireContext(),
                    com.yourfitness.common.R.style.TextAppearance_YFC_Heading2
                )
            }
        }
    }
}
