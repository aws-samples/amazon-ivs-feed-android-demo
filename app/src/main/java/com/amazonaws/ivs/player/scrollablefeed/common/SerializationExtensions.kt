package com.amazonaws.ivs.player.scrollablefeed.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

inline fun <reified T> String.asObject(): T = Json { ignoreUnknownKeys = true }.decodeFromString(this)
