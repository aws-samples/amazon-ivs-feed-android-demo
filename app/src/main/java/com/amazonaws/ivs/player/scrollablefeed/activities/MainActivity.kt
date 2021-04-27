package com.amazonaws.ivs.player.scrollablefeed.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ViewUtil
import com.amazonaws.ivs.player.scrollablefeed.App
import com.amazonaws.ivs.player.scrollablefeed.activities.adapters.MainAdapter
import com.amazonaws.ivs.player.scrollablefeed.common.*
import com.amazonaws.ivs.player.scrollablefeed.data.StreamsModel
import com.amazonaws.ivs.player.scrollablefeed.databinding.ActivityMainBinding
import com.amazonaws.ivs.player.scrollablefeed.viewModels.MainViewModel
import com.amazonaws.ivs.player.scrollablefeed.views.heartView.HeartLayout
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazyViewModel(
        { application as App },
        { MainViewModel() }
    )

    private val mainAdapter by lazy {
        MainAdapter(
            { viewModel.mute(viewModel.isPlayerMuted() != true) },
            { url, title -> startShareIntent(title, url) },
            this
        )
    }

    private var currentPosition: Int = 0
    private var surfaceView: SurfaceView? = null
    private var backgroundImageView: ImageView? = null
    private var overlayView: View? = null
    private var heartView: HeartLayout? = null
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = Runnable {
        launchMain {
            setCurrentTime()
        }
        startDelay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.initPlayer(this)
        mainAdapter.items = readJsonAsset(Configuration.JSON_FILE_NAME).asObject<StreamsModel>().streams
        binding.scrollableFeedView.adapter = mainAdapter

        initScroll()
        PagerSnapHelper().attachToRecyclerView(binding.scrollableFeedView)
        launchMain {
            delay(200)
            initCurrentViews(0)
            playStream()
        }
        startDelay()

        viewModel.playerParamsChanged.observeConsumable(this) {
            surfaceView?.run {
                if (viewModel.isLandscape.consumedValue == true) {
                    ViewUtil.setLayoutParams(this, it.first, it.second)
                } else {
                    setPortraitDimens(windowManager, it.first, it.second)
                }
            }
        }

        viewModel.isPlaying.observeConsumable(this) { isPlaying ->
            if (isPlaying) {
                launchMain {
                    initBlurredBackground(surfaceView as SurfaceView, backgroundImageView as ImageView)
                }
                overlayView?.fadeOutLong()
            }
        }

        viewModel.buffering.observeConsumable(this) { buffering ->
            mainAdapter.buffering.value = buffering
        }

        viewModel.isMuted.observeConsumable(this) { muted ->
            mainAdapter.isMuted.value = muted
        }

        viewModel.isIdle.observeConsumable(this) { idle ->
            if (idle) {
                mainAdapter.isOverlayHidden.value = false
            }
        }

        viewModel.errorHappened.observeConsumable(this) { error ->
            showErrorDialog()
        }
    }

    private fun initScroll() {
        val manager = binding.scrollableFeedView.layoutManager as LinearLayoutManager
        manager.findFirstCompletelyVisibleItemPosition()

        binding.scrollableFeedView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position = manager.findFirstCompletelyVisibleItemPosition()
                if (position >= 0) {
                    heartView?.clearAllViews()
                    if (position != currentPosition) {
                        clearViews()
                        initCurrentViews(position)
                    }
                    playStream()
                }
            }
        })
    }

    private fun initCurrentViews(position: Int) {
        currentPosition = position

        surfaceView = mainAdapter.items.getOrNull(position)?.surfaceView
        backgroundImageView = mainAdapter.items.getOrNull(position)?.backgroundView
        overlayView = mainAdapter.items.getOrNull(position)?.overlayView
        heartView = mainAdapter.items.getOrNull(position)?.heartView
    }

    private fun clearViews() {
        surfaceView = null
        backgroundImageView = null
        overlayView = null
        heartView = null
    }

    private fun playStream() {
        surfaceView?.holder?.surface?.let {
            val url = mainAdapter.items.getOrNull(currentPosition)?.stream?.playbackUrl ?: ""
            viewModel.playerStart(it, url)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun startDelay() {
        timerHandler.postDelayed(timerRunnable, Configuration.ACTIVE_TIME_UPDATE_DELAY)
    }

    private fun setCurrentTime() {
        mainAdapter.currentTime.value = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        viewModel.play()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
        mainAdapter.items = listOf()
    }
}
