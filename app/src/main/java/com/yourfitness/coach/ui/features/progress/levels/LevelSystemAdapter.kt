package com.yourfitness.coach.ui.features.progress.levels

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.ItemLevelSystemBinding
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.common.ui.utils.formatProgressAmount
import com.yourfitness.common.ui.utils.toImageUri

class LevelSystemAdapter : RecyclerView.Adapter<LevelSystemViewHolder>() {

    private val visitedClass = arrayListOf<ProgressLevel>()
    private var totalPoints: Long = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelSystemViewHolder {
        val binding =
            ItemLevelSystemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LevelSystemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelSystemViewHolder, position: Int) {
        holder.bind(visitedClass[position], totalPoints, position)
    }

    override fun getItemCount(): Int = visitedClass.size

    fun setData(item: List<ProgressLevel>, totalPoints: Long) {
        visitedClass.clear()
        visitedClass.addAll(item)
        this.totalPoints = totalPoints
    }
}

class LevelSystemViewHolder(
    binding: ItemLevelSystemBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemLevelSystemBinding = ItemLevelSystemBinding.bind(itemView)

    @SuppressLint("SetTextI18n")
    fun bind(level: ProgressLevel, totalPoints: Long, position: Int) {
        if (totalPoints < level.pointLevel) {
            binding.imageLevel.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_level_system_stub
                )
            )
        } else {
            Glide.with(itemView.context).load(level.mediaId.orEmpty().toImageUri())
                .into(binding.imageLevel)
        }

        binding.textLevel.text = itemView.context.getString(com.yourfitness.common.R.string.level_number, position + 1)
        binding.levelName.text = level.name
        binding.coinsAmount.text = itemView.context.resources.getQuantityString(
            R.plurals.coins_plural_format,
            level.coinRewards.toInt(),
            level.coinRewards
        )

        binding.pointsAmount.text = itemView.context.resources.getQuantityString(
            R.plurals.points_plural_format,
            level.coinRewards.toInt(),
            level.pointLevel.formatProgressAmount()
        ) + " = "
    }
}
