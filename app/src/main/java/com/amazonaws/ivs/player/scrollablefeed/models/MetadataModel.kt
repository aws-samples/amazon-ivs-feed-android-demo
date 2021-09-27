package com.amazonaws.ivs.player.scrollablefeed.models

import kotlinx.serialization.Serializable

@Serializable
data class MetadataModel(
    val streamTitle: String,
    val userAvatar: String,
    val userName: String,
    val userColors: UserColorModel
)
