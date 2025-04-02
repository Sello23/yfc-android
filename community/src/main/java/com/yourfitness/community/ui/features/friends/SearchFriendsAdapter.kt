package com.yourfitness.community.ui.features.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.entity.fullName
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.ItemSearchFriendBinding

class SearchFriendsAdapter(
    private val onClick: (item: FriendsEntity, action: FriendActions, position: Int) -> Unit,
    private val onCardClick: (id: String) -> Unit,
) : PagingDataAdapter<FriendsEntity, SearchFriendsViewHolder>(SearchFriendsDiffCallback()) {

    fun updateSearchItem(position: Int, item: FriendsEntity?) {
        if (position < 0 || item == null) return
        peek(position)?.let {
                it.isFriend = item.isFriend
                it.requestIn = item.requestIn
                it.requestOut = item.requestOut
                notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFriendsViewHolder {
        val binding = ItemSearchFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchFriendsViewHolder(binding, onClick, onCardClick)
    }

    override fun onBindViewHolder(holder: SearchFriendsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
    }
}

class SearchFriendsViewHolder(
    binding: ItemSearchFriendBinding,
    private val onClick: (item: FriendsEntity, action: FriendActions, position: Int) -> Unit,
    private val onCardClick: (id: String) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemSearchFriendBinding = ItemSearchFriendBinding.bind(itemView)

    fun bind(item: FriendsEntity?, position: Int) {
        if (item == null) {
            binding.root.isVisible = false
            return
        }
        val context = binding.root.context
        Glide.with(context).load(item.mediaId.toImageUri()).into(binding.imagePt)
        binding.textName.text = item.fullName
        binding.description.text = "${context.getString(com.yourfitness.common.R.string.level_number, item.levelNumber)}: " +
                "${item.progressLevelName}"
        binding.firstAction.text = context.getString(
            if (item.isFriend) R.string.your_friend
            else if (item.requestIn) R.string.decline
            else if (item.requestOut) R.string.friend_request_sent
            else R.string.friend_request
        )

        binding.firstAction.setTextColorRes(
            if (item.requestIn) com.yourfitness.common.R.color.issue_red
            else if (item.isFriend || item.requestOut) com.yourfitness.common.R.color.grey_separator
            else com.yourfitness.common.R.color.main_active
        )

        if (item.isFriend) {
            binding.root.setOnClickListener { onCardClick(item.id) }
        }

        binding.firstAction.setOnClickListener {
            if (item.requestIn) onClick(item, FriendActions.DECLINE, position)
            else if (!item.isFriend && !item.requestOut) onClick(item, FriendActions.SEND_REQUEST, position)
            binding.firstAction.isClickable = false
        }

        binding.secondAction.isVisible = item.requestIn
        binding.secondAction.setOnClickListener {
            onClick(item, FriendActions.ACCEPT, position)
        }
    }
}

class SearchFriendsDiffCallback : DiffUtil.ItemCallback<FriendsEntity>() {

    override fun areItemsTheSame(oldItem: FriendsEntity, newItem: FriendsEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FriendsEntity, newItem: FriendsEntity): Boolean {
        return oldItem == newItem
    }
}
