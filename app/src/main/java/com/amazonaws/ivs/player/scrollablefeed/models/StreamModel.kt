package com.amazonaws.ivs.player.scrollablefeed.models

import android.net.Uri
import android.view.Surface
import android.view.TextureView
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.scrollablefeed.common.setListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import timber.log.Timber

@Serializable
data class StreamModel(
    val id: Int,
    val stream: StreamItemModel,
    val metadata: MetadataModel
) {
    @Transient private var playerListener: Player.Listener? = null
    @Transient private var player: MediaPlayer? = null
    @Transient private var isLoaded: Boolean = false

    @Transient var error: ErrorModel? = null
    @Transient var currentTime: Long = 0
    @Transient var playerParams: SizeModel = SizeModel(0, 0)
        private set
    @Transient var isMuted: Boolean = true
        private set
    @Transient var isActive: Boolean = false
        private set
    @Transient var isPaused: Boolean = false
        private set
    @Transient var isPlaying: Boolean = false
        private set
    @Transient var isBuffering: Boolean = true
        private set

    fun reset() {
        if (player == null) return
        Timber.d("Resetting player: ${toString()}")
        playerListener?.let { player?.removeListener(it) }
        player?.release()
        playerParams = SizeModel(0, 0)
        error = null
        player = null
        playerListener = null
        currentTime = 0
        isActive = false
        isLoaded = false
        isPaused = false
        isPlaying = false
        isBuffering = true
    }

    fun initPlayer(textureView: TextureView, onUpdated: () -> Unit) {
        if (player != null) {
            updatePlayer(textureView)
            return
        }
        Timber.d("Initializing player: ${toString()}")
        player = MediaPlayer(textureView.context)
        player?.setListener(
            onVideoSizeChanged = { width, height ->
                val params = SizeModel(width, height)
                if (playerParams != params) {
                    playerParams = params
                    Timber.d("Player params changed: ${toString()}")
                    onUpdated()
                }
            },
            onStateChanged = { state ->
                val playing = state == Player.State.PLAYING
                if (isPlaying != playing) {
                    isPlaying = playing
                    isBuffering = false
                    Timber.d("Player state changed: ${toString()}")
                    onUpdated()
                }
            },
            onError = { exception ->
                if (exception.code != 0) {
                    Timber.d("Error happened: ${exception.code}, $exception for: ${toString()}")
                    error = ErrorModel(exception.code, exception.errorMessage)
                    onUpdated()
                }
            }
        )
        updatePlayer(textureView)
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun pause(paused: Boolean) {
        isPaused = paused
    }

    fun setActive(active: Boolean) {
        if (!active) {
            player?.pause()
            isPaused = false
        }
        isActive = active
    }

    fun mute(muted: Boolean) {
        isMuted = muted
        if (isActive) {
            player?.isMuted = muted
            if (isMuted != muted) {
                Timber.d("Muting player: ${toString()}, $muted")
            }
        }
    }

    private fun updatePlayer(textureView: TextureView) {
        player?.setSurface(Surface(textureView.surfaceTexture))
        mute(isMuted)
        load()
        play()
    }

    private fun load() {
        if (isLoaded) return
        Timber.d("Loading player: ${toString()}")
        player?.load(Uri.parse(stream.playbackUrl))
        player?.pause()
        isLoaded = true
    }

    private fun play() {
        if (isActive && !isPaused) {
            if (isPlaying) return
            isBuffering = true
            isPlaying = false
            player?.play()
            Timber.d("Playing player: ${toString()}")
        } else {
            if (!isPlaying) return
            player?.pause()
            Timber.d("Pausing player: ${toString()}")
        }
    }

    override fun toString(): String {
        return "StreamModel(id=$id, isMuted=$isMuted, isActive=$isActive, isPaused=$isPaused, " +
                "isLoaded=$isLoaded, isBuffering=$isBuffering, isPlaying=$isPlaying)"
    }
}
