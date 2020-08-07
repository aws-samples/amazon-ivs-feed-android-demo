package com.amazonaws.ivs.player.scrollablefeed.views.heartView

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView

/**
 * Heart path animator
 * @param configuration configuration params
 */
class PathAnimator(configuration: Config) : AbstractPathAnimator() {
    val handler: Handler = Handler(Looper.getMainLooper())

    init {
        config = configuration
    }

    override fun start(child: ImageView, parent: ViewGroup) {
        parent.addView(child, ViewGroup.LayoutParams(config.heartWidth, config.heartHeight))
        val animation = FloatAnimation(createPath(parent, 2), randomAnimation(), parent, child).apply {
            duration = config.animDuration.toLong()
            interpolator = LinearInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    /* Ignored */
                }

                override fun onAnimationEnd(animation: Animation?) {
                    handler.post { parent.removeView(child) }
                }

                override fun onAnimationStart(animation: Animation?) {
                    /* Ignored */
                }
            })
            interpolator = LinearInterpolator()
        }
        child.startAnimation(animation)
    }
}
