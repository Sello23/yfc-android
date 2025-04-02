package com.yourfitness.shop.ui.features.orders.cart

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.shop.R
import com.yourfitness.shop.ui.features.catalog.toPx
import kotlin.math.absoluteValue

abstract class SwipeToDeleteCallback(val context: Context) : ItemTouchHelper.Callback() {
    private val clearPaint: Paint = Paint()
    private val background: ColorDrawable = ColorDrawable()
    private val cardBackground: ColorDrawable = ColorDrawable()
    private val backgroundColor = ContextCompat.getColor(context, R.color.card_swipe_background)
    private val cardBackgroundColor =
        ContextCompat.getColor(context, com.yourfitness.common.R.color.gray_background)
    private val cardBackgroundDefaultColor =
        ContextCompat.getColor(context, com.yourfitness.common.R.color.white)
    private val deleteDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private var intrinsicWidth = deleteDrawable?.intrinsicWidth ?: 0
    private var intrinsicHeight = deleteDrawable?.intrinsicHeight ?: 0

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView: View = viewHolder.itemView
        val childItemView: View = (viewHolder.itemView as ViewGroup)[0]
        val itemHeight: Int = itemView.height

        val isCancelled = dX == 0f && !isCurrentlyActive
        if (isCancelled) {
            cardBackground.color = cardBackgroundDefaultColor
            childItemView.background = cardBackground

            c.drawRect(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                clearPaint
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false)
            return
        }

        background.color = backgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top + 16.toPx(),
            itemView.right,
            itemView.bottom - 16.toPx()
        )
        background.draw(c)

        cardBackground.color = cardBackgroundColor
        childItemView.background = cardBackground

        val availableWidth = dX.toInt().absoluteValue
        val textStartPadding = 4.toPx()

        val deleteIconTop: Int = itemView.top + (itemHeight - intrinsicHeight) / 2
        var deleteIconRight: Int = itemView.right - (dX.toInt().absoluteValue - intrinsicWidth) / 2
        var deleteIconLeft: Int = deleteIconRight - intrinsicWidth
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        val textPaint = TextPaint()
        textPaint.color = ContextCompat.getColor(context, com.yourfitness.common.R.color.white)
        textPaint.textSize = 15.toPx().toFloat()
        textPaint.typeface = Typeface.DEFAULT_BOLD
        val text = context.getString(R.string.delete)
        val textWidth = textPaint.measureText(text).toInt()

        if (availableWidth > intrinsicWidth + textStartPadding + textWidth) {
            val textOffset = (textStartPadding + textWidth) / 2
            deleteIconRight -= textOffset
            deleteIconLeft -= textOffset

            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)

            val textHeight = bounds.height()
            val textX: Int = deleteIconRight + textStartPadding
            val textY: Int = itemView.bottom - (itemView.bottom - itemView.top - textHeight) / 2

            c.drawText(
                text,
                textX.toFloat(),
                textY.toFloat(),
                textPaint
            )
        }

        deleteDrawable?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }
}
