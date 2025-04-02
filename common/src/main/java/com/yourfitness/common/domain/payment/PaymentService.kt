package com.yourfitness.common.domain.payment

import android.content.Intent
import androidx.fragment.app.Fragment
import com.stripe.android.*
import com.stripe.android.model.*
import com.yourfitness.common.BuildConfig.STRIPE_PUBLISHABLE_KEY
import com.yourfitness.common.domain.models.CreditCard
import timber.log.Timber

class PaymentService(private val callback: Callback) {

    private var stripe: Stripe? = null

    fun payByCard(fragment: Fragment, card: CreditCard, clientSecret: String) {
        log("payByCard -> card=${card}, clientSecret=${clientSecret}")
        stripe = Stripe(fragment.requireContext(), STRIPE_PUBLISHABLE_KEY)
        stripe?.confirmPayment(
            fragment,
            ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                createPaymentMethodCreateParams(card),
                clientSecret
            )
        )
    }

    fun payBySavedCard(fragment: Fragment, cardId: String, clientSecret: String) {
        log("payBySavedCard -> cardId=$cardId, clientSecret=$clientSecret")
        stripe = Stripe(fragment.requireContext(), STRIPE_PUBLISHABLE_KEY)
        stripe?.confirmPayment(
            fragment,
            ConfirmPaymentIntentParams.createWithPaymentMethodId(cardId, clientSecret)
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        stripe?.onPaymentResult(requestCode, data, createPaymentApiResultCallback())
    }

    private fun createPaymentMethodCreateParams(card: CreditCard): PaymentMethodCreateParams {
        return PaymentMethodCreateParams.createCard(
            CardParams(
                number = card.cardNumber.replace(Regex("[^0-9]"), ""),
                expMonth = card.expMonth,
                expYear = card.expYear,
                cvc = card.cvv,
                name = card.cardHolder
            )
        )
    }

    private fun createPaymentApiResultCallback(): ApiResultCallback<PaymentIntentResult> {
        return object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                when {
                    paymentIntent.isConfirmed -> {
                        log("success")
                        callback.onSuccessPayment(amount = paymentIntent.amount, currency = paymentIntent.currency)
                    }
                    paymentIntent.status == StripeIntent.Status.Canceled -> {
                        log("canceled")
                        callback.onCancelPayment()
                    }
                    else -> {
                        val message = paymentIntent.lastErrorMessage ?: "${paymentIntent.status}"
                        log("status=${paymentIntent.status},  $message")
                        callback.onErrorPayment(message)
                    }
                }
            }

            override fun onError(e: Exception) {
                val message = e.localizedMessage ?: "Payment error"
                log("error=$message")
                callback.onErrorPayment(message)
            }
        }
    }

    private fun log(message: String) {
        Timber.tag("PaymentService").d(message)
    }

    interface Callback {
        fun onSuccessPayment(amount: Long?, currency: String?)
        fun onCancelPayment()
        fun onErrorPayment(error: String)
    }
}