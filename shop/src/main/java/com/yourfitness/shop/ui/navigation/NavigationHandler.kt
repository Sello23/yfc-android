package com.yourfitness.shop.ui.navigation

import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import com.yourfitness.shop.R
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.ui.constants.Constants.Companion.ADDRESS
import com.yourfitness.shop.ui.constants.Constants.Companion.ADDRESS_ENTITY
import com.yourfitness.shop.ui.constants.Constants.Companion.CATEGORY
import com.yourfitness.shop.ui.constants.Constants.Companion.CLAIM_DATA
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_AMOUNT
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_COST
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_COUNT
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_VALUE
import com.yourfitness.shop.ui.constants.Constants.Companion.CONTACT_INFO
import com.yourfitness.shop.ui.constants.Constants.Companion.CURRENCY
import com.yourfitness.shop.ui.constants.Constants.Companion.FAVORITES
import com.yourfitness.shop.ui.constants.Constants.Companion.FILTERS
import com.yourfitness.shop.ui.constants.Constants.Companion.ORDER_CLASSIFICATION
import com.yourfitness.shop.ui.constants.Constants.Companion.ORDER_ID
import com.yourfitness.shop.ui.constants.Constants.Companion.PRICE
import com.yourfitness.shop.ui.constants.Constants.Companion.PRODUCT_TYPE_ID
import com.yourfitness.shop.ui.constants.Constants.Companion.PROD_ID
import com.yourfitness.shop.ui.constants.Constants.Companion.REDEEMABLE_AMOUNT
import com.yourfitness.shop.ui.constants.Constants.Companion.SEARCH_QUERY
import com.yourfitness.shop.ui.constants.Constants.Companion.SELECTED_COINS
import com.yourfitness.shop.ui.constants.Constants.Companion.SELECT_COINS_FLOW
import com.yourfitness.shop.ui.features.orders.details.services.VoucherClaimedSuccessfullyDialog.Companion.NAME
import com.yourfitness.shop.ui.features.orders.details.services.VoucherClaimedSuccessfullyDialog.Companion.NUMBER

