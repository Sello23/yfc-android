package com.yourfitness.coach.ui.features.more.faq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.databinding.ItemFaqBinding
import com.yourfitness.coach.domain.models.FAQCard

class FAQAdapter(
    private val onCardClick: (cardTitle: String, isEnabled: Boolean) -> Unit
) : RecyclerView.Adapter<FAQViewHolder>() {

    private val faqCardList = arrayListOf<FAQCard>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FAQViewHolder(binding, onCardClick)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(faqCardList[position])
    }

    override fun getItemCount(): Int = faqCardList.size

    fun setData(faqCard: List<FAQCard>) {
        this.faqCardList.clear()
        this.faqCardList.addAll(faqCard)
    }
}

class FAQViewHolder(
    binding: ItemFaqBinding,
    private val onCardClick: (cardTitle: String, isEnabled: Boolean) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemFaqBinding = ItemFaqBinding.bind(itemView)
    fun bind(faqCard: FAQCard) {
        itemView.setOnClickListener { onCardClick(faqCard.title, faqCard.comingSoonBg == null) }
        binding.textTitle.text = faqCard.title
        binding.backgroundImage.setImageResource(faqCard.backgroundImage)
        faqCard.image?.let { binding.image.setImageResource(it) }
    }
}