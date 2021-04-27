package com.amazonaws.ivs.player.scrollablefeed.common

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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
    fun setImage(view: ImageView, url: String) {
        Glide.with(view).load(url).into(view)
    }

    @BindingAdapter("setCustomDrawable")
    @JvmStatic
    fun setCustomDrawable(view: ProgressBar, color: Int) {
        view.setIndeterminateDrawable(color)
    }

    @BindingAdapter(value = ["activeTime", "currentTime"], requireAll = true)
    @JvmStatic
    fun setUpdateUi(view: TextView, time: String, currentTime: Long) {
        view.setActiveTime(time, currentTime)
    }

    @BindingAdapter("changeVolumeBackground")
    @JvmStatic
    fun setVolumeBackground(view: View, muted: Boolean) {
        view.changeVolumeBackground(muted)
    }

}
