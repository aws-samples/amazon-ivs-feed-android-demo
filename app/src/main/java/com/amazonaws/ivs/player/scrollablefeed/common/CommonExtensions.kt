package com.amazonaws.ivs.player.scrollablefeed.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.amazonaws.ivs.player.scrollablefeed.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException

fun Activity.showErrorDialog() {
    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
        .setTitle(resources.getString(R.string.error))
        .setMessage(resources.getString(R.string.error_dialog_message))
        .setPositiveButton(resources.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Activity.startShareIntent(title: String, url: String) {
    val formattedString = getString(R.string.formatted_share_url, url)
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, formattedString)
        putExtra(Intent.EXTRA_TITLE, title)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

@Throws(IOException::class)
fun Context.readJsonAsset(fileName: String): String {
    val inputStream = assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return String(buffer, Charsets.UTF_8)
}
