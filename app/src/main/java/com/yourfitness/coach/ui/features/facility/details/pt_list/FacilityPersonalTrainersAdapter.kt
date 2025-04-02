package com.yourfitness.coach.ui.features.facility.details.pt_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.ItemFacilityDetailPtListBinding
import com.yourfitness.pt.databinding.ViewPtFacilityInfoBinding
import com.yourfitness.pt.domain.models.TrainerCard

class FacilityPersonalTrainersAdapter(
    private val items: List<TrainerCard>,
    private var expandedState: Map<String, Boolean>,
    private val onClick: (TrainerCard) -> Unit,
    private val onExpandedClick: (String, Int) -> Unit,
) : RecyclerView.Adapter<BindingHolder<ItemFacilityDetailPtListBinding>>() {

    fun setData(expandedState: Map<String, Boolean>) {
        this.expandedState = expandedState
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemFacilityDetailPtListBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFacilityDetailPtListBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BindingHolder<ItemFacilityDetailPtListBinding>,
        position: Int
    ) {
        val binding = holder.binding
        val context = holder.itemView.context
        val item = items[holder.absoluteAdapterPosition]
        val isExpanded = expandedState.getOrDefault(item.id, false)
        binding.root.setOnClickListener { onClick(item) }
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

        binding.listActionBtn.isVisible = item.facilitiesInfo.size > 3
        binding.listActionBtn.setOnClickListener {
            onExpandedClick(item.id, holder.absoluteAdapterPosition)
        }

        binding.listActionBtn.text =
            context.getString(if (isExpanded) R.string.hide else R.string.show_all_gyms)
    }
}
