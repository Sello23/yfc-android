package com.yourfitness.coach.ui.features.more.challenges.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.data.entity.fullName
import com.yourfitness.coach.databinding.ItemLeaderboardBinding
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.ui.utils.LeaderboardDiffCallback
import com.yourfitness.coach.ui.utils.LeaderboardUtils
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.utils.toStringNoDecimal
import com.yourfitness.common.R as common

//class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardViewHolder>() {
//
//    private val entries = arrayListOf<LeaderboardEntity>()
//    private var checkId = ""
//    private var measurement = ""
//    private var needPodiumBg = true
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
//        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return LeaderboardViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
//        holder.bind(entries[position], needPodiumBg, checkId, entries.size, measurement)
//    }
//
//    override fun getItemCount(): Int = entries.size
//
//    fun setData(
//        leaderboard: List<LeaderboardEntity>,
//        checkId: String,
//        measurement: String,
//        needPodiumBg: Boolean = true
//    ) {
//        this.measurement = measurement
//        this.checkId = checkId
//        this.needPodiumBg = needPodiumBg
//        val diffCallBack = LeaderboardDiffCallback(entries, leaderboard)
//        val diffResult = DiffUtil.calculateDiff(diffCallBack)
//        this.entries.clear()
//        this.entries.addAll(leaderboard)
//        diffResult.dispatchUpdatesTo(this)
//    }
//
//    companion object {
//        const val CALORIES = "calories"
//        const val STEPS = "steps"
//        const val POINTS = "points"
//    }
//}

class LeaderboardAdapter2(
    private val checkId: String,
    private val measurement: String,
    private val leaderboardId: LeaderboardId,
    private val needPodiumBg: Boolean
) : PagingDataAdapter<LeaderboardEntity, LeaderboardViewHolder>(LeaderboardDiffCallback2()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(leaderboardId, binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, needPodiumBg, checkId, itemCount, measurement)
    }

    companion object {
        const val CALORIES = "calories"
        const val STEPS = "steps"
        const val POINTS = "points"
    }
}

class LeaderboardViewHolder(
    private val leaderboardId: LeaderboardId,
    binding: ItemLeaderboardBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemLeaderboardBinding = ItemLeaderboardBinding.bind(itemView)

    private val podiumDataMap = mapOf(
        1 to PodiumData(R.drawable.image_gold_medal, common.color.pale_yellow),
        2 to PodiumData(R.drawable.image_silver_medal, common.color.grey_separator),
        3 to PodiumData(R.drawable.image_bronze_medal, common.color.pale_orange),
    )

    fun bind(item: LeaderboardEntity?, needPodiumBg: Boolean, checkId: String, size: Int, measurement: String) {
        if (item == null) {
            binding.root.isVisible = false
            return
        }

        val uiData: PodiumData = podiumDataMap[item.rank] ?: PodiumData(
            bgColor = common.color.white,
            bgColorBack = common.color.transparent
        )

        binding.apply {
            imageMedal.isVisible = uiData.medalRes != null
            constraintLayoutBack.setBackgroundColor(
                ResourcesCompat.getColor(
                    itemView.resources,
                    if (needPodiumBg) uiData.bgColorBack else common.color.transparent,
                    null
                )
            )
            viewSeparator.isVisible = needPodiumBg && item.rank < 4 &&
                    ((size == item.rank) || (size > 3 && item.rank == 3))
            uiData.medalRes?.let { imageMedal.setImageResource(it) }
            textRank.text = item.rank.toString()
            textScore.text = LeaderboardUtils.getScore(itemView.context, item.score, measurement)
            Glide.with(root).load(item.mediaId.toImageUri()).into(imageUser)
        }

        if (item.profileId == checkId) {
            setCurrentUser(item)
        } else {
            setDefaultUser(item, uiData.bgColor)
        }
    }

    private fun setDefaultUser(item: LeaderboardEntity, bgColor: Int) {
        binding.constraintLayout.setBackgroundColor(ResourcesCompat.getColor(itemView.resources, bgColor, null))
        binding.textName.text = item.fullName
        binding.textName.setTextColorRes(common.color.black)
        binding.textScore.setTextColorRes(common.color.grey)
        binding.textRank.setTextColorRes(common.color.black)

    }

    private fun setCurrentUser(item: LeaderboardEntity) {
        binding.constraintLayout.background = ResourcesCompat.getDrawable(itemView.resources, common.drawable.gradient_blue, null)
        val userLabel = if (leaderboardId == LeaderboardId.FRIEND) {
            item.fullName
        } else {
            itemView.context.getString(R.string.leaderboard_screen_name_text, item.fullName)
        }
        binding.textName.text = userLabel
        binding.textName.setTextColorRes(common.color.white)
        binding.textScore.setTextColorRes(common.color.white)
        binding.textRank.setTextColorRes(common.color.white)
    }
}

private data class PodiumData(
    @DrawableRes val medalRes: Int? = null,
    @ColorRes val bgColor: Int,
    @ColorRes val bgColorBack: Int = common.color.white,
)

class LeaderboardDiffCallback2 : DiffUtil.ItemCallback<LeaderboardEntity>() {

    override fun areItemsTheSame(oldItem: LeaderboardEntity, newItem: LeaderboardEntity): Boolean {
        return if (oldItem.profileId.isNotBlank() || newItem.profileId.isNotBlank()) oldItem.profileId == newItem.profileId
        else oldItem.corporationId == newItem.corporationId
    }

    override fun areContentsTheSame(oldItem: LeaderboardEntity, newItem: LeaderboardEntity): Boolean {
        return oldItem == newItem
    }
}