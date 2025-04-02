package com.yourfitness.pt.ui.features.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.ViewDashboardCardBinding
import java.lang.Integer.min

class PtCartAdapter(private val cardList: List<Pair<Int, String>>) : RecyclerView.Adapter<CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            ViewDashboardCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.bind(cardList[position], dashboardCards[min(dashboardCards.size - 1, position)])
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {}

    override fun getItemCount(): Int = cardList.size

    companion object {
        private val dashboardCards = listOf(
            DashboardCard(
                { _, value, _ -> value.toString() },
                R.string.total_sessions,
                R.drawable.ic_total_sessions
            ),
            DashboardCard(
                { _, value, _ -> value.toString() },
                R.string.clients,
                R.drawable.ic_clients
            ),
            DashboardCard(
                { context, value, _ -> context.getString(R.string.percent_value, value) },
                R.string.commission,
                R.drawable.ic_commission
            ),
            DashboardCard(
                { _, value, currency -> value.formatAmount(currency).uppercase() },
                R.string.total_commissions,
                R.drawable.ic_total_commissions
            ),
        )
    }
}

class CartViewHolder(binding: ViewDashboardCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val binding: ViewDashboardCardBinding = ViewDashboardCardBinding.bind(itemView)

    fun bind(cardData: Pair<Int, String>, card: DashboardCard) {
        val context = binding.root.context
        binding.apply {
            title.text = card.valueBuilder(context, cardData.first, cardData.second)
            subtitle.text = context.getString(card.subtitle)
            Glide.with(root).load(ContextCompat.getDrawable(context, card.icon)).into(icon)
        }
    }
}

data class DashboardCard(
    val valueBuilder: (Context, Int, String) -> String,
    @StringRes val subtitle: Int,
    @DrawableRes val icon: Int
)
