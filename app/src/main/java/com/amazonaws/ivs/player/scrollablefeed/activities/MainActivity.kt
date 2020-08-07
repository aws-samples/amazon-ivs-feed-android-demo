package com.amazonaws.ivs.player.scrollablefeed.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ViewUtil
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.activities.adapters.MainAdapter
import com.amazonaws.ivs.player.scrollablefeed.common.*
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration.TAG
import com.amazonaws.ivs.player.scrollablefeed.viewModels.MainViewModel
import com.amazonaws.ivs.player.scrollablefeed.views.heartView.HeartLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazyViewModel {
        MainViewModel(application)
    }

    private val mainAdapter by lazy {
        MainAdapter(
            { viewModel.mute(viewModel.isPlayerMuted() != true) },
            { url, title -> sendChooserIntent(title, url)},
            this
        )
    }

    private var currentPosition: Int = 0

    private var surfaceView: SurfaceView? = null
    private var backgroundImageView: ImageView? = null
    private var overlayView: View? = null
    private var heartView: HeartLayout? = null

    private val timerHandler = Handler()
    private val timerRunnable = Runnable {
        launchMain {
            setCurrentTime()
        }
        startDelay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()

        viewModel.playerParamsChanged.observe(this, Observer {
            Log.d(TAG, "Player layout params changed ${it.first} ${it.second}")
            if (viewModel.isLandscape.value == true) {
                ViewUtil.setLayoutParams(surfaceView, it.first, it.second)
            } else {
                surfaceView?.setPortraitDimens(windowManager, it.first, it.second)
            }
        })

        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            if (isPlaying) {
                launchMain {
                    initBlurredBackground(surfaceView as SurfaceView, backgroundImageView as ImageView)
                }
                overlayView?.fadeOutLong()
            }
        })

        viewModel.buffering.observe(this, Observer { buffering ->
            mainAdapter.buffering.value = buffering
        })

        viewModel.isMuted.observe(this, Observer { muted ->
            mainAdapter.isMuted.value = muted
        })

        viewModel.isIdle.observe(this, Observer { idle ->
            if (idle) {
                mainAdapter.isOverlayHidden.value = false
            }
        })

        viewModel.errorHappened.observe(this, Observer { error ->
            showDialog(error.first, error.second)
        })
    }

    private fun initUi() {
        initAdapter()
        initScroll()
        PagerSnapHelper().attachToRecyclerView(scrollable_feed_view)
        launchMain {
            delay(200)
            initCurrentViews(0)
            playStream()
        }
        startDelay()
    }

    private fun initAdapter() {
        mainAdapter.setItems(this)
        scrollable_feed_view.apply {
            adapter = mainAdapter
        }
    }

    private fun initScroll() {
        val manager = scrollable_feed_view.layoutManager as LinearLayoutManager
        manager.findFirstCompletelyVisibleItemPosition()

        scrollable_feed_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        surfaceView = mainAdapter.items[position].surfaceView
        backgroundImageView = mainAdapter.items[position].backgroundView
        overlayView = mainAdapter.items[position].overlayView
        heartView = mainAdapter.items[position].heartView
    }

    private fun clearViews() {
        surfaceView = null
        backgroundImageView = null
        overlayView = null
        heartView = null
    }

    private fun playStream() {
        surfaceView?.holder?.surface?.let {
            val url = mainAdapter.items[currentPosition].stream.playbackUrl
            viewModel.playerStart(it, url)
        }
    }

    private fun startDelay() {
        timerHandler.postDelayed(timerRunnable, Configuration.ACTIVE_TIME_UPDATE_DELAY)
    }

    private fun setCurrentTime() {
        mainAdapter.currentTime.value = System.currentTimeMillis()
    }

    override fun onBackPressed() {
        finish()
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
    }

}
