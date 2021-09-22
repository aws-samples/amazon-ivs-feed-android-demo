package com.amazonaws.ivs.player.scrollablefeed.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.core.view.doOnLayout
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.models.SizeModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import kotlin.math.roundToInt

private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e ->
        Timber.d(e, "Coroutine failed ${e.localizedMessage}")
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


fun TextView.firstCharColor(color: Int) {
    val spannable = SpannableStringBuilder(text)
    spannable.setSpan(ForegroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannable
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

fun TextureView.onReady(onReady: (surface: Surface) -> Unit) {
    if (surfaceTexture != null) {
        onReady(Surface(surfaceTexture))
        return
    }
    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            surfaceTextureListener = null
            onReady(Surface(surfaceTexture))
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            /* Ignored */
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            /* Ignored */
        }
    }
}

fun View.scaleToFit(videoSize: SizeModel, parentView: View? = null) {
    if (videoSize.width == 0 || videoSize.height == 0) return
    (parentView ?: parent as View).doOnLayout { useToScale ->
        calculateSurfaceSize(videoSize.width, videoSize.height)
        val size = useToScale.calculateSurfaceSize(videoSize.width, videoSize.height)
        val params = layoutParams
        params.width = size.width
        params.height = size.height
        layoutParams = params
    }
}

private fun View.calculateSurfaceSize(videoWidth: Int, videoHeight: Int): Size {
    val ratio = videoHeight / videoWidth.toFloat()
    val scaledWidth: Int
    val scaledHeight: Int
    if (measuredHeight > measuredWidth * ratio) {
        scaledWidth = measuredWidth
        scaledHeight = (measuredWidth * ratio).roundToInt()
    } else {
        scaledWidth = (measuredHeight / ratio).roundToInt()
        scaledHeight = measuredHeight
    }
    return Size(scaledWidth, scaledHeight)
}
