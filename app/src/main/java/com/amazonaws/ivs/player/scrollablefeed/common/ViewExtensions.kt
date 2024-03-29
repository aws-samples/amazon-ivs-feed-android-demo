package com.amazonaws.ivs.player.scrollablefeed.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Size
import androidx.core.view.doOnLayout
import com.amazonaws.ivs.player.scrollablefeed.models.SizeModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

fun initBlurredBackground(textureView: TextureView, background: ImageView) = launchMain {
    if (background.hasImage()) return@launchMain
    delay(SHOW_BACKGROUND_DELAY)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val surface = Surface(textureView.surfaceTexture!!)
        if (surface.isValid) {
            val surfaceBitmap = Bitmap.createBitmap(textureView.width, textureView.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(surface, surfaceBitmap, {
                if (it == PixelCopy.SUCCESS) {
                    val scaledBitmap = surfaceBitmap.scaleBitmap(background.width, background.height)
                    val blurredBitmap = scaledBitmap.blurBitmap(textureView.context)
                    background.alpha = 0f
                    background.setImageBitmap(blurredBitmap)
                    background.animate().alpha(0.35f).setStartDelay(SHOW_BACKGROUND_DELAY).start()
                }
            }, Handler(Looper.getMainLooper()))
        }
    }
}

fun Bitmap.blurBitmap(applicationContext: Context): Bitmap {
    var renderScript: RenderScript? = null
    try {
        val output = Bitmap.createBitmap(width, height, config)
        renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(renderScript, this)
        val outAlloc = Allocation.createTyped(renderScript, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.apply {
            setRadius(BLUR_RADIUS)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)
        return output
    } finally {
        renderScript?.finish()
    }
}

fun Bitmap.scaleBitmap(maxWidth: Int, maxHeight: Int): Bitmap {
    var width = width
    var height = height
    when {
        width > height -> {
            if (maxWidth > 0) {
                val ratio = width / maxWidth
                width = maxWidth
                if (ratio > 0) height /= ratio
            }
        }
        height > width -> {
            if (maxHeight > 0) {
                val ratio = height / maxHeight
                height = maxHeight
                if (ratio > 0) width /= ratio
            }
        }
        else -> {
            height = maxHeight
            width = maxWidth
        }
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun TextView.firstCharColor(color: Int) {
    val spannable = SpannableStringBuilder(text)
    spannable.setSpan(ForegroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannable
}

fun TextView.setActiveTime(time: String, currentTime: Long) {
    val message = StringBuilder("for")
    val pattern = "yyyy-MM-dd hh:mm:ss"
    val date = time.replace("T", " ").replace("Z", " ")
    val dateFormat = SimpleDateFormat(pattern, Locale.US).parse(date)
    dateFormat?.let {
        val diff = abs(currentTime - dateFormat.time)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff - TimeUnit.DAYS.toMillis(days))
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours))
        if (days > 0) {
            message.append(" ${days}d")
        }
        if (hours > 0) {
            message.append(" ${hours}h")
        }
        message.append(" ${minutes}m")
        text = message
    }
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

private fun ImageView.hasImage(): Boolean {
    var hasImage = drawable != null
    if (hasImage && drawable is BitmapDrawable) {
        hasImage = (drawable as BitmapDrawable).bitmap != null
    }
    return hasImage
}
