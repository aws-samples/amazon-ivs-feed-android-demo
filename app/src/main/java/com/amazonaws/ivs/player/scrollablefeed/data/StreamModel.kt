package com.amazonaws.ivs.player.scrollablefeed.data

import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.amazonaws.ivs.player.scrollablefeed.views.heartView.HeartLayout

data class StreamModel(
    val id: Int,
    val stream: StreamItemModel,
    val metadata: MetadataModel,
    @Transient
    var surfaceView: SurfaceView,
    @Transient
    var backgroundView: ImageView,
    @Transient
    var overlayView: View,
    @Transient
    var heartView: HeartLayout
)
