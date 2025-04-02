package com.yourfitness.common.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.R
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.databinding.ItemChallengeBinding
import com.yourfitness.common.domain.date.toDateDMmmYyyy
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.utils.toImageUri
import java.util.*

class MyChallengesAdapter(
    private val onItemClick: (intent: String, challenge: Challenge) -> Unit
) : RecyclerView.Adapter<MyChallengesViewHolder>() {

    private val challenges = arrayListOf<Challenge>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChallengesViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyChallengesViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MyChallengesViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size

    fun setData(challengesList: List<Challenge>) {
        val diffCallBack = ChallengesDiffCallback(challenges, challengesList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.challenges.clear()
        this.challenges.addAll(challengesList)
        diffResult.dispatchUpdatesTo(this)
    }
}

class MyChallengesViewHolder(
    binding: ItemChallengeBinding,
    private val onItemClick: (intent: String, challenge: Challenge) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemChallengeBinding = ItemChallengeBinding.bind(itemView)
    fun bind(item: Challenge) {
        binding.textLeaderboard.isVisible = true
        val date = Calendar.getInstance().timeInMillis
        if (date > (item.startDate?.toMilliseconds() ?: 0)) {
            binding.textLeaderboard.text = itemView.context.getString(R.string.challenges_screen_leaderboard_text)
        } else {
            binding.textLeaderboard.text = itemView.context.getString(R.string.challenges_screen_details_text)
        }
        when {
            date > (item.startDate?.toMilliseconds() ?: 0) && date < (item.endDate?.toMilliseconds() ?: 0) && item.private == true -> {
                binding.textChallengeStateOngoingPrivate.isVisible = true
            }
            date > (item.startDate?.toMilliseconds() ?: 0) && date < (item.endDate?.toMilliseconds() ?: 0) -> {
                binding.textChallengeStateOngoing.isVisible = true
            }
            date < (item.startDate?.toMilliseconds() ?: 0) && item.private == true -> {
                binding.textChallengeStateDetailsPrivate.isVisible = true
            }
        }
        binding.textLeaderboard.setOnClickListener { onItemClick(binding.textLeaderboard.text.toString(), item) }
        itemView.setOnClickListener { onItemClick(DETAILS, item) }
        binding.textChallengeName.text = item.name
        Glide.with(binding.root).load(item.imageID?.toImageUri()).into(binding.imageChallenge)
        binding.textStartDate.text = item.startDate?.toDateDMmmYyyy()
        binding.textFinishDate.text = item.endDate?.toDateDMmmYyyy()
    }
}

class ChallengesDiffCallback(
    private val oldList: List<Challenge>,
    private val newList: List<Challenge>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

const val DETAILS = "Details"
