package com.yourfitness.coach.ui.features.facility.search

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.databinding.ItemFacilitySearchBinding
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.databinding.ItemPtSearchBinding
import com.yourfitness.common.R as common

class SearchAdapter(
    private val items: List<FacilityEntity>,
    private val query: String,
    private val latLng: LatLng,
    private val onClick: (FacilityEntity) -> Unit,
) : RecyclerView.Adapter<BindingHolder<ItemFacilitySearchBinding>>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemFacilitySearchBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFacilitySearchBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemFacilitySearchBinding>, position: Int) {
        val binding = holder.binding
        val resources = binding.root.resources
        val color = resources.getColor(common.color.pale_blue)
        val absolutePosition = holder.absoluteAdapterPosition
        val item = items[absolutePosition]
        binding.root.setOnClickListener { onClick(item) }
        binding.textResults.isVisible = absolutePosition == 0
        binding.textResults.text = resources.getString(common.string.map_screen_results_format, items.size)
        binding.textName.text = highlightSubstring(color, item.name?.trim() ?: "", query.trim())
        binding.textAddress.text = item.address
        binding.textDistance.text = SphericalUtil.computeDistanceBetween(item.toLatLng(), latLng).formatDistance()
        Glide.with(binding.root).load(item.gallery?.first()?.toImageUri()).into(binding.imageFacility)
    }

    private fun highlightSubstring(@ColorInt color: Int, string: String, text: String): CharSequence {
        val index = string.lowercase().indexOf(text.lowercase())
        val result = SpannableStringBuilder(string)
        return if (index >= 0) {
            result.setSpan(
                BackgroundColorSpan(color),
                index,
                index + text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            result
        } else {
            string
        }
    }
}

class PtSearchAdapter(
    private val items: List<PersonalTrainerEntity>,
    private val query: String,
    private val onClick: (PersonalTrainerEntity) -> Unit,
) : RecyclerView.Adapter<BindingHolder<ItemPtSearchBinding>>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemPtSearchBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPtSearchBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemPtSearchBinding>, position: Int) {
        val binding = holder.binding
        val resources = binding.root.resources
        val color = resources.getColor(common.color.pale_blue)
        val absolutePosition = holder.absoluteAdapterPosition
        val item = items[absolutePosition]
        binding.root.setOnClickListener { onClick(item) }
        binding.textResults.isVisible = absolutePosition == 0
        binding.textResults.text = resources.getString(common.string.map_screen_results_format, items.size)
        binding.textName.text = highlightSubstring(color, item.fullName.trim(), query.trim())
        binding.description.text = item.focusAreas?.joinToString(
            separator = ", ",
            limit = 5,
            truncated = "..."
        )
        Glide.with(binding.root).load(item.mediaId?.toImageUri()).into(binding.imagePt)
    }

    private fun highlightSubstring(@ColorInt color: Int, string: String, text: String): CharSequence {
        val index = string.lowercase().indexOf(text.lowercase())
        val result = SpannableStringBuilder(string)
        return if (index >= 0) {
            result.setSpan(
                BackgroundColorSpan(color),
                index,
                index + text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            result
        } else {
            string
        }
    }
}
