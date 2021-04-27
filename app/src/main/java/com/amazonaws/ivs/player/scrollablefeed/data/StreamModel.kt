package com.amazonaws.ivs.player.scrollablefeed.data

import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.amazonaws.ivs.player.scrollablefeed.views.heartView.HeartLayout
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StreamModel(
    val id: Int,
    val stream: StreamItemModel,
    val metadata: MetadataModel,
    @Transient
    var surfaceView: SurfaceView? = null,
    @Transient
    var backgroundView: ImageView? = null,
    @Transient
    var overlayView: View? = null,
    @Transient
    var heartView: HeartLayout? = null,
)
