package com.yourfitness.coach.ui.features.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.coach.databinding.ItemWelcomeSliderBinding
import com.yourfitness.coach.domain.models.Slide

class WelcomeSliderAdapter : RecyclerView.Adapter<SliderViewHolder>() {

    private val slidesList = arrayListOf<Slide>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemWelcomeSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(slidesList[position])
    }

    override fun getItemCount(): Int = slidesList.size

    fun setData(slide: List<Slide>) {
        this.slidesList.clear()
        this.slidesList.addAll(slide)
    }
}

class SliderViewHolder(binding: ItemWelcomeSliderBinding) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemWelcomeSliderBinding = ItemWelcomeSliderBinding.bind(itemView)
    fun bind(slide: Slide) {
        binding.textWelcome.text = slide.welcomeText
        binding.textSubWelcome.text = slide.subWelcomeText

        // Use Glide to load the image
        Glide.with(binding.image.context)
            .load(slide.backgroundImage) // Replace this with the appropriate source (URL, URI, or resource ID)
            .into(binding.image) // Set the image into the ImageView
    }
}