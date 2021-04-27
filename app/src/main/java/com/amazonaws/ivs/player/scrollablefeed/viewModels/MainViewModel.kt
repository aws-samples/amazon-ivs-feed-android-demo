package com.amazonaws.ivs.player.scrollablefeed.viewModels

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.scrollablefeed.common.ConsumableLiveData
import com.amazonaws.ivs.player.scrollablefeed.common.setListener
import timber.log.Timber

class MainViewModel : ViewModel() {

    var player: MediaPlayer? = null
    private var playerListener: Player.Listener? = null

    val buffering = ConsumableLiveData<Boolean>()
    val playerParamsChanged = ConsumableLiveData<Pair<Int, Int>>()
    val errorHappened = ConsumableLiveData<Pair<String, String>>()
    val isMuted = ConsumableLiveData<Boolean>()
    val isPlaying = ConsumableLiveData<Boolean>()
    val isIdle = ConsumableLiveData<Boolean>()
    val isLandscape = ConsumableLiveData<Boolean>()

    fun initPlayer(context: Context) {
        // Media player initialization
        player = MediaPlayer(context)

        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Timber.d("Video size changed: $width $height")
                isLandscape.postConsumable(width >= height)
                playerParamsChanged.postConsumable(Pair(width, height))
            },
            onStateChanged = { state ->
                Timber.d("State changed: $state")
                isIdle.postConsumable(false)
                when (state) {
                    Player.State.BUFFERING -> {
                        buffering.postConsumable(true)
                    }
                    else -> {
                        buffering.postConsumable(false)
                        isPlaying.postConsumable(false)
                        if (state == Player.State.PLAYING) {
                            isPlaying.postConsumable(true)
                        }

                        if (state == Player.State.IDLE) {
                            isIdle.postConsumable(true)
                        }
                    }
                }
            },
            onError = { exception ->
                Timber.d("Error happened: ${exception.code}, $exception")
                if (exception.code != 0) {
                    errorHappened.postConsumable(Pair(exception.code.toString(), exception.errorMessage))
                }
            }
        )
    }

    private fun playerLoadStream(uri: Uri) {
       Timber.d("Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
        player?.play()
    }

    fun playerStart(surface: Surface, link: String) {
       Timber.d("Starting player")
        mute(true)
        updateSurface(surface)
        playerLoadStream(Uri.parse(link))
        play()
    }

    fun mute(mute: Boolean) {
        isMuted.postConsumable(mute)
        player?.isMuted = mute
    }

    fun play() {
       Timber.d("Starting playback")
        // Starts or resumes playback of the stream.
        player?.play()
    }

    fun pause() {
       Timber.d("Pausing playback")
        // Pauses playback of the stream.
        player?.pause()
    }

    fun release() {
       Timber.d("Releasing player")
        errorHappened.consume()
        playerParamsChanged.consume()
        isIdle.consume()
        isLandscape.consume()
        isMuted.consume()
        isPlaying.consume()
        buffering.consume()
        // Removes a playback state listener
        playerListener?.let { player?.removeListener(it) }
        // Releases the player instance
        player?.release()
        player = null
    }

    fun isPlayerMuted(): Boolean? = player?.isMuted

    private fun updateSurface(surface: Surface) {
        player?.setSurface(surface)
    }
}
