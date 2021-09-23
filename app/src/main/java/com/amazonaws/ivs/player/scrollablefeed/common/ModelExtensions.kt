package com.amazonaws.ivs.player.scrollablefeed.common

import com.amazonaws.ivs.player.scrollablefeed.models.StreamModel
import com.amazonaws.ivs.player.scrollablefeed.models.StreamViewModel

fun StreamModel.asStreamViewModel() = StreamViewModel(
    id = id,
    stream = stream,
    metadata = metadata,
    playerParams = playerParams,
    error = error,
    currentTime = currentTime,
    isMuted = isMuted,
    isActive = isActive,
    isPaused = isPaused,
    isPlaying = isPlaying,
    isBuffering = isBuffering
)
