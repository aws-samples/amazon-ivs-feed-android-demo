package com.amazonaws.ivs.player.scrollablefeed.common.heartview

import android.graphics.Path
import android.graphics.PathMeasure
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class FloatAnimation(
    path: Path,
    private val rotation: Float,
    parent: View,
    private val child: View
) : Animation() {

    private val pathMeasure: PathMeasure = PathMeasure(path, false)
    private val distance: Float = pathMeasure.length

    init {
        parent.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
        pathMeasure.getMatrix(distance * interpolatedTime, transformation?.matrix, PathMeasure.POSITION_MATRIX_FLAG)
        child.rotation = rotation * interpolatedTime
        val scale = 1F - (interpolatedTime / 4)
        child.apply {
            scaleX = scale
            scaleY = scale
        }
        transformation?.alpha = 1.0F - interpolatedTime
    }
}
