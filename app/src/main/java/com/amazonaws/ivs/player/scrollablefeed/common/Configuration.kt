package com.amazonaws.ivs.player.scrollablefeed.common

import com.amazonaws.ivs.player.scrollablefeed.R

object Configuration {
    const val JSON_FILE_NAME = "streams.json"

    const val ANIMATION_DURATION = 200L
    const val LONG_ANIMATION_DURATION = 800L
    const val BLUR_RADIUS = 15F // Background blur (0-25)
    const val BACKGROUND_DELAY = 100L

    const val ACTIVE_TIME_UPDATE_DELAY = 60000L // 1 minute

    //Heart animation
    const val HEART_ANIMATION_FACTOR = 6
    const val HEART_ANIMATION_DURATION = 3000L

    val HEART_COLORS = listOf(
        R.color.yellow_accent_color,
        R.color.primary_red_accent_color,
        R.color.pink_accent_color,
        R.color.violet_accent_color
    )
}
