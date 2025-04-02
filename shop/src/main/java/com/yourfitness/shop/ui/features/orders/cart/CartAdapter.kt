package com.yourfitness.shop.ui.features.orders.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.applyTextColorRes
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.ItemCartBinding
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.ui.features.catalog.toPx

class CartAdapter(private val callback: CartActionsCallback? = null): RecyclerView.Adapter<CartViewHolder>() {
    val cartCardList = arrayListOf<CartCard>()
    private var needSample: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding, callback, parent.context)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(
            cartCardList[position],
            position != 0 || (payloads.isNotEmpty() && payloads.first() == true) || !needSample
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {}

    override fun getItemCount(): Int = cartCardList.size

    fun setData(cartCard: List<CartCard>, needSample: Boolean = false) {
        this.cartCardList.clear()
        this.cartCardList.addAll(cartCard)
        this.needSample = needSample
    }

    inline fun <reified T> Any.isSameType() = this is T
}

open class CartViewHolder(
    binding: ItemCartBinding,
    private val callback: CartActionsCallback? = null,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    protected val binding: ItemCartBinding = ItemCartBinding.bind(itemView)

    open fun bind(cartCard: CartCard, needUpdateBounds: Boolean) {
        if (needUpdateBounds) setupLayoutParams()
        binding.itemTitle.text = cartCard.title
        binding.itemSubtitle.text = cartCard.subtitle
        binding.itemPrice.text = cartCard.discountPrice.formatAmount(cartCard.currency.uppercase())
        binding.oldCurrencyPrice.isVisible = cartCard.discountPrice != cartCard.wholePrice
        binding.itemPrice.applyTextColorRes(
            if (binding.oldCurrencyPrice.isVisible) R.color.card_swipe_background
            else com.yourfitness.common.R.color.text_gray
        )
        binding.oldCurrencyPrice.paintFlags =
            binding.oldCurrencyPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.oldCurrencyPrice.text = cartCard.wholePrice.formatAmount()
        binding.sizeDefault.isVisible = cartCard.size.isNotEmpty()
        binding.sizeDefault.text = context.getString(R.string.item_size, cartCard.size)
        binding.colorDefault.isVisible = cartCard.color.isNotEmpty()
        binding.colorDefault.text = context.getString(R.string.item_color, cartCard.color)
        Glide.with(context).load(cartCard.coverImage.toImageUri()).into(binding.image)
        binding.coinRedemptionGroup.isVisible = cartCard.maxCoins > 0
        binding.spinner.text = cartCard.coinsAmount.toString()
        binding.spinner.setOnClickListener {
            callback?.onCoinsPartChangeTapped(cartCard)
        }
    }

    private fun setupLayoutParams() {
        val params = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.MATCH_PARENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 0)
        binding.root.layoutParams = params
        binding.contentContainer.setPadding(0, 0, 16.toPx(), 0)
        binding.deleteContainer.isVisible = false
    }
}
