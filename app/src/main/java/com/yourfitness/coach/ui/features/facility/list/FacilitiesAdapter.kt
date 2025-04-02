package com.yourfitness.coach.ui.features.facility.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.classTypes
import com.yourfitness.coach.data.entity.distance
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.ItemFacilityListBinding
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toImageUri

class FacilitiesAdapter(
    private val items: List<FacilityEntity>,
    private val classification: Classification,
    private val location: LatLng,
    private val onClick: (FacilityEntity) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemFacilityListBinding>>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemFacilityListBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFacilityListBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemFacilityListBinding>, position: Int) {
        val binding = holder.binding
        val item = items[holder.absoluteAdapterPosition]
        binding.root.setOnClickListener { onClick(item) }
        binding.textName.text = item.name?.trim()
        binding.textAddress.text = item.address
        binding.textDistance.text = item.distance(location).formatDistance()
        binding.textLadies.isVisible = item.femaleOnly ?: false
        binding.textDescription.text = when (classification) {
            Classification.GYM -> item.types?.take(3)?.joinToString(", ")
            else -> item.classTypes.sorted().take(3).joinToString(", ")
        }
        val workTime = item.workTimeData
        binding.workTime.root.apply {
            isVisible = workTime != null && classification == Classification.GYM
            if (workTime != null && classification == Classification.GYM) {
                text = binding.root.context.getString(workTime.textId, workTime.args)
                val theme = binding.root.context.theme
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
            }
        }
        Glide.with(binding.root).load(item.icon?.toImageUri()).into(binding.imageIcon)
        Glide.with(binding.root).load(item.gallery?.first()?.toImageUri()).into(binding.imageFacility)
    }
}
