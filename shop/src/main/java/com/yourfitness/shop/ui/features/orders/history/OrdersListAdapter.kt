package com.yourfitness.shop.ui.features.orders.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.ItemOrderSummaryBinding
import com.yourfitness.shop.databinding.ItemOrdersListBinding
import com.yourfitness.shop.databinding.ItemProductLogoBinding
import com.yourfitness.shop.databinding.ItemServiceOrdersSummaryBinding

class OrdersListAdapter(
    private val onOrderClick: (orderId: String) -> Unit,
    private val loadColor: (view: View, color: Int) -> Unit,
    private val onClaimClick: ((orderId: String) -> Unit)? = null
) : RecyclerView.Adapter<OrderViewHolder>() {

    private val dataList = mutableMapOf<String, List<OrderData>>()
    private var orderClassification: OrderClassification = OrderClassification.GOODS

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, onOrderClick, onClaimClick, loadColor)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(dataList.entries.toList()[position], orderClassification)
    }

    override fun getItemCount(): Int = dataList.size

    fun setData(data: Map<String, List<OrderData>>, classification: OrderClassification) {
        this.dataList.clear()
        this.dataList.putAll(data)
        orderClassification = classification
    }
}

class OrderViewHolder(
    binding: ItemOrdersListBinding,
    private val onOrderClick: (orderId: String) -> Unit,
    private val onClaimClick: ((orderId: String) -> Unit)? = null,
    private val loadColor: (view: View, color: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private var adapter = OrderItemAdapter(onOrderClick, onClaimClick, loadColor)
    private val binding: ItemOrdersListBinding = ItemOrdersListBinding.bind(itemView)
    private var classification: OrderClassification = OrderClassification.GOODS

    init {
        binding.ordersRecyclerView.adapter = adapter
    }

    fun bind(data: MutableMap.MutableEntry<String, List<OrderData>>, classification: OrderClassification) {
        binding.dateSeparatorContainer.separatorDate.text = data.key
        if (this.classification != classification) {
            adapter = OrderItemAdapter(onOrderClick, onClaimClick, loadColor)
            binding.ordersRecyclerView.adapter = adapter
        }
        adapter.setData(data.value, classification)
        adapter.notifyDataSetChanged()
        this.classification = classification
    }
}

class OrderItemAdapter(
    private val onOrderClick: (orderId: String) -> Unit,
    private val onClaimClick: ((orderId: String) -> Unit)? = null,
    private val loadColor: (view: View, color: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ordersList = arrayListOf<OrderData>()
    private var orderClassification: OrderClassification = OrderClassification.GOODS

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (orderClassification == OrderClassification.GOODS) {
            val binding = ItemOrderSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OrderItemViewHolder(binding, onOrderClick, onClaimClick, loadColor)
        } else {
            val binding = ItemServiceOrdersSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ServiceOrderItemViewHolder(binding, onOrderClick, onClaimClick, loadColor)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderItemViewHolder) {
            holder.bind(ordersList[position] as GoodsOrderData)
        } else if (holder is ServiceOrderItemViewHolder) {
            holder.bind(ordersList[position] as ServiceOrderData)
        }
    }

    override fun getItemCount(): Int = ordersList.size

    fun setData(orders: List<OrderData>, classification: OrderClassification) {
        this.ordersList.clear()
        this.ordersList.addAll(orders)
        orderClassification = classification
    }
}

class OrderItemViewHolder(
    binding: ItemOrderSummaryBinding,
    private val onOrderClick: (orderId: String) -> Unit,
    private val onClaimClick: ((orderId: String) -> Unit)? = null,
    private val loadColor: (view: View, color: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemOrderSummaryBinding = ItemOrderSummaryBinding.bind(itemView)

    fun bind(order: GoodsOrderData) {
        binding.statusLabel.text = order.statusText.uppercase()
        loadColor(binding.statusLabel, getStatusBgColor(order.status))
        binding.orderNumberValue.text = order.orderNumber
        binding.orderDateValue.text = order.orderDate
        setupOrderImagesArea(order.images)

        binding.orderDetails.setOnClickListener {
            onOrderClick(order.orderId)
        }
    }

    private fun setupOrderImagesArea(images: List<String>) {
        val itemsLimit = 3
        binding.itemsImages.removeAllViews()
        for (i in images.indices) {
            val imagesContainer = ItemProductLogoBinding.inflate(LayoutInflater.from(itemView.context), binding.root, false)
            Glide.with(imagesContainer.root).load(images[i].toImageUri()).into(imagesContainer.ivImage)
            if (i == itemsLimit) {
                imagesContainer.imageLimit.isVisible = true
                imagesContainer.imageLimit.text = "+${images.size - itemsLimit}"
                binding.itemsImages.addView(imagesContainer.root)
                break
            }
            binding.itemsImages.addView(imagesContainer.root)
        }
    }

    private fun getStatusBgColor(status: Int): Int {
        return when (status) {
            0 -> com.yourfitness.common.R.color.main_active
            1 -> com.yourfitness.common.R.color.yellow
            2 -> com.yourfitness.common.R.color.issue_red
            3 -> R.color.status_gb_fulfilled
            else -> com.yourfitness.common.R.color.grey
        }
    }
}

class ServiceOrderItemViewHolder(
    binding: ItemServiceOrdersSummaryBinding,
    private val onOrderClick: (orderId: String) -> Unit,
    private val onClaimClick: ((orderId: String) -> Unit)? = null,
    private val loadColor: (view: View, color: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemServiceOrdersSummaryBinding = ItemServiceOrdersSummaryBinding.bind(itemView)

    fun bind(order: ServiceOrderData) {
        binding.vendorName.text = order.service
        binding.statusLabel.text = order.statusText.uppercase()
        loadColor(binding.statusLabel, getStatusBgColor(order.status))
        Glide.with(binding.root).load(order.image.toImageUri()).into(binding.serviceImage)
        Glide.with(binding.root).load(order.logo.toImageUri()).into(binding.serviceLogo)
        binding.name.text = order.name
        binding.textAddress.text = order.address
        binding.textPhone.text = order.phone

        binding.voucherDetails.setOnClickListener {
            onOrderClick(order.orderId)
        }

        binding.voucherClaim.isVisible = order.statusText.lowercase() == UNCLAIMED
        binding.voucherClaim.setOnClickListener {
            onClaimClick?.invoke(order.orderId)
        }
    }

    private fun getStatusBgColor(status: Int): Int {
        return when (status) {
            0 -> R.color.status_gb_fulfilled
            1 -> com.yourfitness.common.R.color.main_active
            2 -> com.yourfitness.common.R.color.issue_red
            else -> com.yourfitness.common.R.color.grey
        }
    }
}

