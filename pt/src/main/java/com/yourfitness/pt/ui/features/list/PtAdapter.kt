package com.yourfitness.pt.ui.features.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.ItemPtListBinding
import com.yourfitness.pt.databinding.ViewPtFacilityInfoBinding
import com.yourfitness.pt.domain.models.TrainerCard

class PersonalTrainerAdapter(
    private val items: List<TrainerCard>,
    private var expandedState: Map<String, Boolean>,
    private val onClick: (TrainerCard) -> Unit,
    private val onBookClick: (TrainerCard) -> Unit,
    private val onExpandedClick: (String, Int) -> Unit,
    private val onPtZeroSessionsClicked: (TrainerCard) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemPtListBinding>>() {

    fun setData(expandedState: Map<String, Boolean>) {
        this.expandedState = expandedState
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemPtListBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPtListBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemPtListBinding>, position: Int) {
        val binding = holder.binding
        val context = holder.itemView.context
        val item = items[holder.absoluteAdapterPosition]
        val isExpanded = expandedState.getOrDefault(item.id, false)
        binding.root.setOnClickListener { onClick(item) }

        binding.bookBtn.isVisible = item.isBookable
        binding.sessionsAmount.isVisible = item.isBookable
        binding.detailsBtn.isVisible = item.isBookable
        binding.detailsBtnIsBookable.isVisible = !item.isBookable

        binding.bookBtn.text = context.getString(
            if (item.availableSessions == 0) R.string.buy_and_book
            else R.string.book
        )
        binding.bookBtn.setOnClickListener { onBookClick(item) }
        binding.detailsBtnIsBookable.setOnClickListener { onClick(item) }

        Glide.with(binding.root).load(item.image.toImageUri()).into(binding.imagePt)
        binding.textName.text = item.name
        binding.description.text = item.focusAreas.joinToString(
            separator = ", ",
            limit = 3,
            truncated = "..."
        )
        binding.facilityInfo.removeAllViews()
        (if (isExpanded) item.facilitiesInfo
        else item.facilitiesInfo.take(3)).map {
            val inflater = LayoutInflater.from(context)
            val listBinding =
                ViewPtFacilityInfoBinding.inflate(inflater, binding.facilityInfo, false)
            listBinding.root.text = context.getString(
                R.string.pt_facility_info, it.first, it.second.formatDistance()
            )
            binding.facilityInfo.addView(listBinding.root)
        }
        val res = context.resources
        binding.sessionsAmount.text =
            if (item.availableSessions == 0) context.getString(R.string.available_slots)
            else res.getQuantityString(
                R.plurals.available_sessions,
                item.availableSessions,
                item.availableSessions
            )
        binding.sessionsAmount.setTextColorRes(
            if (item.availableSessions == 0) com.yourfitness.common.R.color.main_active
            else com.yourfitness.common.R.color.grey
        )
        binding.sessionsAmount.setCompoundDrawables(
            start = if (item.availableSessions == 0) com.yourfitness.common.R.drawable.ic_calendar_blue
            else 0
        )
        if (item.availableSessions == 0) {
            binding.sessionsAmount.setOnClickListener { onPtZeroSessionsClicked(item) }
        }
        binding.listActionBtn.isVisible = item.facilitiesInfo.size > 3
        binding.listActionBtn.setOnClickListener {
            onExpandedClick(item.id, holder.absoluteAdapterPosition)
        }

        binding.listActionBtn.text =
            context.getString(if (isExpanded) R.string.hide else R.string.show_all_gyms)
    }
}
