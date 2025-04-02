package com.yourfitness.common.ui.features.payments.buy_options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemBuyOptionBinding
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.ui.utils.BuyOptionsDiffCallback
import com.yourfitness.common.ui.utils.formatAmount

class CreditsAdapter(
    private val onItemClick: (BuyOptionData) -> Unit
) : ListAdapter<BuyOptionData, CreditViewHolder>(BuyOptionsDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_buy_option
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return CreditViewHolder(ItemBuyOptionBinding.bind(view), onItemClick)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CreditViewHolder(
    private val binding: ItemBuyOptionBinding,
    private val onItemClick: (BuyOptionData) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BuyOptionData) {
        with(binding) {
            rbState.isChecked = item.isSelected
            tvPlan.text = item.plan
            tvAmount.text = item.amountString
            tvPrice.text = item.price.formatAmount(item.currency)
            root.isSelected = item.isSelected
            root.setOnClickListener { onItemClick(item) }
        }
    }
}
