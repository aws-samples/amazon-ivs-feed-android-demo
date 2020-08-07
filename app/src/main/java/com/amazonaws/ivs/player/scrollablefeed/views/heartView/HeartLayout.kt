package com.amazonaws.ivs.player.scrollablefeed.views.heartView

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import com.amazonaws.ivs.player.scrollablefeed.R

/**
 * Layout that displays floating heart animation
 */
class HeartLayout : RelativeLayout {

    private lateinit var animator: AbstractPathAnimator

    constructor(context: Context?) : super(context!!) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.HeartLayout, defStyleAttr, 0)
        animator = PathAnimator(AbstractPathAnimator.Config.fromTypeArray(a))
        a.recycle()
    }

    fun clearAllViews() {
        if (childCount != 0) {
            for (i in 0 until childCount - 1) {
                getChildAt(i).clearAnimation()
            }
            removeAllViews()
        }
    }

    fun addHeart(color: Int) {
        animator.start(ImageView(context).createHeartView(color), this)
    }

}
