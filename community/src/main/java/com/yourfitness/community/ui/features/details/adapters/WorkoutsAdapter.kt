package com.yourfitness.community.ui.features.details.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.formatted
import com.yourfitness.common.domain.date.secToMinutes
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toDateDMmmYyyy
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.utils.stringCommaSeparated
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.community.data.entity.fullName
import com.yourfitness.community.network.dto.WorkoutInfoDto
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.ItemFriendBinding
import com.yourfitness.comunity.databinding.ItemWorkoutDayInfoBinding

class WorkoutsAdapter(private val onClick: (id: String, liked: Boolean) -> Unit) :
    RecyclerView.Adapter<WorkoutsViewHolder>() {
    private val workouts = mutableListOf<WorkoutInfoDto>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutsViewHolder {
        val binding = ItemWorkoutDayInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutsViewHolder(binding, onClick, ::onLikeUpdated)
    }

    override fun getItemCount(): Int = workouts.size

    override fun onBindViewHolder(holder: WorkoutsViewHolder, position: Int) {
        val item = workouts[position]
        holder.bind(item, position)
    }

    fun setData(friendsList: List<WorkoutInfoDto>) {
        val diffCallBack = WorkoutsDiffCallback(workouts, friendsList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.workouts.clear()
        this.workouts.addAll(friendsList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun onLikeUpdated(position: Int, isLiked: Boolean, likesCount: Long) {
        val updatedItem = workouts[position].copy(
            isLiked = isLiked,
            likesCount = likesCount
        )
        workouts[position] = updatedItem
    }
}

class WorkoutsViewHolder(
    binding: ItemWorkoutDayInfoBinding,
    private val onClick: (id: String, liked: Boolean) -> Unit,
    private val onLikeUpdated: (position: Int, isLiked: Boolean, likesCount: Long) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemWorkoutDayInfoBinding = ItemWorkoutDayInfoBinding.bind(itemView)

    fun bind(item: WorkoutInfoDto?, position: Int) {
        if (item == null) {
            binding.root.isVisible = false
            return
        }
        binding.date.text = item.date?.formatted().orEmpty()
        binding.likeIcon.isSelected = item.isLiked == true
        binding.likesAmount.text = (item.likesCount ?: 0).toString()
        binding.likesAmount.isVisible = (item.likesCount ?: 0) > 0
        val minutes = item.duration?.secToMinutes() ?: 0
        binding.info.durationValue.text = minutes.stringCommaSeparated
        binding.info.durationLabel.text =
            binding.root.context.resources.getQuantityString(R.plurals.minutes, minutes)
        binding.info.stepsValue.text = (item.steps ?: 0).stringCommaSeparated
        binding.info.caloriesValue.text = (item.calories ?: 0).stringCommaSeparated
        binding.likeIcon.setOnClickListener {
            var likes = binding.likesAmount.text.toString().toLong()

            binding.likeIcon.isSelected = !binding.likeIcon.isSelected
            val isLiked = if (binding.likeIcon.isSelected) {
                likes++
                true
            } else {
                likes--
                false
            }
            onLikeUpdated(position, isLiked, likes)
            item.id?.let { it1 -> onClick(it1, isLiked) }
            binding.likesAmount.text = likes.toString()
            binding.likesAmount.isVisible = likes > 0
        }
    }
}

private class WorkoutsDiffCallback(
    private val oldList: List<WorkoutInfoDto>,
    private val newList: List<WorkoutInfoDto>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].date == newList[newItemPosition].date
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
