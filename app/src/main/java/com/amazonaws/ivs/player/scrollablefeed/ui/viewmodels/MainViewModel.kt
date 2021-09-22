package com.amazonaws.ivs.player.scrollablefeed.ui.viewmodels

import android.os.Handler
import android.os.Looper
import android.view.TextureView
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.scrollablefeed.common.ACTIVE_TIME_UPDATE_DELAY
import com.amazonaws.ivs.player.scrollablefeed.common.setListener
import com.amazonaws.ivs.player.scrollablefeed.models.ErrorModel
import com.amazonaws.ivs.player.scrollablefeed.models.ScrollDirection
import com.amazonaws.ivs.player.scrollablefeed.models.SizeModel
import com.amazonaws.ivs.player.scrollablefeed.models.StreamModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
    private val _streams = MutableSharedFlow<List<StreamModel>>(replay = 1)
    val streams: SharedFlow<List<StreamModel>> = _streams
    val currentSteam get() = rawStreams[currentPosition]
    var scrollDirection = ScrollDirection.NONE
        private set

    init {
        startTimer()
        updateStreams()
    }

    fun playerStart(streamId: Int, textureView: TextureView) {
        rawStreams.firstOrNull { it.id == streamId }?.let { stream ->
            if (stream.player != null) {
                stream.start(textureView)
                return@let
            }
            Timber.d("Initializing player: $streamId")
            stream.player = MediaPlayer(textureView.context)
            stream.player?.setListener(
                onVideoSizeChanged = { width, height ->
                    val params = SizeModel(width, height)
                    if (stream.playerParams != params) {
                        Timber.d("Video size changed: $width $height")
                        stream.playerParams = params
                        updateStreams()
                    }
                },
                onStateChanged = { state ->
                    if (stream.state != state) {
                        Timber.d("State changed: $state")
                        stream.state = state
                        if (state == Player.State.PLAYING) {
                            stream.isPaused = false
                        }
                        updateStreams()
                    }
                },
                onError = { exception ->
                    if (exception.code != 0) {
                        Timber.d("Error happened: ${exception.code}, $exception")
                        stream.error = ErrorModel(exception.code, exception.errorMessage)
                        updateStreams()
                    }
                }
            )
            stream.start(textureView)
        }
    }

    private fun mute(isMuted: Boolean) {
        rawStreams.forEach { it.mute(isMuted) }
        updateStreams()
    }

    fun toggleMute() {
        val isMuted = rawStreams.firstOrNull { it.isActive }?.isMuted ?: false
        mute(!isMuted)
    }

    fun togglePlayback() {
        rawStreams.firstOrNull { it.isActive }?.let { stream ->
            if (stream.isPlaying) {
                stream.pause()
            } else {
                stream.play()
            }
        }
    }

    fun play() {
        rawStreams.forEach { it.play() }
    }

    fun pause() {
        rawStreams.forEach { it.pause() }
    }

    fun release() {
        rawStreams.forEach { it.reset() }
    }

    fun consumeError(streamId: Int) {
        rawStreams.firstOrNull { it.id == streamId }?.error = null
        updateStreams()
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
        rawStreams.forEach { stream ->
            stream.pause()
            stream.isActive = false
        }
        rawStreams[currentPosition].isActive = true
        updateStreams()
    }

    private fun startTimer() {
        timerHandler.postDelayed(timerRunnable, ACTIVE_TIME_UPDATE_DELAY)
    }

    private fun updateStreams() {
        if (rawStreams.isEmpty()) return
        rawStreams.forEach { it.isActive = false }
        rawStreams[currentPosition].isActive = true
        val streams = rawStreams.map { it.copy() }
        val streamCenter = streams[currentPosition]
        val streamTop = if (currentPosition - 1 >= 0) streams[currentPosition - 1] else streams.last()
        val streamBottom = if (currentPosition + 1 < streams.size) streams[currentPosition + 1] else streams.first()
        rawStreams.filter { it.id != streamTop.id && it.id != streamCenter.id && it.id != streamBottom.id }.forEach { stream ->
            stream.reset()
        }
        _streams.tryEmit(listOf(streamTop, streamCenter, streamBottom))
    }
}
