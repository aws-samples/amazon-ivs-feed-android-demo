package com.amazonaws.ivs.player.scrollablefeed.data

import kotlinx.serialization.Serializable

@Serializable
data class StreamsModel(val streams: List<StreamModel>)
