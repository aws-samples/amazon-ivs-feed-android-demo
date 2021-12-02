package com.amazonaws.ivs.player.scrollablefeed.ui.viewmodels

import android.os.Handler
import android.os.Looper
import android.view.TextureView
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.scrollablefeed.common.ACTIVE_TIME_UPDATE_DELAY
import com.amazonaws.ivs.player.scrollablefeed.common.ConsumableSharedFlow
import com.amazonaws.ivs.player.scrollablefeed.common.asStreamViewModel
import com.amazonaws.ivs.player.scrollablefeed.models.ScrollDirection
import com.amazonaws.ivs.player.scrollablefeed.models.StreamModel
import com.amazonaws.ivs.player.scrollablefeed.models.StreamUIModel
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.util.*

class MainViewModel(private val rawStreams: List<StreamModel>) : ViewModel() {

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = Runnable {
        rawStreams.forEach { it.currentTime = Date().time }
        updateStreams()
        startTimer()
    }

    private var currentPosition = 0
    private val _streams = ConsumableSharedFlow<List<StreamUIModel>>(canReplay = true)

    val streams = _streams.asSharedFlow()
    val currentSteam get() = rawStreams[currentPosition]
    var scrollDirection = ScrollDirection.NONE
        private set

    init {
        startTimer()
        updateStreams()
    }

    fun initPlayer(streamId: Int, textureView: TextureView) {
        rawStreams.firstOrNull { it.id == streamId }?.let { stream ->
            stream.initPlayer(textureView) {
                updateStreams()
            }
        }
    }

    fun togglePause() {
        currentSteam.togglePause()
        updateStreams()
    }

    fun toggleMute() = mute(!currentSteam.isMuted)

    fun play() {
        currentSteam.pause(false)
        updateStreams()
    }

    fun pause() {
        currentSteam.pause(true)
        updateStreams()
    }

    fun releaseAll() = rawStreams.forEach { it.reset() }

    fun consumeError() {
        currentSteam.error = null
    }

    fun scrollStreams(direction: ScrollDirection) {
        scrollDirection = direction
        currentPosition = when (direction) {
            ScrollDirection.UP -> if (currentPosition - 1 >= 0) currentPosition - 1 else rawStreams.size - 1
            ScrollDirection.DOWN -> if (currentPosition + 1 < rawStreams.size) currentPosition + 1 else 0
            ScrollDirection.NONE -> {
                currentPosition
                return
            }
        }
        Timber.d("Feed scrolled: $direction to index: $currentPosition")
        updateActiveStream()
        updateStreams()
    }

    private fun mute(isMuted: Boolean) {
        rawStreams.forEach { it.mute(isMuted) }
        updateStreams()
    }

    private fun startTimer() {
        timerHandler.postDelayed(timerRunnable, ACTIVE_TIME_UPDATE_DELAY)
    }

    private fun updateActiveStream() {
        rawStreams.forEachIndexed { index, stream ->
            stream.setActive(index == currentPosition)
        }
    }

    private fun updateStreams() {
        if (rawStreams.isEmpty()) return
        updateActiveStream()
        val streamTop = if (currentPosition - 1 >= 0) rawStreams[currentPosition - 1] else rawStreams.last()
        val streamBottom = if (currentPosition + 1 < rawStreams.size) rawStreams[currentPosition + 1] else rawStreams.first()
        rawStreams.filter { it.id != streamTop.id && it.id != currentSteam.id && it.id != streamBottom.id }.forEach { stream ->
            stream.reset()
        }
        _streams.tryEmit(listOf(
            streamTop.asStreamViewModel(),
            currentSteam.asStreamViewModel(),
            streamBottom.asStreamViewModel()
        ))
    }
}
