package com.amazonaws.ivs.player.scrollablefeed.views.heartView

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.amazonaws.ivs.player.scrollablefeed.R

/**
 * Heart imageView
 * @param color heart color
 * @return heart imageView object
 */
fun ImageView.createHeartView(color: Int): ImageView {
    val heart: Bitmap? = createHeart(context, color)
    setImageDrawable(BitmapDrawable(resources, heart))
    return this
}

/**
 * Heart bitmap
 * @param context context
 * @param color bitmap color
 * @return heart bitmap
 */
private fun createHeart(context: Context, color: Int): Bitmap? {
    val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_favorite, null)
    val bitmap = drawable?.toBitmap()
    val canvas = bitmap?.let { Canvas(it) }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_ATOP)
    }
    canvas?.let {
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }
    return bitmap
}
