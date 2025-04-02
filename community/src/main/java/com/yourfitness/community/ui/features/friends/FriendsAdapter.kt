package com.yourfitness.community.ui.features.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.features.fitness_calendar.toPx
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.entity.fullName
import com.yourfitness.comunity.databinding.ItemFriendBinding

class FriendsAdapter(
    private val onClick: ((item: FriendsEntity, action: FriendActions, position: Int) -> Unit)? = null,
    private val onCardClick: ((id: String) -> Unit)? = null,
    private val minAvatar: Boolean = false,
    ) : RecyclerView.Adapter<FriendsViewHolder>() {

    val friends = mutableListOf<FriendsEntity>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendsViewHolder(binding, onClick, onCardClick)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val item = friends[position]
        holder.bind(item, minAvatar, position)
    }

    fun setData(friendsList: List<FriendsEntity>) {
        val diffCallBack = FriendsDiffCallback(friends, friendsList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.friends.clear()
        this.friends.addAll(friendsList)
        diffResult.dispatchUpdatesTo(this)
    }
}

class FriendsViewHolder(
    binding: ItemFriendBinding,
    private val onClick: ((item: FriendsEntity, action: FriendActions, position: Int) -> Unit)?,
    private val onCardClick: ((id: String) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemFriendBinding = ItemFriendBinding.bind(itemView)

    fun bind(item: FriendsEntity?, minAvatar: Boolean, pos: Int) {
        if (item == null) {
            binding.root.isVisible = false
            return
        }
        val context = binding.root.context
        if (minAvatar) {
            binding.imagePt.updateLayoutParams<ViewGroup.LayoutParams> {
                height = 40.toPx()
                width = 40.toPx()
            }
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = 0
                marginEnd = 0
            }
            binding.topSeparator.isVisible = pos != 0
        }
        Glide.with(context).load(item.mediaId.toImageUri()).into(binding.imagePt)
        binding.textName.text = item.fullName
        binding.description.text =
            "${context.getString(com.yourfitness.common.R.string.level_number, item.levelNumber)}: " +
                    "${item.progressLevelName}"

        binding.actionGroup.isVisible = item.requestIn
        if (item.isFriend) {
            binding.root.setOnClickListener { onCardClick?.invoke(item.id) }
        }
        binding.firstAction.setOnClickListener {
            onClick?.invoke(item, FriendActions.DECLINE, -1)
            binding.firstAction.isClickable = false
        }
        binding.secondAction.setOnClickListener {
            onClick?.invoke(item, FriendActions.ACCEPT, -1)
            binding.firstAction.isClickable = false
        }
    }
}

private class FriendsDiffCallback(
    private val oldList: List<FriendsEntity>,
    private val newList: List<FriendsEntity>
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
