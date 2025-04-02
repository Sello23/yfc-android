package com.yourfitness.shop.ui.features.orders.details

import android.content.Context
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.applyTextColorRes
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.getColorCompat
import com.yourfitness.common.ui.utils.setColor
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.ItemCartBinding
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.ui.features.orders.cart.CartViewHolder

class DetailsAdapter: RecyclerView.Adapter<OrderViewHolder>() {
    private val cartCardList = arrayListOf<CartCard>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(cartCardList[position], true)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {}

    override fun getItemCount(): Int = cartCardList.size

    fun setData(cartCard: List<CartCard>) {
        this.cartCardList.clear()
        this.cartCardList.addAll(cartCard)
    }
}

open class OrderViewHolder(
    binding: ItemCartBinding,
    private val context: Context
) : CartViewHolder(binding, context = context) {

    override fun bind(cartCard: CartCard, needUpdateBounds: Boolean) {
        super.bind(cartCard, needUpdateBounds)

        binding.priceLabel.isVisible = false
        binding.itemPrice.isVisible = false
        binding.oldCurrencyPrice.isVisible = false

        binding.itemPrice2.isVisible = true
        binding.itemPrice2.text = cartCard.discountPrice.formatAmount(cartCard.currency.uppercase())
        binding.oldCurrencyPrice2.isVisible = cartCard.discountPrice != cartCard.wholePrice
        binding.itemPrice2.applyTextColorRes(
            if (binding.oldCurrencyPrice2.isVisible) R.color.card_swipe_background
            else com.yourfitness.common.R.color.text_gray
        )
        binding.oldCurrencyPrice2.paintFlags =
            binding.oldCurrencyPrice2.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.oldCurrencyPrice2.text = cartCard.wholePrice.formatAmount()

        binding.coinRedemptionGroup.isVisible = false
        binding.coinsUsed.isVisible = cartCard.coinsAmount != 0L
        val priceCoinsPart = cartCard.coinsAmount.toString() + " / " +
                cartCard.coinsPriceEquivalent.formatAmount(cartCard.currency.uppercase())
        binding.coinsUsed.text = priceCoinsPart
        binding.orderStatus.isVisible = true
        binding.orderStatus.text = buildDebitedCreditsText(cartCard.statusText, cartCard.status)
        binding.selectInfoLabel.isVisible = false
        val invoiceDrawable = ContextCompat.getDrawable(context, com.yourfitness.common.R.drawable.ic_profile_coins)
        binding.spinner.setCompoundDrawablesWithIntrinsicBounds(invoiceDrawable, null, null, null)
        Glide.with(context).load(cartCard.coverImage.toImageUri()).into(binding.image)
    }

    private fun buildDebitedCreditsText(statusValue: String, status: Int): SpannableString {
        val text = context.getString(R.string.item_status, statusValue)
        return SpannableString(text).apply {
            val start = text.indexOf(":")
            val color = context.getColorCompat(getStatusColor(status))
            setColor(color, start + 1, text.length)
        }
    }

    private fun getStatusColor(status: Int): Int {
        return when (status) {
            0 -> com.yourfitness.common.R.color.yellow
            1 -> com.yourfitness.common.R.color.main_active
            2 -> R.color.status_text_delivered
            3 -> com.yourfitness.common.R.color.grey
            4 -> com.yourfitness.common.R.color.issue_red
            else -> R.color.status_gb_fulfilled
        }
    }
}
