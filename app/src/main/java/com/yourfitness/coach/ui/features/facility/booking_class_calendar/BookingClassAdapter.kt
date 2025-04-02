package com.yourfitness.coach.ui.features.facility.booking_class_calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.coach.domain.date.millisToLocalDate
import com.yourfitness.coach.ui.utils.ClassCalendarDayDiffCallback
import com.yourfitness.common.domain.models.ClassCalendarDayViewItem
import kotlinx.android.extensions.LayoutContainer
import java.time.LocalDate

class BookingClassAdapter(val listener: ClickListener) :
    ListAdapter<ClassCalendarDayViewItem, BookingClassAdapter.DayViewHolder>(
        ClassCalendarDayDiffCallback()
    ) {

    override fun getItemViewType(position: Int): Int = getItem(position).resource

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(position)
    }

    fun getHeaderLocalDateForPosition(position: Int): LocalDate? {
        return getItem(position)?.dateInMillis?.millisToLocalDate()
    }

    fun getPositionByLocalDate(date: LocalDate): Int {
        return currentList.indexOfFirst { it.dateInMillis?.millisToLocalDate() == date }
    }

    inner class DayViewHolder(
        override val containerView: View,
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

//        private val context = containerView.context
//
//        private val dayHeader = containerView.findViewById<TextView>(R.id.text_header_date)
//        private val divider = containerView.findViewById<View>(R.id.divider)
//        private val buttonAction = containerView.findViewById<MaterialButton>(R.id.button_action)
//        private val time = containerView.findViewById<TextView>(R.id.text_time)
//        private val coach = containerView.findViewById<TextView>(R.id.text_name)
//        private val date = containerView.findViewById<TextView>(R.id.text_date)
//        private val card = containerView.findViewById<View>(R.id.view_card)
//        private val statusBooked = containerView.findViewById<MaterialTextView>(R.id.text_status_booked)
//        private val status = containerView.findViewById<MaterialTextView>(R.id.text_status)
//        private val noClassesBooked = containerView.findViewById<TextView>(R.id.text_no_classes_booked_day)
//
//        private val colorBlack = context.getColorCompat(common.color.black)
//        private val colorMainActive = context.getColorCompat(common.color.main_active)
//        private val colorGrayLight = context.getColorCompat(common.color.gray_light)
//
//        private val grayLightTintList = ContextCompat.getColorStateList(context, common.color.gray_light)
//        private val mainActiveTintList = ContextCompat.getColorStateList(context, common.color.main_active)
//        private val issueRedTintList = ContextCompat.getColorStateList(context, common.color.issue_red)
//
//        private val nowSeconds by lazy { today().time.toSeconds() }

        fun bind(position: Int) {
           /* when (val item = getItem(position)) {
                is ClassCalendarDayViewItem.HeaderItem -> {
                    dayHeader.text = item.dateInMillis.toDate().formatEeeeMmmDd()
                }
                is ClassCalendarDayViewItem.Item -> {
                    card.isVisible = item.day == 0L
                    noClassesBooked.isVisible = item.day != 0L
                    time.text = item.datetimeInSeconds?.toDateTime()
                    coach.text = item.instructor
                    date.text = item.datetimeInSeconds?.toDateDayOfWeekMonth()
                    statusBooked.isVisible = item.isBooked == true
                    status.isInvisible = item.isBooked == true
                    when {
                        nowSeconds > (item.datetimeInSeconds ?: 0)
                                || item.isNotAvailable == true -> bindUnbookedInactiveState(item)
                        item.isBooked == true -> bindBookedState(item)
                        else -> bindUnbookedState(item)
                    }
                    divider.isVisible = item.day == 0L &&
                            (position < currentList.size - 1 && currentList[position + 1] is ClassCalendarDayViewItem.Item)
                }
            }*/
        }

//        private fun bindUnbookedState(item: ClassCalendarDayViewItem.Item) {
//            if ((item.availableSpots ?: 0) > 0) {
//                status.isSelected = true
//                time.setTextColor(colorBlack)
//                coach.setTextColor(colorBlack)
//                status.setTextColor(colorMainActive)
//                status.text = context.getString(R.string.spots_available, item.availableSpots ?: 0)
//                buttonAction.backgroundTintList = mainActiveTintList
//                buttonAction.setOnClickListener { listener.onBookClassClick(item) }
//                buttonAction.text = context.getString(R.string.btn_book_credits, item.credits ?: 0)
//                buttonAction.isClickable = true
//            } else {
//                bindUnbookedInactiveState(item)
//            }
//        }
//
//        private fun bindUnbookedInactiveState(item: ClassCalendarDayViewItem.Item) {
//            time.setTextColor(colorGrayLight)
//            coach.setTextColor(colorGrayLight)
//            status.isSelected = false
//            status.setTextColor(colorGrayLight)
//            status.setText(R.string.no_spots_available)
//            buttonAction.backgroundTintList = grayLightTintList
//            buttonAction.setOnClickListener(null)
//            buttonAction.text = context.getString(R.string.btn_book_credits, item.credits ?: 0)
//            buttonAction.isClickable = false
//        }
//
//        private fun bindBookedState(item: ClassCalendarDayViewItem.Item) {
//            buttonAction.setText(common.string.btn_cancel)
//            time.setTextColor(colorBlack)
//            coach.setTextColor(colorBlack)
//            if (item.isCancelAvailable == true) {
//                buttonAction.backgroundTintList = issueRedTintList
//                buttonAction.setOnClickListener { listener.onCancelClassClick(item) }
//                buttonAction.isClickable = true
//            } else {
//                bindBookedInactiveState(item)
//            }
//        }
//
//        private fun bindBookedInactiveState(item: ClassCalendarDayViewItem.Item) {
//            statusBooked.setTextColor(colorGrayLight)
//            buttonAction.backgroundTintList = grayLightTintList
//            buttonAction.setOnClickListener(null)
//            buttonAction.isClickable = false
//        }
    }

    interface ClickListener {
        fun onBookClassClick(bookedClass: ClassCalendarDayViewItem.Item)
        fun onCancelClassClick(bookedClass: ClassCalendarDayViewItem.Item)
    }
}
