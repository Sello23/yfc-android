package com.yourfitness.pt.ui.features.details.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.pt.data.entity.EducationEntity
import com.yourfitness.pt.databinding.ItemQualificationListBinding

class QualificationAdapter : RecyclerView.Adapter<QualificationInfoViewHolder>() {
    private val qualifications = arrayListOf<EducationEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QualificationInfoViewHolder {
        val binding =
            ItemQualificationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QualificationInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QualificationInfoViewHolder, position: Int) {
        holder.bind(qualifications[position])
    }

    override fun getItemCount(): Int = qualifications.size

    fun setData(challengesList: List<EducationEntity>) {
        this.qualifications.clear()
        this.qualifications.addAll(challengesList)
    }
}

class QualificationInfoViewHolder(binding: ItemQualificationListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemQualificationListBinding = ItemQualificationListBinding.bind(itemView)

    fun bind(item: EducationEntity) {
        binding.textName.text = item.qualification
        binding.address.text = item.institute
        binding.year.text = item.year.toString()
    }
}
