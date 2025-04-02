package com.yourfitness.shop.ui.features.product_details

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.*
import com.yourfitness.shop.R
import com.yourfitness.shop.data.entity.ItemColorEntity
import com.yourfitness.shop.data.entity.ItemSizeEntity
import com.yourfitness.shop.ui.features.orders.cart.enter_coins.REQUEST_CODE_ENTER_COINS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceDetailsFragment : ProductDetailsFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.productTitle.setTextAppearance(
            requireContext(),
            com.yourfitness.common.R.style.TextAppearance_YFC_Heading2
        )
        binding.serviceLogo.isVisible = true
        binding.textAddress.isVisible = true
        binding.distanceTo.isVisible = true
        binding.serviceLogo.isVisible = true
        binding.separator.isVisible = true
        binding.productSubtitle.isVisible = false
        binding.redemptionCoins.setTextColorRes(com.yourfitness.common.R.color.main_active)
        binding.buttonAdd.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.PurchaseServiceTapped
        }
        binding.iconInfo.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.RedemptionInfoTapped
        }
    }

    override fun showColors(items: List<ItemColorEntity>, selectedColor: String) {
        binding.listColorsContainer.isVisible = false
        binding.colorLabel.isVisible = false
        binding.spacer1.isVisible = false
        binding.spacer2.isVisible = false
    }

    override fun showSizes(items: List<ItemSizeEntity>, selectedSize: String) {
        binding.listSizesContainer.isVisible = false
        binding.sizeLabel.isVisible = false
        binding.spacer3.isVisible = false
        binding.spacer4.isVisible = false
    }

    private fun onCoinsPartChangeTapped() {
        setFragmentResultListener(REQUEST_CODE_ENTER_COINS) { _, bundle ->
            val coinsPart = bundle.getLong("enteredCoins")
            viewModel.intent.postValue(ProductDetailsIntent.CoinsPartChanged(coinsPart))
            clearFragmentResult(REQUEST_CODE_ENTER_COINS)
        }
        viewModel.intent.value = ProductDetailsIntent.ChangeRedemptionCoinsTapped
    }

    override fun setupSpecificViews(state: ProductDetailsState.ProductLoaded) {
        val product = state.product.product
        binding.textAddress.text = product.vendorAddress.orEmpty()
        binding.distanceTo.text = state.distance?.formatDistance().orEmpty()
        binding.serviceName.text = product.vendorName
        binding.discountInfo.isVisible
        val price = product.price.formatAmount(state.currency.uppercase())
        binding.buttonAdd.text = getString(R.string.purchase_button_message, price)
        if (product.vendorImageId != null) {
            Glide.with(binding.root).load(product.vendorImageId.toImageUri()).into(binding.serviceLogo)
        } else {
            binding.serviceLogo.isVisible = false
        }
        binding.discountInfo.isVisible = state.product.product.redeemableCoins > 0

//        setupPriceArea(state)
        super.setupSpecificViews(state)
        setupCoinsArea(state)
        setupActionButton(state)
    }

    private fun setupActionButton(state: ProductDetailsState.ProductLoaded) {
        val coinsSuffix: String
        if (state.coinsSelected > 0) {
            coinsSuffix = resources.getQuantityString(
                R.plurals.purchase_button_coins,
                state.coinsSelected.toInt(),
                state.coinsSelected
            )
            val price = state.discountPrice.formatAmount(state.currency.uppercase())
            binding.buttonAdd.text = getString(R.string.purchase_button_message, "$price$coinsSuffix")
        }
    }

    private fun setupPriceArea(state: ProductDetailsState.ProductLoaded) {
        val product = state.product
        val productPrice = product.product.price.formatAmount(state.currency.uppercase())
        if (state.coinsSelected > 0) {
            binding.redemptionLabel.isVisible = true
//            binding.oldCurrencyPrice.applyTextColorRes(R.color.card_swipe_background)
            binding.oldCurrencyPrice.text = state.discountPrice.formatAmount(state.currency.uppercase())
            binding.oldCurrencyPrice.isVisible = true
            binding.price.text = productPrice
            binding.price.paintFlags =
                binding.oldCurrencyPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.redemptionLabel.isVisible = false
//            binding.price.applyTextColorRes(com.yourfitness.common.R.color.black)
            binding.oldCurrencyPrice.isVisible = false
            binding.price.text = productPrice
        }
    }

    private fun setupCoinsArea(state: ProductDetailsState.ProductLoaded) {
        binding.redemptionCoins.text = state.product.product.redeemableCoins.toString()
    }

    override fun showStockLabel(value: String) {
        binding.productStock.isVisible = false
    }
}
