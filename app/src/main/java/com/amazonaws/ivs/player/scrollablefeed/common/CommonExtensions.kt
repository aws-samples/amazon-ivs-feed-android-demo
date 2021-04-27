package com.amazonaws.ivs.player.scrollablefeed.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.amazonaws.ivs.player.scrollablefeed.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException

private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e ->
        Timber.d("Coroutine failed ${e.localizedMessage}")
    },
    block = block
)

fun Activity.showErrorDialog() {
    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
        .setTitle(resources.getString(R.string.error))
        .setMessage(resources.getString(R.string.error_dialog_message))
        .setPositiveButton(resources.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

/**
 * Fade in view
 */
fun View.fadeIn() {
    if (this.visibility == View.INVISIBLE) {
        startAnimation(getFadeInAnimation())
        this.visibility = View.VISIBLE
    }
}

/**
 * Fade out view
 */
fun View.fadeOutLong() {
    if (this.visibility == View.VISIBLE) {
        startAnimation(getFadeOutAnimation(Configuration.LONG_ANIMATION_DURATION))
        this.visibility = View.INVISIBLE
    }
}

/**
 * TextView first char color
 */
fun TextView.firstCharColor(color: Int) {
    val spannable = SpannableStringBuilder(text)
    spannable.setSpan(ForegroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannable
}

/**
 * ProgressBar custom indeterminateDrawable
 */
fun ProgressBar.setIndeterminateDrawable(color: Int) {
    val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.spinner_background)
    if (unwrappedDrawable != null) {
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        indeterminateDrawable = wrappedDrawable
    }
}

/**
 * Fade in animation
 */
fun getFadeInAnimation(): Animation {
    return AlphaAnimation(0f, 1f).apply {
        interpolator = DecelerateInterpolator()
        duration = Configuration.ANIMATION_DURATION
    }
}

/**
 * Fade out animation
 * @param animDuration animation duration
 */
fun getFadeOutAnimation(animDuration: Long): Animation {
    return AlphaAnimation(1f, 0f).apply {
        interpolator = DecelerateInterpolator()
        startOffset = animDuration
        duration = animDuration
    }
}

/**
 * Read json from asset file
 * @param fileName asset file name
 */
@Throws(IOException::class)
fun Context.readJsonAsset(fileName: String): String {
    val inputStream = assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return String(buffer, Charsets.UTF_8)
}

/**
 * Send intent
 * @param title title message
 * @param url stream url
 */
fun Activity.startShareIntent(title: String, url: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        putExtra(Intent.EXTRA_TITLE, title)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}
