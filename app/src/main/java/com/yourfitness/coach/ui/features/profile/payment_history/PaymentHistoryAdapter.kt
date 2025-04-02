package com.yourfitness.coach.ui.features.profile.payment_history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.date.formatDateMmDd
import com.yourfitness.coach.domain.date.formatDateMmmDd
import com.yourfitness.coach.domain.models.PaymentItems
import com.yourfitness.coach.domain.models.ViewItem
import com.yourfitness.coach.ui.utils.PaymentHistoryDiffCallback
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.year
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.setCompoundDrawables
import kotlinx.android.extensions.LayoutContainer
import com.yourfitness.common.R as common

class PaymentHistoryAdapter(
    private val onItemClick: (paymentIntent: String) -> Unit,
) : ListAdapter<ViewItem, ViewHolder>(PaymentHistoryDiffCallback()) {

    override fun getItemViewType(position: Int): Int = currentList[position].resource

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.size
}

class ViewHolder(
    override val containerView: View,
    private val onItemClick: (paymentIntent: String) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    private val dateHeader = containerView.findViewById<TextView>(common.id.text_header_date)
    private val dateItem = containerView.findViewById<TextView>(R.id.text_date)
    private val aedAmount = containerView.findViewById<TextView>(R.id.text_aed_amount)
    private val type = containerView.findViewById<TextView>(R.id.text_type)

    fun bind(item: ViewItem) {
        when (item) {
            is ViewItem.HeaderItem -> {
                if (item.isYear) {
                    dateHeader.text = item.date?.toDate()?.year().toString()
                } else {
                    dateHeader.text = item.date.toDate().formatDateMmmDd()
                }
            }
            is ViewItem.Item -> {
                when (item.title) {
                    PaymentItems.MEMBERSHIP.value -> {
                        type.text = containerView.context.getString(R.string.profile_screen_subscription)
                        type.setCompoundDrawables(start = null)
                    }

                    PaymentItems.CREDITS.value -> {
                        type.text = containerView.context.resources.getQuantityString(
                            R.plurals.credits,
                            item.boughtNumber ?: 0,
                            item.boughtNumber
                        )
                        type.setCompoundDrawables(start = com.yourfitness.common.R.drawable.ic_profile_coins)
                    }

                    PaymentItems.PT_SESSIONS.value -> {
                        type.text = containerView.context.resources.getQuantityString(
                            com.yourfitness.pt.R.plurals.sessions_number,
                            item.boughtNumber ?: 0,
                            item.boughtNumber
                        )
                        type.setCompoundDrawables(start = com.yourfitness.pt.R.drawable.ic_barbell)
                    }

                    PaymentItems.ONE_TIME.value -> {
                        val duration = item.duration ?: 0
                        val durationText = containerView.context.resources.getQuantityString(
                            R.plurals.number_of_months,
                            duration,
                            duration,
                        )
                        type.text = containerView.context.getString(
                            R.string.payment_history_screen_one_time_subscription_text,
                            durationText
                        )
                        type.setCompoundDrawables(start = null)
                    }
                }

                dateItem.text = item.created
                aedAmount.text = item.amount?.formatAmount(item.currency.uppercase())
                itemView.setOnClickListener { onItemClick(item.paymentIntent ?: "") }
            }
        }
    }
}