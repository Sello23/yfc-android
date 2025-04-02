package com.yourfitness.coach.ui.features.progress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.databinding.ItemProgressActionBinding
import com.yourfitness.coach.domain.models.ProgressActionCard

class ActionCardGridAdapter: RecyclerView.Adapter<StoryHorizontalViewModel>() {

    private val actionCardList = arrayListOf<ProgressActionCard>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHorizontalViewModel {
        val binding = ItemProgressActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryHorizontalViewModel(binding)
    }

    override fun onBindViewHolder(holder: StoryHorizontalViewModel, position: Int) {
        holder.bind(actionCardList[position])
    }

    override fun getItemCount(): Int = actionCardList.size

    fun setData(faqCard: List<ProgressActionCard>) {
        this.actionCardList.clear()
        this.actionCardList.addAll(faqCard)
    }
}

class StoryHorizontalViewModel(
    binding: ItemProgressActionBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemProgressActionBinding = ItemProgressActionBinding.bind(itemView)

    fun bind(actionCard: ProgressActionCard) {
        itemView.setOnClickListener { actionCard.onClick() }
        binding.textTitle.text = actionCard.title.uppercase()
        binding.backgroundImage.setImageResource(actionCard.backgroundImage)
    }
}
