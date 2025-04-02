package com.yourfitness.coach.ui.features.more.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.ItemCreditBinding
import com.yourfitness.coach.network.dto.Bonuses

class HowToBuyCreditsStoryAdapter : RecyclerView.Adapter<HowToBuyCreditsStoryViewHolder>() {

    private val data = arrayListOf<Bonuses>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HowToBuyCreditsStoryViewHolder {
        val binding = ItemCreditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HowToBuyCreditsStoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HowToBuyCreditsStoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: List<Bonuses>?) {
        this.data.clear()
        data?.let { this.data.addAll(it) }
    }
}

class HowToBuyCreditsStoryViewHolder(binding: ItemCreditBinding) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemCreditBinding = ItemCreditBinding.bind(itemView)
    fun bind(data: Bonuses) {
        binding.textNumberCredits.text = itemView.context.getString(R.string.story_screen_number_credits_text, data.credits)
        binding.textNumberVisit.text = itemView.context.getString(R.string.story_screen_number_visit_text, data.amount)
    }
}