class ShopNavigationHandler constructor(
    private val navController: NavController,
    private val navigator: ShopNavigator
) {

    fun observeNavigation(owner: LifecycleOwner) {
        navigator.navigation.observe(owner) {
            if (it != null) {
                navigator.navigation.value = null
                navigate(it)
            }
        }
    }

    private fun navigate(node: ShopNavigation) {
        when (node) {
            is ShopNavigation.ShopIntro -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.shop/fragment_shop_intro/${node.coins}/${node.coinsCost}/${node.currency}".toUri())
                    .build()
                navController.navigate(request)
            }
            is ShopNavigation.ShopCategories -> {
                navController.popBackStack()
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.shop/fragment_shop_categories".toUri())
                    .build()
                navController.navigate(request)
            }
            is ShopNavigation.Catalog -> {
                val destId = when (node.productTypeId) {
                    ProductTypeId.APPAREL -> R.id.fragment_apparel
                    ProductTypeId.ACCESSORIES -> R.id.fragment_accessories
                    ProductTypeId.EQUIPMENT -> R.id.fragment_equipment
                    ProductTypeId.SERVICES -> R.id.fragment_services
                }
                val args = bundleOf(
                    FILTERS to node.filter,
                    PRODUCT_TYPE_ID to node.productTypeId,
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_shop_categories, false)
                    .build()
                navController.navigate(destId, args, options)
            }
            is ShopNavigation.Cart -> {
                val args = bundleOf(
                    CURRENCY to node.currency,
                    COINS_VALUE to node.coinsValue
                )
                navController.navigate(R.id.fragment_cart, args)
            }
            is ShopNavigation.ProductDetails -> {
                val args = bundleOf(
                    CATEGORY to node.category,
                    PROD_ID to node.productId,
                    CURRENCY to node.currency,
                    COINS_VALUE to node.coinsValue,
                    PRODUCT_TYPE_ID to node.productTypeId
                )
                if (node.isServices) {
                    navController.navigate(R.id.fragment_service_details, args)
                } else {
                    navController.navigate(R.id.fragment_product_details, args)
                }
            }
            is ShopNavigation.ProductFilter -> {
                val args = bundleOf(
                    CURRENCY to node.currency,
                    FILTERS to node.filters,
                    SEARCH_QUERY to node.searchQuery,
                    FAVORITES to node.isFavorites,
                    PRODUCT_TYPE_ID to node.productTypeId
                )
                navController.navigate(R.id.dialog_product_filters, args)
            }
            is ShopNavigation.ServicesFilter -> {
                val args = bundleOf(
                    FILTERS to node.filters,
                    SEARCH_QUERY to node.searchQuery,
                    FAVORITES to node.isFavorites,
                    PRODUCT_TYPE_ID to node.productTypeId
                )
                navController.navigate(R.id.dialog_services_filters, args)
            }
            is ShopNavigation.EnterRange -> {
                val args = bundleOf(
                    CURRENCY to node.currency,
                    "rangeMin" to node.rangeMin,
                    "rangeMax" to node.rangeMax
                )
                navController.navigate(R.id.dialog_enter_range, args)
            }
            is ShopNavigation.EnterCoins -> {
                val args = bundleOf(
                    CURRENCY to node.currency,
                    "maxRedemption" to node.maxRedemption,
                    "availableCoins" to node.availableCoins,
                    COINS_VALUE to node.coinsValue
                )
                navController.navigate(R.id.dialog_enter_coins, args)
            }
            is ShopNavigation.DeliveryAddress -> {
                val args = bundleOf(
                    COINS_AMOUNT to node.coinsAmount,
                    COINS_VALUE to node.coinsValue,
                    CURRENCY to node.currency
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_cart, false)
                    .build()
                navController.navigate(R.id.fragment_delivery_address, args, options)
            }
            is ShopNavigation.PaymentOptions -> {
                val args = bundleOf(
                    COINS_AMOUNT to node.coinsAmount,
                    ADDRESS to node.address,
                    CONTACT_INFO to node.contactInfo,
                    ADDRESS_ENTITY to node.addressEntity
                )
                navController.navigate(R.id.fragment_payment_options, args)
            }
            is ShopNavigation.ServicePaymentOptions -> {
                val args = bundleOf(
                    COINS_AMOUNT to node.balanceCoins,
                    COINS_COUNT to node.selectedCoins,
                    PRICE to node.price,
                    PROD_ID to node.productId
                )
                navController.navigate(R.id.fragment_service_payment_options, args)
            }
            is ShopNavigation.BackToShopping -> {
                for (i in 0 until node.popAmount) {
                    navController.popBackStack()
                }
                if (node.paymentSuccess) {
                    val args = bundleOf(
                        ORDER_CLASSIFICATION to node.orderClassification
                    )
                    navController.navigate(R.id.dialog_products_payment_success, args)
                }
            }
            is ShopNavigation.PaymentError -> {
                val args = bundleOf(
                    "popAmount" to node.popAmount,
                    ORDER_CLASSIFICATION to node.orderClassification
                )
                navController.navigate(R.id.dialog_products_payment_error, args)
            }
            is ShopNavigation.OrderHistory -> {
                val args = bundleOf(
                    ORDER_CLASSIFICATION to node.orderClassification
                )
                navController.navigate(R.id.fragment_orders_history, args)
            }
            is ShopNavigation.OrderHistoryDetails -> {
                val args = bundleOf(
                    ORDER_ID to node.orderId
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_orders_history, false)
                    .build()
                navController.navigate(R.id.fragment_orders_history_details, args, options)
            }
            is ShopNavigation.ServiceOrderHistoryDetails -> {
                val args = bundleOf(
                    ORDER_ID to node.orderId
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_orders_history, false)
                    .build()
                navController.navigate(R.id.fragment_service_orders_history_details, args, options)
            }
            is ShopNavigation.CoinRedemptionInfo -> {
                val args = bundleOf(
                    REDEEMABLE_AMOUNT to node.redeemableAmount,
                )
                navController.navigate(R.id.dialog_coin_redemption_info, args)
            }
            is ShopNavigation.DetailInitialInfo -> {
                val args = bundleOf(
                    COINS_COUNT to node.coinsAmount,
                )
                navController.navigate(R.id.dialog_initial_info, args)
            }
            is ShopNavigation.CancelOrderConfirmDialog -> {
                val args = bundleOf(
                    "total_coins" to node.totalCoins,
                    "total_price" to node.totalPrice,
                    CURRENCY to node.currency
                )
                navController.navigate(R.id.dialog_cancel_order_confirm, args)
            }
            is ShopNavigation.OrderCancelled -> {
                navController.navigate(R.id.dialog_order_cancelled)
            }
            is ShopNavigation.ConfirmClaimVoucher -> {
                val args = bundleOf(
                    ORDER_ID to node.orderId
                )
                navController.navigate(R.id.dialog_claim_voucher_confirm, args)
            }
            is ShopNavigation.ClaimVoucherConfirmed -> {
                val args = bundleOf(
                    CLAIM_DATA to node.data
                )
                navController.navigate(R.id.dialog_voucher_claimed_confirm, args)
            }
            is ShopNavigation.VoucherClaimedSuccessfully -> {
                val args = bundleOf(
                    NAME to node.name,
                    NUMBER to node.number
                )
                navController.navigate(R.id.dialog_voucher_claimed_successfully, args)
            }
            is ShopNavigation.CoinsUsageInfo -> {
                if (node.coins == 0L) {
                    openCoinsFaq()
                    return
                }
                val args = bundleOf(
                    COINS_COUNT to node.coins,
                    "coins_cost" to node.coinsCost,
                    "currency" to node.currency
                )
                navController.navigate(R.id.dialog_coins_usage, args)
            }
            is ShopNavigation.SelectCoinsAmount -> {
                val args = bundleOf(
                    PRICE to node.price,
                    COINS_AMOUNT to node.maxCoins,
                    COINS_COST to node.coinsCost,
                    CURRENCY to node.currency,
                    SELECTED_COINS to node.currentlySelectedCoins,
                    SELECT_COINS_FLOW to node.flow,
                )
                navController.navigate(R.id.dialog_select_coins_amount, args)
            }
            is ShopNavigation.HowToEarnCoins -> {
                openCoinsFaq(node.title)
            }
            is ShopNavigation.PopScreen -> {
                for (i in 1..node.times) navController.popBackStack()
            }
            else -> Toast.makeText(navController.context, "Navigation destination not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCoinsFaq(titleValue: String? = null) {
        val title = titleValue ?: "How to earn coins?"
        val request = NavDeepLinkRequest.Builder
            .fromUri("android-app://com.yourfitness.coach/fragment_story/$title".toUri())
            .build()
        navController.navigate(request)
    }
}