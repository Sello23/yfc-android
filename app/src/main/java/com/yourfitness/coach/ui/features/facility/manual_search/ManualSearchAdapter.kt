package com.yourfitness.coach.ui.features.facility.manual_search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.ItemManualSearchGymBinding
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.coach.ui.utils.ManualSearchDiffCallback
import com.yourfitness.common.ui.utils.setCompoundDrawables

class ManualSearchAdapter(
    private val onClick: (FacilityEntity) -> Unit,
) : RecyclerView.Adapter<ManualSearchViewHolder>() {

    private var facilities = arrayListOf<FacilityEntity>()
    private var location = LatLng(0.0, 0.0)

    override fun getItemCount() = facilities.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManualSearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemManualSearchGymBinding.inflate(inflater, parent, false)
        return ManualSearchViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ManualSearchViewHolder, position: Int) {
        holder.bind(facilities[position], location)
    }

    fun updateItems(newItems: List<FacilityEntity>, latLng: LatLng) {
        location = latLng
        val diffCallBack = ManualSearchDiffCallback(facilities, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        facilities.clear()
        facilities.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }
}

class ManualSearchViewHolder(
    binding: ItemManualSearchGymBinding,
    private val onClick: (item: FacilityEntity) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemManualSearchGymBinding = ItemManualSearchGymBinding.bind(itemView)
    fun bind(facilities: FacilityEntity, location: LatLng) {
        binding.root.setOnClickListener { onClick(facilities) }
        binding.textName.text = facilities.name?.trim()
        binding.textLadies.isVisible = facilities.femaleOnly ?: false
        binding.textAddress.text = facilities.address
        binding.textDistance.text = SphericalUtil.computeDistanceBetween(facilities.toLatLng(), location).formatDistance()
        Glide.with(binding.root).load(facilities.icon?.toImageUri()).into(binding.imageIcon)
        Glide.with(binding.root).load(facilities.gallery?.first()?.toImageUri()).into(binding.imageFacility)

        val workTime = facilities.workTimeData
        val context = binding.root.context
        binding.workTime.root.apply {
            isVisible = workTime != null
            if (workTime != null) {
                text = context.getString(workTime.textId, workTime.args)
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, context.theme)
            }
        }
    }
}