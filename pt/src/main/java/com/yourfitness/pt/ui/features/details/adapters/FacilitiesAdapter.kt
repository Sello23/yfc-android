package com.yourfitness.pt.ui.features.details.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.databinding.ItemFacilityInfoBinding
import com.yourfitness.pt.domain.models.FacilityInfo

class FacilitiesAdapter : RecyclerView.Adapter<FacilityInfoViewHolder>() {

    private val facilities = arrayListOf<FacilityInfo>()
    private var location: LatLng = LatLng(0.0, 0.0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityInfoViewHolder {
        val binding = ItemFacilityInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FacilityInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FacilityInfoViewHolder, position: Int) {
        holder.bind(facilities[position], location)
    }

    override fun getItemCount(): Int = facilities.size

    fun setData(challengesList: List<FacilityInfo>, location: LatLng) {
        this.facilities.clear()
        this.facilities.addAll(challengesList)
        this.location = location
    }
}

class FacilityInfoViewHolder(binding: ItemFacilityInfoBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemFacilityInfoBinding = ItemFacilityInfoBinding.bind(itemView)

    fun bind(item: FacilityInfo, location: LatLng) {
        binding.textName.text = item.name
        binding.textAddress.text = item.address
        Glide.with(binding.root).load(item.image.toImageUri()).into(binding.imagePt)
        val itemLocation = LatLng(item.latitude, item.longitude)
        binding.distanceTo.text =
            SphericalUtil.computeDistanceBetween(itemLocation, location).formatDistance().orEmpty()
    }
}
