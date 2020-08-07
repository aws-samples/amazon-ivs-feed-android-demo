package com.amazonaws.ivs.player.scrollablefeed.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.core.content.res.ResourcesCompat
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.activities.adapters.MainAdapter
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration.JSON_FILE_NAME
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration.TAG
import com.amazonaws.ivs.player.scrollablefeed.data.StreamsModel
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Blurred background
 * implemented using PixelCopy (API > 24)
 */
suspend fun Activity.initBlurredBackground(surface: SurfaceView, background: ImageView) {
    delay(Configuration.BACKGROUND_DELAY)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        runOnUiThread {
            if (surface.holder.surface.isValid) {
                val surfaceBitmap =
                    Bitmap.createBitmap(surface.width, surface.height, Bitmap.Config.ARGB_8888)

                // PixelCopy provides a mechanisms to issue pixel copy requests to allow for copy operations from Surface to Bitmap
                PixelCopy.request(
                    surface,
                    surfaceBitmap,
                    {
                        if (it == PixelCopy.SUCCESS) {
                            val scaledBitmap =
                                surfaceBitmap.scaleBitmap(background.width, background.height)
                            val blurredBitmap = scaledBitmap.blurBitmap(applicationContext)
                            background.setImageBitmap(blurredBitmap)
                            background.fadeIn()
                        }
                    },
                    Handler()
                )
            }
        }
    }
}

/**
 * Background Bitmap blur
 */
@WorkerThread
fun Bitmap.blurBitmap(applicationContext: Context): Bitmap {
    lateinit var renderScript: RenderScript
    try {
        // Create the output bitmap
        val output = Bitmap.createBitmap(
            width, height, config
        )
        // Blur the image
        renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(renderScript, this)
        val outAlloc = Allocation.createTyped(renderScript, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.apply {
            setRadius(Configuration.BLUR_RADIUS)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        renderScript.finish()
    }
}

/**
 * Background Bitmap scaling
 */
@WorkerThread
fun Bitmap.scaleBitmap(maxWidth: Int, maxHeight: Int): Bitmap {
    var width = width
    var height = height

    when {
        width > height -> {
            // Landscape
            if (maxWidth > 0) {
                val ratio = width / maxWidth
                width = maxWidth
                if (ratio > 0) height /= ratio
            }
        }
        height > width -> {
            // Portrait
            if (maxHeight > 0) {
                val ratio = height / maxHeight
                height = maxHeight
                if (ratio > 0) width /= ratio
            }
        }
        else -> {
            // Square
            height = maxHeight
            width = maxWidth
        }
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}

/**
 * Surface view width/height scaling
 * @param windowManager window manager
 * @param width video width
 * @param height video height
 */
fun SurfaceView.setPortraitDimens(windowManager: WindowManager, width: Int, height: Int) {
    val point = Point()
    windowManager.defaultDisplay.getSize(point)
    val aspectRatio = width.toFloat() / height.toFloat()
    val portraitWidth = point.y * aspectRatio
    if (portraitWidth >= point.x) {
        layoutParams.width = portraitWidth.toInt()
        layoutParams.height = point.y
    } else {
        val portraitHeight = point.x / aspectRatio
        layoutParams.width = point.x
        layoutParams.height = portraitHeight.toInt()
    }
}

/**
 * Adapter items from JSON asset file
 * @param context Context
 */
fun MainAdapter.setItems(context: Context) {
    try {
        val data = Gson().fromJson(context.readJsonAsset(JSON_FILE_NAME), StreamsModel::class.java)
        items = data.streams
        Log.d(TAG, "Stream data: $data")
    } catch (exception: Exception) {
        Log.d(TAG, "Error happened: $exception")
    }
}

/**
 * Set active time
 * @param time time string in a format 'yyyy-MM-ddThh:mm:ssZ'
 */
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

/**
 * Change volume view background
 * @param muted volume param
 */
fun View.changeVolumeBackground(muted: Boolean) {
    val onDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_volume_on, null)
    val offDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_volume_off, null)
    background = if (muted) offDrawable else onDrawable
}
