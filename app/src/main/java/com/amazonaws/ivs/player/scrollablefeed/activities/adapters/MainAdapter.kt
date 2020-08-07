package com.amazonaws.ivs.player.scrollablefeed.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.scrollablefeed.common.Configuration
import com.amazonaws.ivs.player.scrollablefeed.data.StreamModel
import com.amazonaws.ivs.player.scrollablefeed.databinding.StreamItemBinding
import com.amazonaws.ivs.player.scrollablefeed.viewModels.MainViewModel
import kotlin.properties.Delegates

class MainAdapter(
    private val onVolumeClicked: () -> Unit,
    private val onShareClicked: (url: String, title: String) -> Unit,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    var items: List<StreamModel> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    val isOverlayHidden = MutableLiveData<Boolean>()
    val isMuted = MutableLiveData<Boolean>()
    val buffering = MutableLiveData<Boolean>()
    val currentTime = MutableLiveData<Long>()

    init {
        isOverlayHidden.value = false
        isMuted.value = true
        currentTime.value = System.currentTimeMillis()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.binding.data = data

        data.apply {
            surfaceView = holder.binding.surfaceView
            backgroundView = holder.binding.backgroundView
            overlayView = holder.binding.overlayView
            heartView = holder.binding.titleView.heartView
        }

        holder.binding.apply {

            volumeRoot.setOnClickListener {
                onVolumeClicked()
            }

            titleView.share.setOnClickListener {
                onShareClicked(data.stream.playbackUrl, data.metadata.streamTitle)
            }

            titleView.favorite.setOnClickListener {
                holder.binding.titleView.heartView.addHeart(Configuration.HEART_COLORS.random())
            }
        }

        isOverlayHidden.observe(lifecycleOwner, Observer { hidden ->
            holder.binding.backgroundView.setImageBitmap(null)
            holder.binding.overlayHidden = hidden
        })

        currentTime.observe(lifecycleOwner, Observer { time ->
            holder.binding.currentTime = time
        })

        isMuted.observe(lifecycleOwner, Observer { muted ->
            holder.binding.isMuted = muted
        })

        buffering.observe(lifecycleOwner, Observer { buffering ->
            holder.binding.pbBuffering.visibility = if (buffering) View.VISIBLE else View.GONE
            holder.binding.pbBufferingSmall.visibility = if (buffering) View.VISIBLE else View.GONE
        })
    }

    inner class ViewHolder(val binding: StreamItemBinding) : RecyclerView.ViewHolder(binding.root)

}
