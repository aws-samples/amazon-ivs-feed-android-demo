package com.amazonaws.ivs.player.scrollablefeed.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration.TAG
import com.amazonaws.ivs.player.scrollablefeed.common.setListener

class MainViewModel(private val context: Application) : ViewModel() {

    var player: MediaPlayer? = null
    private var playerListener: Player.Listener? = null

    val buffering = MutableLiveData<Boolean>()
    val playerParamsChanged = MutableLiveData<Pair<Int, Int>>()
    val errorHappened = MutableLiveData<Pair<String, String>>()
    val isMuted = MutableLiveData<Boolean>()
    val isPlaying = MutableLiveData<Boolean>()
    val isIdle = MutableLiveData<Boolean>()
    val isLandscape = MutableLiveData<Boolean>()

    init {
        initPlayer()
    }

    private fun initPlayer() {
        // Media player initialization
        player = MediaPlayer(context)

        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Log.d(TAG, "Video size changed: $width $height")
                isLandscape.value = width >= height
                playerParamsChanged.value = Pair(width, height)
            },
            onStateChanged = { state ->
                Log.d(TAG, "State changed: $state")
                isIdle.value = false
                when (state) {
                    Player.State.BUFFERING -> {
                        buffering.value = true
                    }
                    else -> {
                        buffering.value = false
                        isPlaying.value = false
                        if (state == Player.State.PLAYING) {
                            isPlaying.value = true
                        }

                        if (state == Player.State.IDLE) {
                            isIdle.value = true
                        }
                    }
                }
            },
            onError = { exception ->
                Log.d(TAG, "Error happened: $exception")
                errorHappened.value = Pair(exception.code.toString(), exception.errorMessage)
            }
        )
    }

    private fun playerLoadStream(uri: Uri) {
        Log.d(TAG, "Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
        player?.play()
    }

    fun playerStart(surface: Surface, link : String) {
        Log.d(TAG, "Starting player")
        mute(true)
        updateSurface(surface)
        playerLoadStream(Uri.parse(link))
        play()
    }

    fun updateSurface(surface: Surface?) {
        Log.d(TAG, "Updating player surface: $surface")
        // Sets the Surface to use for rendering video
        player?.setSurface(surface)
    }

    fun mute(mute: Boolean) {
        isMuted.value = mute
        player?.isMuted = mute
    }

    fun play() {
        Log.d(TAG, "Starting playback")
        // Starts or resumes playback of the stream.
        player?.play()
    }

    fun pause() {
        Log.d(TAG, "Pausing playback")
        // Pauses playback of the stream.
        player?.pause()
    }

    fun release() {
        Log.d(TAG, "Releasing player")
        // Removes a playback state listener
        playerListener?.let { player?.removeListener(it) }
        // Releases the player instance
        player?.release()
        player = null
    }

    fun isPlayerMuted() : Boolean? = player?.isMuted

}
