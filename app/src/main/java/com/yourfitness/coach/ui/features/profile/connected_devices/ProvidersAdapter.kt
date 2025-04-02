package com.yourfitness.coach.ui.features.profile.connected_devices

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.ItemFitnessProviderBinding
import com.yourfitness.coach.ui.utils.toPx
import com.yourfitness.comunity.databinding.ItemFriendBinding

class ProvidersAdapter(
    private val onClick: (type: ProviderType, selected: Boolean) -> Unit,
) : RecyclerView.Adapter<ProvidersViewHolder>() {

    private val providers = mutableListOf<ProviderInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvidersViewHolder {
        val binding =
            ItemFitnessProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProvidersViewHolder(binding, onClick)
    }

    override fun getItemCount(): Int = providers.size

    override fun onBindViewHolder(holder: ProvidersViewHolder, position: Int) {
        val item = providers[position]
        holder.bind(item)
    }

    fun setData(providersList: List<ProviderInfo>) {
        val diffCallBack = ProvidersDiffCallback(providers, providersList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.providers.clear()
        this.providers.addAll(providersList)
        diffResult.dispatchUpdatesTo(this)
    }
}

class ProvidersViewHolder(
    binding: ItemFitnessProviderBinding,
    private val onClick: (type: ProviderType, selected: Boolean) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemFitnessProviderBinding = ItemFitnessProviderBinding.bind(itemView)

    fun bind(item: ProviderInfo) {
        val context = binding.root.context

        binding.providerBorder.strokeWidth = if (!item.hasBorder || !item.isEnabled) {
            0
        } else {
            1.toPx() / 2
        }

        binding.providerLogo.setImageResource(item.logo)

        binding.providerName.text = context.getString(item.name)
        binding.providerName.isEnabled = item.isEnabled

        binding.switchView.isChecked = item.checked
        binding.switchView.isEnabled = item.isEnabled

        binding.root.isEnabled = item.isEnabled
        binding.root.setOnClickListener {
            onClick(item.type, !binding.switchView.isChecked)
        }

        if (!item.isEnabled) {
            val matrix = ColorMatrix().apply {
                setSaturation(0f)
            }
            val filter = ColorMatrixColorFilter(matrix)
            binding.providerLogo.colorFilter = filter
            binding.providerLogo.alpha = 0.5f

        } else {
            binding.providerLogo.clearColorFilter()
            binding.providerLogo.alpha = 1f
        }
    }
}

private class ProvidersDiffCallback(
    private val oldList: List<ProviderInfo>,
    private val newList: List<ProviderInfo>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].type == newList[newItemPosition].type
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
