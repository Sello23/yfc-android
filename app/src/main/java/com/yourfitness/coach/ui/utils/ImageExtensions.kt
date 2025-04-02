package com.yourfitness.coach.ui.utils

import android.graphics.*
import android.view.View
import androidx.core.view.drawToBitmap


fun Bitmap.rotate(degree: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degree) }
    val output = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return output
}

fun Bitmap.circle(): Bitmap {
    val output = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(output)
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawCircle(width / 2.0f, height / 2.0f, width / 2.0f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

fun Bitmap.overlay(overlay: Bitmap, inset: Float): Bitmap {
    val output = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(output)
    canvas.drawBitmap(this, 0f, 0f, null)
    canvas.drawBitmap(overlay, inset, inset, null)
    return output
}

fun View.toBitmap(): Bitmap {
    val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(spec, spec)
    layout(0, 0, measuredWidth, measuredHeight)
    return drawToBitmap()
//    val output = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(output)
//    draw(canvas)
//    return output
}