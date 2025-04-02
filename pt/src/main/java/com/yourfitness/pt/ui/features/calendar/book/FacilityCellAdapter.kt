package com.yourfitness.pt.ui.features.calendar.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.databinding.ItemFacilityCellBinding
import com.yourfitness.pt.domain.models.FacilityInfo

class FacilityCellAdapter(
    private val onCellClick: (id: String, pos: Int) -> Unit,
) : RecyclerView.Adapter<FacilityCellViewHolder>() {

    private val facilities = arrayListOf<FacilityInfo>()
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityCellViewHolder {
        val binding =
            ItemFacilityCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FacilityCellViewHolder(binding, onCellClick)
    }

    override fun onBindViewHolder(holder: FacilityCellViewHolder, position: Int) {
        holder.bind(facilities[position], selectedId)
    }

    override fun getItemCount(): Int = facilities.size

    fun setData(challengesList: List<FacilityInfo>, selectedId: String? = null) {
        this.facilities.clear()
        this.facilities.addAll(challengesList)
        this.selectedId = selectedId
    }

    fun setSelectedId(selectedId: String?) {
        this.selectedId = selectedId
    }
}

class FacilityCellViewHolder(
    binding: ItemFacilityCellBinding,
    private val onCellClick: (id: String, pos: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemFacilityCellBinding = ItemFacilityCellBinding.bind(itemView)

    fun bind(item: FacilityInfo, selectedId: String?) {
        val isSelected = item.id == selectedId
        binding.apply {
            root.isClickable = !isSelected
            root.isEnabled = item.workTimeData?.isAccessible == true
            textName.isSelected = isSelected
            textAddress.isSelected = isSelected
            textName.setTextAppearance(
                binding.root.context,
                if (isSelected) com.yourfitness.common.R.style.TextAppearance_YFC_Heading2
                else com.yourfitness.common.R.style.TextAppearance_YFC_Input
            )
            root.isSelected = isSelected
            textName.text = item.name
            textAddress.text = item.address
            Glide.with(root).load(item.image.toImageUri()).into(imageFacility)
            root.setOnClickListener { onCellClick(item.id, layoutPosition) }

            val workTime = item.workTimeData
            workTimeContainer.root.apply {
                isVisible = workTime != null
                if (workTime != null) {
                    text = binding.root.context.getString(workTime.textId, workTime.args)
                    val theme = binding.root.context.theme
                    background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
                }
            }
        }
    }
}
