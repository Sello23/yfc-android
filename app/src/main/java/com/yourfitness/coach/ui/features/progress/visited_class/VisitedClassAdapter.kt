package com.yourfitness.coach.ui.features.progress.visited_class

import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.databinding.ItemVisitedStudioBinding
import com.yourfitness.coach.domain.models.VisitedClass
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.R as common

class VisitedClassAdapter(
    private val onItemClick: (facility: FacilityEntity) -> Unit
) : RecyclerView.Adapter<VisitedClassViewHolder>() {

    private val visitedClass = arrayListOf<VisitedClass>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitedClassViewHolder {
        val binding = ItemVisitedStudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitedClassViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: VisitedClassViewHolder, position: Int) {
        holder.bind(visitedClass[position])
    }

    override fun getItemCount(): Int = visitedClass.size

    fun setData(item: List<VisitedClass>) {
        visitedClass.clear()
        visitedClass.addAll(item)
    }
}

class VisitedClassViewHolder(
    binding: ItemVisitedStudioBinding,
    private val onItemClick: (facility: FacilityEntity) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemVisitedStudioBinding = ItemVisitedStudioBinding.bind(itemView)
    fun bind(visitedClass: VisitedClass) {
        Glide.with(itemView.context).load(visitedClass.studioIcon.toImageUri()).into(binding.imageIcon)
        if (visitedClass.currentVisits > visitedClass.visitsToNextLevel) {
            binding.textNextLevel.isVisible = false
            binding.imageYfcLogoNext.isVisible = false
            binding.textNextLevelCredits.isVisible = false
            binding.viewEllipse.isVisible = false
            binding.progressBar.progress = 100
            binding.progressVisits.setProgress(visitedClass.currentVisits.toDouble(), visitedClass.currentVisits.toDouble())
            binding.textVisitsProgress.text = itemView.context.getString(R.string.progress_screen_ring_progress_text, "100")
            binding.textVisits.setTextColor(itemView.context.resources.getColor(common.color.blue))
            binding.textVisits.text = itemView.context.getString(R.string.progress_screen_visit_text, visitedClass.currentVisits)
        } else {
            binding.progressBar.progress = visitedClass.progressbarPosition
            binding.progressVisits.setProgress(visitedClass.currentVisits.toDouble(), visitedClass.visitsToNextLevel.toDouble())
            binding.textVisitsProgress.text = itemView.context.getString(R.string.progress_screen_ring_progress_text, visitedClass.ringProgress)
            val spannableString = SpannableString(
                itemView.context.getString(
                    R.string.progress_screen_visits_text,
                    visitedClass.currentVisits,
                    visitedClass.visitsToNextLevel
                )
            )
            val end = spannableString.indexOf("/")
            val foregroundSpan = ForegroundColorSpan(itemView.context.resources.getColor(common.color.blue))
            spannableString.setSpan(foregroundSpan, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textVisits.text = spannableString
        }
        binding.textStudioName.text = visitedClass.studioName
        binding.textCurrentStudioLevel.text = visitedClass.studioCurrentLevel
        binding.textCurrentStudioLevel.text = visitedClass.studioCurrentLevel
        binding.textNextLevel.text = visitedClass.studioNextLevel
        binding.textNextLevelCredits.text = visitedClass.creditsLevelUpBonus
        binding.textAvailableBonusCreditsAmount.text = visitedClass.availableBonusCredits
        binding.textBookClass.setOnClickListener { onItemClick(visitedClass.facilityEntity) }
        binding.imageYfcLogoCurrent.setColorFilter(Color.parseColor(visitedClass.color), PorterDuff.Mode.SRC_ATOP)
        binding.imageYfcLogoNext.setColorFilter(Color.parseColor(visitedClass.nextLevelColor), PorterDuff.Mode.SRC_ATOP)
    }
}