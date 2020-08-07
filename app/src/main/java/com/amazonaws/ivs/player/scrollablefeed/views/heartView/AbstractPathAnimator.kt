package com.amazonaws.ivs.player.scrollablefeed.views.heartView

import android.content.res.TypedArray
import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration
import java.util.*

/**
 * Abstract path animator class
 */
abstract class AbstractPathAnimator {

    lateinit var config: Config

    fun randomAnimation(): Float = Random().nextFloat()

    fun createPath(view: View, factor: Int): Path {
        val rand = Random()
        var x = rand.nextInt(config.rand).toFloat()
        val y = view.height - config.y
        var x2 = rand.nextInt(config.rand).toFloat()
        val y2 = y - config.animLength * factor + rand.nextInt(config.animLengthRand)
        val value = y2 / config.factor

        x += config.point * (1 + rand.nextInt(2))
        x2 += config.point * (1 + rand.nextInt(2))
        val y3 = y - y2
        return Path().apply {
            moveTo(config.x, y)
            cubicTo(config.x, (y - value), x, y2 + value, x, y2)
            moveTo(x, y2)
            cubicTo(x, (y2 - value), x2, (y3 + factor), x2, y3)
        }
    }

    abstract fun start(child: ImageView, parent: ViewGroup)

    class Config {
        var x: Float = 0f
        var y: Float = 0f
        var rand: Int = 0
        var animLengthRand: Int = 0
        var factor: Int = 0
        var point: Int = 0
        var animLength: Int = 0
        var heartWidth: Int = 0
        var heartHeight: Int = 0
        var animDuration: Int = 0

        companion object {
            fun fromTypeArray(typedArray: TypedArray): Config {
                val config = Config()
                val res = typedArray.resources
                config.x = typedArray.getDimension(R.styleable.HeartLayout_initX, res.getDimensionPixelOffset(R.dimen.heart_anim_init_x).toFloat())
                config.y = typedArray.getDimension(R.styleable.HeartLayout_initY, res.getDimensionPixelOffset(R.dimen.heart_anim_init_y).toFloat())
                config.rand = typedArray.getDimension(R.styleable.HeartLayout_xRand, res.getDimensionPixelOffset(R.dimen.heart_anim_bezier_x_rand).toFloat()).toInt()
                config.animLengthRand = typedArray.getDimension(R.styleable.HeartLayout_animLengthRand, res.getDimensionPixelOffset(R.dimen.heart_anim_length_rand).toFloat()).toInt()
                config.factor = typedArray.getDimension(R.styleable.HeartLayout_bezierFactor, Configuration.HEART_ANIMATION_FACTOR.toFloat()).toInt()
                config.point = typedArray.getDimension(R.styleable.HeartLayout_xPointFactor, res.getDimensionPixelOffset(R.dimen.heart_anim_x_point_factor).toFloat()).toInt()
                config.animLength = typedArray.getDimension(R.styleable.HeartLayout_animLength, res.getDimensionPixelOffset(R.dimen.heart_anim_length).toFloat()).toInt()
                config.heartWidth = typedArray.getDimension(R.styleable.HeartLayout_heart_width, res.getDimensionPixelOffset(R.dimen.heart_size_width).toFloat()).toInt()
                config.heartHeight = typedArray.getDimension(R.styleable.HeartLayout_heart_height, res.getDimensionPixelOffset(R.dimen.heart_size_height).toFloat()).toInt()
                config.animDuration = typedArray.getDimension(R.styleable.HeartLayout_anim_duration, Configuration.HEART_ANIMATION_DURATION.toFloat()).toInt()
                return config
            }
        }
    }
}
