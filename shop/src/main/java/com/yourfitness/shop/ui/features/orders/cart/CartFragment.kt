package com.yourfitness.shop.ui.features.orders.cart

import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.applyTextColorRes
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentCartBinding
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.catalog.REQUEST_CODE_CART_CLOSED
import com.yourfitness.shop.ui.features.product_details.dialogs.SelectCoinsAmountDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : MviFragment<CartIntent, Any, CartViewModel>(), CartActionsCallback {

    override val binding: FragmentCartBinding by viewBinding()
    override val viewModel: CartViewModel by viewModels()
    private val adapter by lazy { CartAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.intent.value = CartIntent.ScreenOpened
        setupRecyclerView()
        setupSwipeToDelete()
        setupToolbar(binding.toolbar.root)
        binding.toolbar.root.title = getString(R.string.yfc_shop)
        binding.subtotal.btnCheckout.setOnClickListener {
            viewModel.intent.value = CartIntent.Checkout
        }
        binding.toolbar.coins.root.setOnClickListener {
            viewModel.intent.value = CartIntent.CoinsInfo
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is CartState.Loading -> showLoading(state.loading)
            is CartState.Error -> showError(state.error)
            is CartState.GeneralDataLoaded -> {
                showLoading(false)
                configureToolbar(state.coins)
            }
            is CartState.DataLoaded -> {
                configureToolbar(state.coins)
                adapter.setData(state.items, state.needSample)
                adapter.notifyDataSetChanged()
                setupOverallView(state)
                if (state.deleteAnimationShowed) return
                lifecycleScope.launch {
                    delay(500)
                    initExampleSwipe()
                }
            }
            is CartState.CartIsEmpty -> findNavController().navigateUp()
        }
    }

    private fun configureToolbar(coins: Long) {
        binding.toolbar.coins.textCoins.text = resources.getQuantityString(
            com.yourfitness.common.R.plurals.profile_screen_format_coins,
            coins.toInt(),
            coins.formatCoins()
        )
        binding.toolbar.emptyCart.setOnClickListener {
            viewModel.intent.value = CartIntent.EmptyCart
        }
    }

    private fun setupRecyclerView() {
        binding.cartRecyclerView.adapter = adapter
        binding.cartRecyclerView.setDivider(R.drawable.cart_item_divider)
    }

    private fun setupOverallView(state: CartState.DataLoaded) {
        binding.subtotal.itemsAmount.text = resources.getQuantityString(R.plurals.items_amount, state.items.size, state.items.size)
        binding.subtotal.currencyPrice.text = state.priceWithoutCoins.formatAmount(state.currency).uppercase()
        binding.subtotal.oldCurrencyPrice.isVisible = state.price != state.priceWithoutCoins
        binding.subtotal.currencyPrice.applyTextColorRes(
            if (binding.subtotal.oldCurrencyPrice.isVisible) R.color.card_swipe_background
            else com.yourfitness.common.R.color.black
        )
        binding.subtotal.oldCurrencyPrice.text = state.price.formatAmount()
        binding.subtotal.oldCurrencyPrice.paintFlags =
            binding.subtotal.oldCurrencyPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.subtotal.coinsPrice.text = buildCoinsPricePartText(
            state.overallCoins,
            state.currency,
            state.overallPrice
        )
        binding.subtotal.coinsPrice.applyTextColorRes(
            if (state.overallCoins == 0L) com.yourfitness.common.R.color.gray_light
            else com.yourfitness.common.R.color.black
        )
    }

    private fun setupSwipeToDelete() {
        val swipeToDeleteCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    val item = adapter.cartCardList[position]
                    viewModel.intent.value =
                        CartIntent.ItemDeleted(item.uuid)
                    adapter.notifyItemRemoved(position)
                }
            }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.cartRecyclerView)
    }

    private fun initExampleSwipe() {
        if (!isAdded) return
        val enterAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.enter_anim)
        val exitAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.exit_anim)
        enterAnim.fillAfter = true
        exitAnim.fillAfter = true
        val view = binding.cartRecyclerView[0]

        exitAnim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                adapter.notifyItemChanged(0, true)
                viewModel.intent.value = CartIntent.DeleteAnimationShowed
            }
        })

        view.startAnimation(enterAnim)
        Handler().postDelayed({
            view.startAnimation(exitAnim)
        }, enterAnim.duration)
    }

    private fun buildCoinsPricePartText(
        coins: Long,
        currency: String,
        currencyValue: Long
    ): String {
        val currencyPart = currencyValue.formatAmount(currency).uppercase()
        return getString(R.string.cart_screen_redemption_data, coins, currencyPart)
    }

    override fun onCoinsPartChangeTapped(item: CartCard) {
        setFragmentResultListener(SelectCoinsAmountDialog.RESULT) { _, bundle ->
            val coinsUsed = bundle.getLong(Constants.COINS_AMOUNT)
            viewModel.intent.postValue(CartIntent.CoinsPartChanged(item.uuid, coinsUsed))
            clearFragmentResult(SelectCoinsAmountDialog.RESULT)
        }

        viewModel.intent.value = CartIntent.ChangeRedemptionCoinsTapped(item.uuid)
    }

    override fun onDestroy() {
        setFragmentResult(REQUEST_CODE_CART_CLOSED, bundleOf())
        super.onDestroy()
    }
}

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(this.context, drawableRes)

    class CustomDividerItemDecoration : RecyclerView.ItemDecoration() {
        override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val dividerLeft = parent.paddingLeft
            val dividerRight = parent.width - parent.paddingRight
            val childCount = parent.childCount
            for (i in 0..childCount - 2) {
                val child: View = parent.getChildAt(i)
                val params =
                    child.layoutParams as RecyclerView.LayoutParams
                val dividerTop: Int = child.bottom + params.bottomMargin
                val dividerBottom = dividerTop + (drawable?.intrinsicHeight ?: 0)
                drawable?.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                drawable?.draw(canvas)
            }
        }
    }
    addItemDecoration(CustomDividerItemDecoration())
}
