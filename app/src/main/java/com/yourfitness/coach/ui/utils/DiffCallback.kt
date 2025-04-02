package com.yourfitness.coach.ui.utils

import androidx.recyclerview.widget.DiffUtil
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.domain.models.ViewItem
import com.yourfitness.common.domain.models.CalendarDayViewItem
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.domain.models.ClassCalendarDayViewItem

class ManualSearchDiffCallback(
    private val oldList: List<FacilityEntity>,
    private val newList: List<FacilityEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

class PaymentHistoryDiffCallback : DiffUtil.ItemCallback<ViewItem>() {

    override fun areItemsTheSame(oldItem: ViewItem, newItem: ViewItem): Boolean {
        val isSameRepoItem = oldItem is ViewItem.Item
                && newItem is ViewItem.Item
                && oldItem.paymentIntent == newItem.paymentIntent
        val isSameSeparatorItem = oldItem is ViewItem.HeaderItem
                && newItem is ViewItem.HeaderItem
                && oldItem.date == newItem.date
        return isSameRepoItem || isSameSeparatorItem
    }

    override fun areContentsTheSame(oldItem: ViewItem, newItem: ViewItem) = oldItem == newItem
}

class MonthDiffCallback(
    private val oldList: List<BookedClass>,
    private val newList: List<BookedClass>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].time == newList[newItemPosition].time
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

class CalendarWeekDiffCallback : DiffUtil.ItemCallback<CalendarView>() {

    override fun areItemsTheSame(
        oldItem: CalendarView,
        newItem: CalendarView
    ): Boolean {
        val isSameRepoItem = oldItem is CalendarView.CalendarItem
                && newItem is CalendarView.CalendarItem
                && oldItem.date == newItem.date
        val isSameSeparatorItem = oldItem is CalendarView.Header
                && newItem is CalendarView.Header
                && oldItem.startDate == newItem.startDate
        return isSameRepoItem || isSameSeparatorItem
    }

    override fun areContentsTheSame(oldItem: CalendarView, newItem: CalendarView) =
        oldItem == newItem
}

class CalendarDayDiffCallback : DiffUtil.ItemCallback<CalendarDayViewItem>() {

    override fun areItemsTheSame(
        oldItem: CalendarDayViewItem,
        newItem: CalendarDayViewItem
    ): Boolean {
        val isSameRepoItem = oldItem is CalendarDayViewItem.Item
                && newItem is CalendarDayViewItem.Item
                && oldItem.date == newItem.date
        val isSameSeparatorItem = oldItem is CalendarDayViewItem.HeaderItem
                && newItem is CalendarDayViewItem.HeaderItem
                && oldItem.date == newItem.date
        return isSameRepoItem || isSameSeparatorItem
    }

    override fun areContentsTheSame(oldItem: CalendarDayViewItem, newItem: CalendarDayViewItem) =
        oldItem == newItem
}

class ClassCalendarDayDiffCallback : DiffUtil.ItemCallback<ClassCalendarDayViewItem>() {

    override fun areItemsTheSame(
        oldItem: ClassCalendarDayViewItem,
        newItem: ClassCalendarDayViewItem
    ): Boolean {
        val isSameRepoItem = oldItem is ClassCalendarDayViewItem.Item
                && newItem is ClassCalendarDayViewItem.Item
                && oldItem.datetimeInSeconds == newItem.datetimeInSeconds
        val isSameSeparatorItem = oldItem is ClassCalendarDayViewItem.HeaderItem
                && newItem is ClassCalendarDayViewItem.HeaderItem
                && oldItem.dateInMillis == newItem.dateInMillis
        return isSameRepoItem || isSameSeparatorItem
    }

    override fun areContentsTheSame(oldItem: ClassCalendarDayViewItem, newItem: ClassCalendarDayViewItem) =
        oldItem == newItem
}

class LeaderboardDiffCallback(
    private val oldList: List<LeaderboardEntity>,
    private val newList: List<LeaderboardEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].profileId == newList[newItemPosition].profileId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}