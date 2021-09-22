package com.amazonaws.ivs.player.scrollablefeed.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

inline fun <reified T> String.asObject(): T = json.decodeFromString(this)
