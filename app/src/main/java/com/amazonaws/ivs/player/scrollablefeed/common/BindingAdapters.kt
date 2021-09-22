package com.amazonaws.ivs.player.scrollablefeed.common

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapters {

    @BindingAdapter("firstCharColor")
    @JvmStatic
    fun setFirstCharColor(view: TextView, color: Int) {
        view.firstCharColor(color)
    }

    @BindingAdapter("loadImage")
    @JvmStatic
    fun setImage(view: ImageView, url: String?) {
        if (url == null) return
        Glide.with(view).load(url).circleCrop().into(view)
    }

    @BindingAdapter(value = ["activeTime", "currentTime"], requireAll = true)
    @JvmStatic
    fun setUpdateUi(view: TextView, time: String?, currentTime: Long) {
        if (time == null) return
        view.setActiveTime(time, currentTime)
    }

    @BindingAdapter("animateVisibility")
    @JvmStatic
    fun animateVisibility(view: View, visibility: Int) {
        val alpha = if (visibility == View.VISIBLE) 1f else 0f
        view.animate().alpha(alpha).start()
    }
}
