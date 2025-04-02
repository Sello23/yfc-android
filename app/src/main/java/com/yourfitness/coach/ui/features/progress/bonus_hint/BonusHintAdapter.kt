package com.yourfitness.coach.ui.features.progress.bonus_hint

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.ItemBonusHintBinding
import com.yourfitness.coach.domain.models.BonusCredits

class BonusHintAdapter(
) : RecyclerView.Adapter<BonusHintViewHolder>() {

    private val bonus = arrayListOf<BonusCredits>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BonusHintViewHolder {
        val binding = ItemBonusHintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BonusHintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BonusHintViewHolder, position: Int) {
        holder.bind(bonus[position])
    }

    override fun getItemCount(): Int = bonus.size

    fun setData(item: List<BonusCredits>) {
        this.bonus.clear()
        this.bonus.addAll(item)
    }
}

class BonusHintViewHolder(
    binding: ItemBonusHintBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemBonusHintBinding = ItemBonusHintBinding.bind(itemView)
    fun bind(item: BonusCredits) {
        binding.textVisits.text = itemView.context.getString(R.string.progress_screen_visits_th_text, item.amount)
        binding.textBonusLevel.text = item.name
        binding.textCredits.text = itemView.context.getString(R.string.progress_screen_credits_text, item.credits)
        binding.imageYfcLogo.setColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_ATOP)
        if (item.isFirst) {
            binding.viewVerticalLine.isVisible = false
        }
        if (item.maxVisits.toInt() >= item.amount.toInt()) {
            binding.viewVerticalLine.setBackgroundColor(Color.parseColor(item.color))
            binding.viewCircle.setColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_ATOP)
        }
    }
}