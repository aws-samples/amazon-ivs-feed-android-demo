package com.amazonaws.ivs.player.scrollablefeed.data

import android.graphics.Color

data class UserColorModel(val primary: String, val secondary: String) {
    fun primaryColor() = Color.parseColor(primary)
    fun secondaryColor() = Color.parseColor(secondary)
}
