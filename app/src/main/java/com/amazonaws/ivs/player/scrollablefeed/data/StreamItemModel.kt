package com.amazonaws.ivs.player.scrollablefeed.data

data class StreamItemModel(
    val channelArn: String,
    val health: String,
    val playbackUrl: String,
    val startTime: String,
    val state: String,
    val viewerCount: Int
)
