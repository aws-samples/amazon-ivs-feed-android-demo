package com.amazonaws.ivs.player.scrollablefeed.common

import com.amazonaws.ivs.player.scrollablefeed.R

const val JSON_FILE_NAME = "streams.json"

const val BLUR_RADIUS = 15F
const val CLICK_THRESHOLD = 100
const val TRANSITION_THRESHOLD = 500
const val ACTIVE_TIME_UPDATE_DELAY = 60 * 1000L
const val HEART_ANIMATION_FACTOR = 6
const val HEART_ANIMATION_DURATION = 3 * 1000L
const val SHOW_PLAYER_DELAY = 200L
const val SHOW_BACKGROUND_DELAY = 300L

val HEART_COLORS = listOf(
    R.color.yellow_accent_color,
    R.color.primary_red_accent_color,
    R.color.pink_accent_color,
    R.color.violet_accent_color
)
