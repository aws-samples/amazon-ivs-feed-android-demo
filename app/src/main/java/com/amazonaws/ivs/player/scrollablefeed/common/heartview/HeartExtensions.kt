package com.amazonaws.ivs.player.scrollablefeed.common.heartview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.common.HEART_ANIMATION_DURATION
import com.amazonaws.ivs.player.scrollablefeed.common.HEART_ANIMATION_FACTOR

fun ImageView.createHeartView(color: Int): ImageView {
    val heart: Bitmap? = context.createHeart(color)
    setImageDrawable(BitmapDrawable(resources, heart))
    return this
}

fun TypedArray.toHeartConfig(): HeartConfig {
    return HeartConfig(
        x = getDimension(R.styleable.HeartLayout_initX, resources.getDimensionPixelOffset(R.dimen.heart_anim_init_x).toFloat()),
        y = getDimension(R.styleable.HeartLayout_initY, resources.getDimensionPixelOffset(R.dimen.heart_anim_init_y).toFloat()),
        rand = getDimension(R.styleable.HeartLayout_xRand, resources.getDimensionPixelOffset(R.dimen.heart_anim_bezier_x_rand).toFloat()).toInt(),
        animLengthRand = getDimension(R.styleable.HeartLayout_animLengthRand, resources.getDimensionPixelOffset(R.dimen.heart_anim_length_rand).toFloat()).toInt(),
        factor = getDimension(R.styleable.HeartLayout_bezierFactor, HEART_ANIMATION_FACTOR.toFloat()).toInt(),
        point = getDimension(R.styleable.HeartLayout_xPointFactor, resources.getDimensionPixelOffset(R.dimen.heart_anim_x_point_factor).toFloat()).toInt(),
        animLength = getDimension(R.styleable.HeartLayout_animLength, resources.getDimensionPixelOffset(R.dimen.heart_anim_length).toFloat()).toInt(),
        heartWidth = getDimension(R.styleable.HeartLayout_heart_width, resources.getDimensionPixelOffset(R.dimen.heart_size_width).toFloat()).toInt(),
        heartHeight = getDimension(R.styleable.HeartLayout_heart_height, resources.getDimensionPixelOffset(R.dimen.heart_size_height).toFloat()).toInt(),
        animDuration = getDimension(R.styleable.HeartLayout_anim_duration, HEART_ANIMATION_DURATION.toFloat()).toInt()
    )
}

private fun Context.createHeart(color: Int): Bitmap? {
    val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite, null)
    val bitmap = drawable?.toBitmap()
    val canvas = bitmap?.let { Canvas(it) }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this@createHeart, color), PorterDuff.Mode.SRC_ATOP)
    }
    canvas?.let {
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }
    return bitmap
}
