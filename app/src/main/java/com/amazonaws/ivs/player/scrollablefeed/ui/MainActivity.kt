package com.amazonaws.ivs.player.scrollablefeed.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.doOnLayout
import com.amazonaws.ivs.player.scrollablefeed.App
import com.amazonaws.ivs.player.scrollablefeed.R
import com.amazonaws.ivs.player.scrollablefeed.common.*
import com.amazonaws.ivs.player.scrollablefeed.models.StreamsModel
import com.amazonaws.ivs.player.scrollablefeed.databinding.ActivityMainBinding
import com.amazonaws.ivs.player.scrollablefeed.databinding.StreamItemBinding
import com.amazonaws.ivs.player.scrollablefeed.models.ScrollDirection
import com.amazonaws.ivs.player.scrollablefeed.models.StreamUIModel
import com.amazonaws.ivs.player.scrollablefeed.ui.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazyViewModel(
        { application as App },
        { MainViewModel(readJsonAsset(JSON_FILE_NAME).asObject<StreamsModel>().streams) }
    )
    private var lastTransitionTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.streamCenter.run {
            muteButton.setOnClickListener {
                viewModel.toggleMute()
            }
            titleView.share.setOnClickListener {
                val stream = viewModel.currentSteam
                startShareIntent(title = stream.metadata.streamTitle, url = stream.stream.playbackUrl)
            }
            titleView.favorite.setOnClickListener {
                titleView.heartView.addHeart(HEART_COLORS.random())
            }
        }
        binding.motionLayout.setOnTouchListener { _, event ->
            val duration = event.eventTime - event.downTime
            val isTransitioning = binding.motionLayout.progress > 0 && binding.motionLayout.progress < 1
            if (event.action == MotionEvent.ACTION_UP
                && duration < CLICK_THRESHOLD
                && !isTransitioning
            ) {
                Timber.d("Motion layout clicked")
                viewModel.togglePause()
                true
            } else {
                false
            }
        }

        binding.motionLayout.doOnLayout { root ->
            updateViewHeight(root.height)
        }

        launchUI {
            viewModel.streams.collect { streams ->
                // Lets assume that the ViewModel will always return 3 streams!
                val topStream = streams[0]
                val centerStream = streams[1]
                val bottomStream = streams[2]
                binding.itemTop = topStream
                binding.itemCenter = centerStream
                binding.itemBottom = bottomStream
                initStream(binding.streamTop, topStream)
                initStream(binding.streamBottom, bottomStream)
                initStream(binding.streamCenter, centerStream)
            }
        }
        binding.motionLayout.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                super.onTransitionCompleted(motionLayout, currentId)
                val currentTime = Date().time
                val transitionDelay = currentTime - lastTransitionTime
                lastTransitionTime = currentTime
                // Workaround for a Bug in ConstraintLayout that causes multiple onTransitionCallbacks for no reason
                if (transitionDelay < TRANSITION_THRESHOLD) return
                Timber.d("Transition completed: $currentId, ${R.id.state_top}, ${R.id.state_center}, ${R.id.state_bottom}")
                when (currentId) {
                    R.id.state_top -> {
                        viewModel.scrollStreams(ScrollDirection.UP)
                        resetCenterStream()
                        motionLayout?.jumpToState(R.id.state_center)
                    }
                    R.id.state_bottom -> {
                        viewModel.scrollStreams(ScrollDirection.DOWN)
                        resetCenterStream()
                        motionLayout?.jumpToState(R.id.state_center)
                    }
                }
            }
        })
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
        viewModel.releaseAll()
    }

    private fun initStream(binding: StreamItemBinding, stream: StreamUIModel) {
        binding.textureView.onReady {
            if (stream.isActive && stream.isPlaying && binding.textureView.alpha != 1f) {
                Timber.d("Showing player: $stream")
                binding.textureView.animate().alpha(1f).setStartDelay(SHOW_PLAYER_DELAY).start()
                initBlurredBackground(binding.textureView, binding.backgroundView)
            }
            binding.textureView.scaleToFit(stream.playerParams)
            viewModel.initPlayer(stream.id, binding.textureView)
        }
        if (stream.isActive && stream.error != null) {
            showErrorDialog()
            viewModel.consumeError()
        }
    }

    private fun resetCenterStream() {
        if (binding.streamCenter.textureView.alpha == 0f) return
        Timber.d("Hiding player")
        binding.streamCenter.textureView.alpha = 0f
        binding.streamCenter.titleView.heartView.clearAllViews()
        binding.streamCenter.backgroundView.setImageDrawable(null)
        binding.streamTop.backgroundView.setImageDrawable(null)
        binding.streamBottom.backgroundView.setImageDrawable(null)
        when (viewModel.scrollDirection) {
            ScrollDirection.UP -> binding.streamCenter.stream = binding.streamTop.stream
            ScrollDirection.DOWN -> binding.streamCenter.stream = binding.streamBottom.stream
            ScrollDirection.NONE -> { /* Ignored */ }
        }
        viewModel.scrollStreams(ScrollDirection.NONE)
    }

    private fun updateViewHeight(height: Int) {
        binding.motionLayout.getConstraintSet(R.id.state_top).constrainHeight(R.id.stream_top, height)
        binding.motionLayout.getConstraintSet(R.id.state_top).constrainHeight(R.id.stream_center, height)
        binding.motionLayout.getConstraintSet(R.id.state_top).constrainHeight(R.id.stream_bottom, height)
        binding.motionLayout.getConstraintSet(R.id.state_center).constrainHeight(R.id.stream_top, height)
        binding.motionLayout.getConstraintSet(R.id.state_center).constrainHeight(R.id.stream_center, height)
        binding.motionLayout.getConstraintSet(R.id.state_center).constrainHeight(R.id.stream_bottom, height)
        binding.motionLayout.getConstraintSet(R.id.state_bottom).constrainHeight(R.id.stream_top, height)
        binding.motionLayout.getConstraintSet(R.id.state_bottom).constrainHeight(R.id.stream_center, height)
        binding.motionLayout.getConstraintSet(R.id.state_bottom).constrainHeight(R.id.stream_bottom, height)
    }
}
