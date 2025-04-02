package com.yourfitness.common.ui.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.databinding.ItemGalleryBinding
import com.yourfitness.common.ui.utils.BindingHolder
import com.yourfitness.common.ui.utils.toImageUri

class GalleryAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<BindingHolder<ItemGalleryBinding>>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemGalleryBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGalleryBinding.inflate(inflater, parent, false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemGalleryBinding>, position: Int) {
        val binding = holder.binding
        val item = items[holder.absoluteAdapterPosition]
        Glide.with(binding.root).load(item.toImageUri()).into(binding.imageGallery)
    }
}

