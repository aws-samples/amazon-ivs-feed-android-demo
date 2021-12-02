package com.amazonaws.ivs.player.scrollablefeed.models

data class StreamUIModel(
    val id: Int,
    val stream: StreamItemModel,
    val metadata: MetadataModel,
    val playerParams: SizeModel,
    val error: ErrorModel?,
    val currentTime: Long,
    val isMuted: Boolean,
    val isActive: Boolean,
    val isPaused: Boolean,
    val isPlaying: Boolean,
    val isBuffering: Boolean
)
