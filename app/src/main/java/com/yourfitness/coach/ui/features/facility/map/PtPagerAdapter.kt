package com.yourfitness.coach.ui.features.facility.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.databinding.ItemPtPagerBinding

class PtPagerAdapter(
    private val trainers: PtShortcut,
    private val onPageChange: (Int) -> Unit,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemPtPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding, onPageChange, onClick)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(
            trainers.entries.elementAt(position),
            position == 0,
            position == trainers.size - 1,
            position
        )
    }

    override fun getItemCount(): Int = trainers.size
}

class SliderViewHolder(
    binding: ItemPtPagerBinding,
    private val onPageChange: (Int) -> Unit,
    private val onClick: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemPtPagerBinding = ItemPtPagerBinding.bind(itemView)

    fun bind(
        pt: Map.Entry<String, Triple<String, List<String>, String>>,
        isFirst: Boolean,
        isLast: Boolean,
        pos: Int
    ) {
        binding.arrowLeft.isInvisible = isFirst
        binding.arrowRight.isInvisible = isLast
        binding.arrowLeft.setOnClickListener { onPageChange(pos - 1) }
        binding.arrowRight.setOnClickListener { onPageChange(pos + 1) }
        binding.root.setOnClickListener { onClick(pt.key) }
        Glide.with(binding.root).load(pt.value.first.toImageUri()).into(binding.imagePt)
        binding.textName.text = pt.value.third
        binding.description.text = pt.value.second.joinToString(
            separator = ", ",
            limit = 5,
            truncated = "..."
        )
    }
}
