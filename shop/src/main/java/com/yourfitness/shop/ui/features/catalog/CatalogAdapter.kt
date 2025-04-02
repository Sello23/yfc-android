package com.yourfitness.shop.ui.features.catalog

import android.content.res.Resources
import android.graphics.Rect
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.shop.R
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.databinding.ItemCatalogBinding
import com.yourfitness.shop.domain.model.CatalogCard
import com.yourfitness.shop.ui.utils.GoodsDiffCallback

class CatalogAdapter(
    private val onCardClick: (cardTitle: String) -> Unit,
    @ColorInt private val highlightColor: Int,
    private val onFavoriteClick: (Int, String, Boolean) -> Unit,
    private val type: ProductTypeId
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val catalogCardList = arrayListOf<CatalogCard>()
    private var query: String? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (type == ProductTypeId.SERVICES) {
            ServicesViewHolder(binding, onCardClick, onFavoriteClick, highlightColor)
        } else {
            CatalogViewHolder(binding, onCardClick, onFavoriteClick, highlightColor)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CatalogViewHolder) {
            holder.bind(catalogCardList[position], query)
        } else if (holder is ServicesViewHolder) {
            holder.bind(catalogCardList[position], query)
        }
    }

    override fun getItemCount(): Int = catalogCardList.size

    fun setData(catalogCard: List<CatalogCard>, query: String? = null) {
        val diffCallBack = GoodsDiffCallback(catalogCardList, catalogCard)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.catalogCardList.clear()
        this.catalogCardList.addAll(catalogCard)
        diffResult.dispatchUpdatesTo(this)
        this.query = query
    }

    fun addData(catalogCard: List<CatalogCard>) {
        val diffCallBack = GoodsDiffCallback(catalogCardList, catalogCard)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.catalogCardList.addAll(catalogCard)
        diffResult.dispatchUpdatesTo(this)
    }
}

open class CatalogViewHolder(
    binding: ItemCatalogBinding,
    private val onCardClick: (productId: String) -> Unit,
    private val onFavoriteClick: (Int, String, Boolean) -> Unit,
    @ColorInt private val highlightColor: Int,
) : RecyclerView.ViewHolder(binding.root) {

    protected val binding: ItemCatalogBinding = ItemCatalogBinding.bind(itemView)
    open fun bind(catalogCard: CatalogCard, query: String?) {
        itemView.setOnClickListener { onCardClick(catalogCard.id) }
        binding.itemTitle.text = highlightSubstring(highlightColor, catalogCard.title, query?.trim())
        binding.itemSubtitle.text = catalogCard.subtitle

        binding.discountGroup.isVisible = catalogCard.priceCoinsPart > 0L
        binding.itemPrice.text = if (catalogCard.priceCoinsPart > 0L) catalogCard.discountPrice
                                 else catalogCard.price.uppercase()
        binding.itemPrice.setTextColorRes(
            if (catalogCard.priceCoinsPart > 0L) com.yourfitness.common.R.color.issue_red
            else com.yourfitness.common.R.color.text_gray
        )
        binding.itemOldPrice.text = catalogCard.price.uppercase()
        binding.itemOldPrice.paintFlags = binding.itemOldPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.textCoins.text = catalogCard.priceCoinsPart.toString()
        Glide.with(binding.root).load(catalogCard.image).into(binding.image)
        binding.favoriteIcon.isSelected = catalogCard.isFavorite
        binding.favoriteIcon.setOnClickListener {
            onFavoriteClick(catalogCard.dbId, catalogCard.id, !catalogCard.isFavorite)
            binding.favoriteIcon.isSelected = !catalogCard.isFavorite
        }
    }


    private fun highlightSubstring(@ColorInt color: Int, string: String, text: String?): CharSequence {
        if (text == null) return string
        val index = string.lowercase().indexOf(text.lowercase())
        val result = SpannableStringBuilder(string)
        return if (index >= 0) {
            result.setSpan(
                BackgroundColorSpan(color),
                index,
                index + text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            result
        } else {
            string
        }
    }
}

class ServicesViewHolder(
    binding: ItemCatalogBinding,
    onCardClick: (productId: String) -> Unit,
    onFavoriteClick: (Int, String, Boolean) -> Unit,
    @ColorInt highlightColor: Int,
) : CatalogViewHolder(binding, onCardClick, onFavoriteClick, highlightColor) {
    override fun bind(catalogCard: CatalogCard, query: String?) {
        super.bind(catalogCard, query)

        val resources = binding.root.context.resources
        binding.itemTitle.setTextAppearance(
            binding.root.context,
            com.yourfitness.common.R.style.TextAppearance_YFC_Subheading7
        )
        binding.image.layoutParams.height = resources.getDimensionPixelSize(R.dimen.service_image_height)
        binding.itemSubtitle.visibility = View.GONE
        binding.serviceName.isVisible = true
        binding.serviceLogo.isVisible = true
        binding.distanceTo.isVisible = true
        val padding1x = resources.getDimension(com.yourfitness.common.R.dimen.spacing_0_625x).toInt()
        val padding075x = resources.getDimension(com.yourfitness.common.R.dimen.spacing_0_75x).toInt()
        binding.itemTitle.setPadding(
            padding1x,
            0,
            padding1x,
            padding075x
        )
        binding.serviceName.text = catalogCard.subtitle
        if (catalogCard.vendorImageId != null) {
            Glide.with(binding.root).load(catalogCard.vendorImageId).into(binding.serviceLogo)
        }
        binding.distanceTo.text = catalogCard.distance
    }
}

class SpacesItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildLayoutPosition(view) % 2 == 0) {
            outRect.right = 3.toPx()
        } else {
            outRect.left = 3.toPx()
        }
    }
}

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toPx() = this * Resources.getSystem().displayMetrics.density
