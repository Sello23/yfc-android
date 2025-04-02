package com.yourfitness.common.ui.utils

import androidx.recyclerview.widget.DiffUtil
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.domain.models.PaymentOption

class PaymentOptionsDiffCallback : DiffUtil.ItemCallback<PaymentOption>() {

    override fun areItemsTheSame(oldItem: PaymentOption, newItem: PaymentOption): Boolean {
        return oldItem.method == newItem.method
    }

    override fun areContentsTheSame(oldItem: PaymentOption, newItem: PaymentOption): Boolean {
        return oldItem == newItem
    }
}

class BuyOptionsDiffCallback : DiffUtil.ItemCallback<BuyOptionData>() {

    override fun areItemsTheSame(oldItem: BuyOptionData, newItem: BuyOptionData): Boolean {
        return oldItem.plan == newItem.plan
    }

    override fun areContentsTheSame(oldItem: BuyOptionData, newItem: BuyOptionData): Boolean {
        return oldItem == newItem
    }
}
