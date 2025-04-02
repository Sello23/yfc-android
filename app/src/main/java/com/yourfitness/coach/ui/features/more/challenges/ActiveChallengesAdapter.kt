package com.yourfitness.coach.ui.features.more.challenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.coach.ui.features.more.challenges.ChallengesFragment.Companion.JOIN_DETAILS
import com.yourfitness.coach.ui.features.more.challenges.ChallengesFragment.Companion.JOIN_DETAILS_PRIVATE
import com.yourfitness.coach.ui.features.more.challenges.ChallengesFragment.Companion.JOIN_PRIVATE
import com.yourfitness.common.databinding.ItemChallengeBinding
import com.yourfitness.common.domain.date.toDateDMmmYyyy
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.adapters.ChallengesDiffCallback
import com.yourfitness.common.ui.utils.toImageUri
import java.util.*

class ActiveChallengesAdapter(
    private val onItemClick: (intent: String, challenge: Challenge) -> Unit
) : RecyclerView.Adapter<ActiveChallengesViewHolder>() {

    private val challenges = arrayListOf<Challenge>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveChallengesViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActiveChallengesViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ActiveChallengesViewHolder, position: Int) {
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

class ActiveChallengesViewHolder(
    binding: ItemChallengeBinding,
    private val onItemClick: (intent: String, challenge: Challenge) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemChallengeBinding = ItemChallengeBinding.bind(itemView)
    fun bind(item: Challenge) {
        val date = Calendar.getInstance().timeInMillis
        if (item.private == true) {
            if (date > (item.startDate?.toMilliseconds() ?: 0) && date < (item.endDate?.toMilliseconds() ?: 0)) {
                binding.textChallengeStateOngoingPrivate.isVisible = true
            } else {
                binding.textChallengeStatePrivate.isVisible = true
            }
            binding.textJoinPrivate.isVisible = true
            binding.textJoinPrivate.setOnClickListener { onItemClick(JOIN_PRIVATE, item) }
            itemView.setOnClickListener { onItemClick(JOIN_DETAILS_PRIVATE, item) }
        } else {
            binding.textJoin.isVisible = true
            binding.textJoin.setOnClickListener { onItemClick(binding.textJoin.text.toString(), item) }
            itemView.setOnClickListener { onItemClick(JOIN_DETAILS, item) }
            if (date > (item.startDate?.toMilliseconds() ?: 0) && date < (item.endDate?.toMilliseconds() ?: 0)) {
                binding.textChallengeStateOngoing.isVisible = true
            }
        }
        binding.textChallengeName.text = item.name
        Glide.with(binding.root).load(item.imageID?.toImageUri()).into(binding.imageChallenge)
        binding.textStartDate.text = item.startDate?.toDateDMmmYyyy()
        binding.textFinishDate.text = item.endDate?.toDateDMmmYyyy()
    }
}