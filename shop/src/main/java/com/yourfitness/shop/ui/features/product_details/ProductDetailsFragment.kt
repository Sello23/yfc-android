package com.yourfitness.shop.ui.features.product_details

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.*
import com.yourfitness.common.ui.views.GalleryAdapter
import com.yourfitness.shop.R
import com.yourfitness.shop.data.entity.ItemColorEntity
import com.yourfitness.shop.data.entity.ItemSizeEntity
import com.yourfitness.shop.databinding.FragmentProductDetailsBinding
import com.yourfitness.shop.databinding.ItemColorImgBinding
import com.yourfitness.shop.databinding.ItemInfoBinding
import com.yourfitness.shop.databinding.ItemProductSizeBinding
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_AMOUNT
import com.yourfitness.shop.ui.features.catalog.REQUEST_CODE_PRODUCT_DETAILS
import com.yourfitness.shop.ui.features.catalog.toPx
import com.yourfitness.shop.ui.features.product_details.dialogs.SelectCoinsAmountDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class ProductDetailsFragment : MviFragment<ProductDetailsIntent, Any, ProductDetailsViewModel>() {

    override val binding: FragmentProductDetailsBinding by viewBinding()
    override val viewModel: ProductDetailsViewModel by viewModels()
    private val pagerIndicator by lazy { PagerIndicator() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.intent.value = ProductDetailsIntent.ScreenOpened
        setupViews()
        setupToolbar(binding.toolbar.root)
        val category = requireArguments().getString("category")
        binding.toolbar.detailsTitle.text = category
        setCoinsUsedAmountListener()
        setupHowToEarnCoinsFragmentListener()
    }

    override fun onPause() {
        super.onPause()
        viewModel.intent.value = ProductDetailsIntent.UploadFavorites
    }

    private fun setupViews() {
        binding.toolbar.coins.root.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.CoinsInfo
        }

        binding.shopBucket.root.setOnClickListener {
            viewModel.handleIntent(ProductDetailsIntent.Cart)
        }

        binding.buttonAdd.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.ActionButtonClicked
        }

        binding.favoriteIcon.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.FavoriteChanged(!binding.favoriteIcon.isSelected)
            binding.favoriteIcon.isSelected = !binding.favoriteIcon.isSelected
        }

        binding.discountInfo.setOnClickListener {
            viewModel.intent.value = ProductDetailsIntent.RedemptionInfoTapped
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is ProductDetailsState.CartSizeLoaded -> setupShopBucket(state.size)
            is ProductDetailsState.ProductLoaded -> {
                setupShopBucket(state.cartSize)
                showDetails(state)
                binding.toolbar.coins.textCoins.text = resources.getQuantityString(
                    com.yourfitness.common.R.plurals.profile_screen_format_coins,
                    state.coinsAmount.toInt(),
                    state.coinsAmount.formatCoins()
                )
            }
            is ProductDetailsState.UpdateSizesList -> {
                showGallery(state.images)
                showStockLabel(state.stockState)
                showColors(state.colors, state.selectedColor)
                showSizes(state.sizes, state.selectedSize)
            }
            is ProductDetailsState.ItemAddedToCart -> {
                setupShopBucket(state.cartSize)
                binding.toolbar.coins.textCoins.text = resources.getQuantityString(
                    com.yourfitness.common.R.plurals.profile_screen_format_coins,
                    state.coinsAmount.toInt(),
                    state.coinsAmount.formatCoins()
                )
                showPopupMessage()
            }
            is ProductDetailsState.FavoriteStateUpdated -> binding.favoriteIcon.isSelected = state.checked
        }
    }

    private fun showDetails(state: ProductDetailsState.ProductLoaded) {
        val product = state.product
        val defProduct = product.colors.find { it.itemColor.isDefault }
        val type = object : TypeToken<List<String>>() {}.type
        showGallery(defProduct?.images?.map { it.image } ?: Gson().fromJson(product.product.images, type))

        binding.favoriteIcon.isSelected = state.isFavorite
        binding.productTitle.text = product.product.name
        setupSpecificViews(state)
        binding.textDescription.text = product.product.description

        showColors(product.colors.map { it.itemColor }, state.selectedColor)
        showSizes(
            product.colors.find { it.itemColor.color == state.selectedColor }?.sizes.orEmpty(),
            state.selectedSize
        )
        showInfo(state.info)
        showStockLabel(state.stockState)
    }

    protected open fun setupSpecificViews(state: ProductDetailsState.ProductLoaded) {
        val product = state.product
        binding.price.text = product.product.price.formatAmount(state.currency).uppercase()
        binding.discountInfo.isVisible = product.product.redeemableCoins > 0
        if (product.product.redeemableCoins > 0) {
            binding.price.paintFlags = binding.price.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            binding.oldCurrencyPrice.text =
                (product.product.price - (product.product.redeemableCoins * state.coinsValue).toCoins())
                    .formatAmount(state.currency).uppercase()
            binding.redemptionCoins.text = product.product.redeemableCoins.toString()
        }
        binding.productSubtitle.text = product.product.brandName
    }

    protected open fun showStockLabel(value: String) {
        val background: Int = when (value) {
            ItemSizeEntity.IN_STOCK -> {
                R.drawable.rounded_border_in_stock
            }
            ItemSizeEntity.LIMITED_STOCK -> {
                R.drawable.rounded_border_limited_stock
            }
            ItemSizeEntity.NO_STOCK -> {
                R.drawable.rounded_border_no_stock
            }
            else -> {
                binding.productStock.isVisible = false
                return
            }
        }
        binding.productStock.isVisible = true
        binding.productStock.text = value
        val theme = requireActivity().theme
        binding.productStock.background = ResourcesCompat.getDrawable(resources, background, theme)
        binding.buttonAdd.isEnabled = value.isNotBlank() && value != ItemSizeEntity.NO_STOCK
    }

    private fun showGallery(gallery: List<String>) {
        binding.indicators.removeAllViews()
        binding.indicators.isVisible = gallery.size > 1
        binding.pagerGallery.adapter = GalleryAdapter(gallery)
        pagerIndicator.setupViewpager(binding.indicators, binding.pagerGallery)
    }

    protected open fun showColors(items: List<ItemColorEntity>, selectedColor: String) {
        binding.listColorsContainer.isVisible = items.isNotEmpty()
        binding.colorLabel.isVisible = items.isNotEmpty()
        binding.spacer1.isVisible = items.isNotEmpty()
        binding.spacer2.isVisible = items.isNotEmpty()
        if (items.isEmpty()) return

        val parent = binding.listColors
        parent.removeAllViews()
        items.forEach { data ->
            val binding = ItemColorImgBinding.inflate(layoutInflater, parent, false)
            Glide.with(binding.root).load(data.defaultImageId.toImageUri()).into(binding.colorImage)
            val isSelected = data.color == selectedColor
            binding.colorImageContainer.isSelected = isSelected
            val padding = if (isSelected) 1.toPx() else 0.5f.toPx().toInt()
            binding.colorImage.setPadding(padding, padding, padding, padding)
            binding.root.setOnClickListener {
                viewModel.intent.value = ProductDetailsIntent.ColorChanged(data.color, data.colorId)
            }
            parent.addView(binding.root)
        }
    }

    protected open fun showSizes(sizes: List<ItemSizeEntity>, selectedSize: String) {
        val items = sizes.sortedBy { it.sequence }
        binding.listSizesContainer.isVisible = items.isNotEmpty()
        binding.sizeLabel.isVisible = items.isNotEmpty()
        binding.spacer3.isVisible = items.isNotEmpty()
        binding.spacer4.isVisible = items.isNotEmpty()
        if (items.isEmpty()) return

        val parent = binding.listSizes
        parent.removeAllViews()
        items.forEach { data ->
            val binding = ItemProductSizeBinding.inflate(layoutInflater, parent, false)
            binding.sizeDefault.text = data.size
            binding.sizeDefault.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.yourfitness.common.R.color.black
                )
            )
            binding.sizeDefault.isSelected = data.size == selectedSize
            binding.root.setOnClickListener {
                viewModel.intent.value = ProductDetailsIntent.SizeChanged(data.size, data.type, data.sizeId)
            }
            parent.addView(binding.root)
        }
    }

    private fun setupShopBucket(size: Int) {
        binding.shopBucket.root.isVisible = size > 0
        binding.shopBucket.bucketItems.text = size.toString()
    }

    private fun showInfo(items: Map<String, Any>) {
        val parent = binding.listDescription
        parent.removeAllViews()
        items.forEach { info ->
            val binding = ItemInfoBinding.inflate(layoutInflater, parent, false)
            binding.label.text = getString(R.string.product_info, info.key)
            binding.value.text = info.value.toString()
            parent.addView(binding.root)
        }
    }

    private fun showPopupMessage() {
        val snackbar = Snackbar.make(
            binding.snackbarContainer,
            R.string.product_added_to_cart,
            Snackbar.LENGTH_SHORT
        )
        val customSnackView: View = layoutInflater.inflate(com.yourfitness.common.R.layout.snackbar_cart, null)
        val text = customSnackView.findViewById<MaterialTextView>(com.yourfitness.common.R.id.message)
        text.text = getString(R.string.product_added_to_cart)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
    }

    private fun setCoinsUsedAmountListener() {
        setFragmentResultListener(SelectCoinsAmountDialog.RESULT) { _, bundle ->
            val coinsUsed = bundle.getLong(COINS_AMOUNT)
            viewModel.intent.postValue(ProductDetailsIntent.CoinsAmountSelected(coinsUsed))
            clearFragmentResult(SelectCoinsAmountDialog.RESULT)
            setCoinsUsedAmountListener()
        }
    }

    private fun setupHowToEarnCoinsFragmentListener() {
        setFragmentResultListener("faq_result") { _, _ ->
            viewModel.intent.postValue(ProductDetailsIntent.RedemptionInfoTapped)
            clearFragmentResultListener("faq_result")
            setupHowToEarnCoinsFragmentListener()
        }
    }

    override fun onDestroy() {
        viewModel.product?.let {
            val bundle = bundleOf(
                "dbId" to it.product._id,
                "productId" to it.product.id,
                "checked" to (it.favourite != null),
            )
            setFragmentResult(REQUEST_CODE_PRODUCT_DETAILS, bundle)
        }

        super.onDestroy()
    }
}
