package com.ins.insdrama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ins.insdrama.R
import com.ins.insdrama.model.Episode
import com.ins.insdrama.util.DownloadManager

class EpisodeAdapter(
    private val dramaId: String,
    private val dramaTitle: String,
    private val onPlayClick: (Episode) -> Unit,
    private val onDownloadClick: (Episode) -> Unit
) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view, onPlayClick, onDownloadClick)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position), dramaId, dramaTitle)
    }

    class EpisodeViewHolder(
        private val itemView: View,
        private val onPlayClick: (Episode) -> Unit,
        private val onDownloadClick: (Episode) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val episodeNumberText: TextView = itemView.findViewById(R.id.episodeNumberText)
        private val episodeTitleText: TextView = itemView.findViewById(R.id.episodeTitleText)
        private val episodeStatusText: TextView = itemView.findViewById(R.id.episodeStatusText)
        private val downloadedIndicator: ImageView = itemView.findViewById(R.id.downloadedIndicator)
        private val downloadButton: ImageButton = itemView.findViewById(R.id.downloadButton)
        private val playIcon: ImageView = itemView.findViewById(R.id.playIcon)
        private val downloadProgress: ProgressBar = itemView.findViewById(R.id.downloadProgress)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlayClick(getItem(position))
                }
            }

            downloadButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDownloadClick(getItem(position))
                }
            }
        }

        fun bind(episode: Episode, dramaId: String, dramaTitle: String) {
            episodeNumberText.text = episode.index.toString()
            episodeTitleText.text = "Episode ${episode.index}"

            // Check if downloaded
            val isDownloaded = DownloadManager.isEpisodeDownloaded(
                itemView.context, dramaId, episode
            )

            if (isDownloaded) {
                // Episode downloaded
                downloadedIndicator.visibility = View.VISIBLE
                episodeStatusText.text = "Downloaded - Ready for offline"
                episodeStatusText.setTextColor(itemView.context.getColor(R.color.primary))
                downloadButton.setImageResource(android.R.drawable.ic_menu_delete)
                downloadButton.contentDescription = "Delete download"
                downloadProgress.visibility = View.GONE
            } else {
                // Episode not downloaded
                downloadedIndicator.visibility = View.GONE
                episodeStatusText.text = "Tap to download"
                episodeStatusText.setTextColor(itemView.context.getColor(R.color.text_secondary))
                downloadButton.setImageResource(android.R.drawable.stat_sys_download)
                downloadButton.contentDescription = "Download"
                downloadProgress.visibility = View.GONE
            }

            // Play click
            playIcon.setOnClickListener {
                onPlayClick(episode)
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }
}
