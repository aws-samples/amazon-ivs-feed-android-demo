package com.amazonaws.ivs.player.scrollablefeed.models

import kotlinx.serialization.Serializable

@Serializable
data class StreamsModel(val streams: List<StreamModel>)
