package com.yourfitness.community.ui.features.likes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.toDateDMmmYyyyUtc
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.community.network.dto.FriendsDto
import com.yourfitness.community.network.dto.fullName
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.ItemFriendLikeBinding

class LikesAdapter(
    private val workoutDate: Int,
    private val onClick: ((friendId: String) -> Unit)? = null,
) : RecyclerView.Adapter<LikesViewHolder>() {

    private val friends = mutableListOf<FriendsDto>()
    private val fitnessData: FitnessData = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val binding = ItemFriendLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikesViewHolder(binding, workoutDate, onClick)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        val item = friends[position]
        holder.bind(item, position, fitnessData)
    }

    fun setData(friendsList: List<FriendsDto>, fitnessData: FitnessData) {
        val diffCallBack = LikesDiffCallback(friends, friendsList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.friends.clear()
        this.friends.addAll(friendsList)
        diffResult.dispatchUpdatesTo(this)

        this.fitnessData.clear()
        this.fitnessData.putAll(fitnessData)
    }
}

class LikesViewHolder(
    binding: ItemFriendLikeBinding,
    private val workoutDate: Int,
    private val onClick: ((friendId: String) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemFriendLikeBinding = ItemFriendLikeBinding.bind(itemView)

    fun bind(item: FriendsDto?, position: Int, fitnessData: FitnessData) {
        if (item == null) {
            binding.root.isVisible = false
            return
        }
        val context = binding.root.context

        binding.root.setOnClickListener { item.friendId?.let { friendId -> onClick?.invoke(friendId) } }
        binding.topSeparator.isVisible = position > 0
        Glide.with(context).load(item.mediaId?.toImageUri()).into(binding.imageFriend)
        binding.textName.text = item.fullName
        val date = item.workoutDate
        if (date != null) {
            binding.description.text = date.toDateDMmmYyyyUtc()
            fitnessData[date]?.let {
                binding.steps.text = context.resources.getQuantityString(
                    R.plurals.steps,
                    it.first.toInt(),
                    it.first
                )
                binding.calories.text = context.getString(R.string.kcal_value, it.second)
            }
        }
        binding.newLikeGroup.isVisible = item.viewed == false
        binding.activityInfoGroup.isVisible = item.viewed == false && workoutDate.toLong() != date
    }
}

private class LikesDiffCallback(
    private val oldList: List<FriendsDto>,
    private val newList: List<FriendsDto>
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
