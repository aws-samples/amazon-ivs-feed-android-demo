package com.amazonaws.ivs.player.scrollablefeed.models

import android.net.Uri
import android.view.Surface
import android.view.TextureView
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import timber.log.Timber

@Serializable
data class StreamModel(
    val id: Int,
    val stream: StreamItemModel,
    val metadata: MetadataModel,
    @Transient var player: MediaPlayer? = null,
    @Transient var playerListener: Player.Listener? = null,
    @Transient var playerParams: SizeModel = SizeModel(0, 0),
    @Transient var error: ErrorModel? = null,
    @Transient var currentTime: Long = 0,
    @Transient var state: Player.State = Player.State.IDLE,
    @Transient var isMuted: Boolean = true,
    @Transient var isActive: Boolean = false,
    @Transient var isPaused: Boolean = false,
    @Transient private var isLoaded: Boolean = false
) {
    val isBuffering get() = player?.state == Player.State.BUFFERING
    val isPlaying get() = player?.state == Player.State.PLAYING

    fun reset() {
        if (player == null) return
        Timber.d("Resetting player: $id")
        playerListener?.let { player?.removeListener(it) }
        player?.release()
        playerParams = SizeModel(0, 0)
        error = null
        player = null
        playerListener = null
        currentTime = 0
        state = Player.State.IDLE
        isActive = false
        isLoaded = false
        isPaused = false
    }

    fun start(textureView: TextureView) {
        mute(isMuted)
        player?.setSurface(Surface(textureView.surfaceTexture))
        if (!isLoaded) {
            Timber.d("Starting player: $id")
            player?.load(Uri.parse(stream.playbackUrl))
            isLoaded = true
        }
        if (isActive) {
            play()
        } else {
            pause()
        }
    }

    fun play() {
        if (!isLoaded || isPlaying || isBuffering) return
        Timber.d("Playing player: $id")
        player?.play()
        isPaused = false
    }

    fun pause() {
        if (!isLoaded || isPaused) return
        Timber.d("Pausing player: $id")
        player?.pause()
        isPaused = true
    }

    fun mute(muted: Boolean) {
        isMuted = muted
        if (isActive) {
            player?.isMuted = muted
            if (isMuted != muted) {
                Timber.d("Muting player: $id, $muted")
            }
        }
    }
}
