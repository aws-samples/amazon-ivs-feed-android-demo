package com.amazonaws.ivs.player.scrollablefeed.common.heartview

import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.amazonaws.ivs.player.scrollablefeed.common.launchMain
import java.util.*

class PathAnimator(private val configuration: HeartConfig) {

    fun start(child: ImageView, parent: ViewGroup) {
        parent.addView(child, ViewGroup.LayoutParams(configuration.heartWidth, configuration.heartHeight))
        val animation = FloatAnimation(createPath(parent), randomAnimation(), parent, child).apply {
            duration = configuration.animDuration.toLong()
            interpolator = LinearInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    /* Ignored */
                }
                override fun onAnimationEnd(animation: Animation?) {
                    launchMain {
                        parent.removeView(child)
                    }
                }
                override fun onAnimationStart(animation: Animation?) {
                    /* Ignored */
                }
            })
            interpolator = LinearInterpolator()
        }
        child.startAnimation(animation)
    }

    private fun randomAnimation() = Random().nextFloat()

    private fun createPath(view: View): Path {
        val rand = Random()
        val factor = 2
        var x = rand.nextInt(configuration.rand).toFloat()
        val y = view.height - configuration.y
        var x2 = rand.nextInt(configuration.rand).toFloat()
        val y2 = y - configuration.animLength * factor + rand.nextInt(configuration.animLengthRand)
        val value = y2 / configuration.factor

        x += configuration.point * (1 + rand.nextInt(2))
        x2 += configuration.point * (1 + rand.nextInt(2))
        val y3 = y - y2
        return Path().apply {
            moveTo(configuration.x, y)
            cubicTo(configuration.x, (y - value), x, y2 + value, x, y2)
            moveTo(x, y2)
            cubicTo(x, (y2 - value), x2, (y3 + factor), x2, y3)
        }
    }
}
