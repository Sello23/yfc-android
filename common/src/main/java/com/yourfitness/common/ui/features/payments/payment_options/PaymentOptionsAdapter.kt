package com.yourfitness.common.ui.features.payments.payment_options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.R
import com.yourfitness.common.databinding.ItemPaymentOptionBinding
import com.yourfitness.common.domain.models.PaymentOption
import com.yourfitness.common.ui.utils.PaymentOptionsDiffCallback

class PaymentOptionsAdapter(
    private val onItemClick: (PaymentOption) -> Unit
) : ListAdapter<PaymentOption, PaymentOptionViewHolder>(PaymentOptionsDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_payment_option
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentOptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PaymentOptionViewHolder(
            ItemPaymentOptionBinding.bind(view),
            onItemClick
        )
    }

    override fun onBindViewHolder(holder: PaymentOptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PaymentOptionViewHolder(
    private val binding: ItemPaymentOptionBinding,
    private val onItemClick: (PaymentOption) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: PaymentOption) {
        with(binding) {
            radioButton.isChecked = item.isSelected
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            ivIcon.setImageResource(item.iconResId)
            root.setOnClickListener { onItemClick(item) }
        }
    }
}
