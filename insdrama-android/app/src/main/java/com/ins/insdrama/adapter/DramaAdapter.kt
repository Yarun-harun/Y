package com.ins.insdrama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ins.insdrama.R
import com.ins.insdrama.model.Drama

class DramaAdapter(
    private val onItemClick: (Drama) -> Unit
) : ListAdapter<Drama, DramaAdapter.DramaViewHolder>(DramaDiffCallback()) {

    private var currentPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DramaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drama, parent, false)
        return DramaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: DramaViewHolder, position: Int) {
        holder.bind(getItem(position), position == currentPosition)
    }

    fun setCurrentPosition(position: Int) {
        currentPosition = position
        notifyItemChanged(currentPosition)
    }

    class DramaViewHolder(
        itemView: View,
        private val onItemClick: (Drama) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val genreChipGroup: ChipGroup = itemView.findViewById(R.id.genreChipGroup)
        private val playButton: ImageButton = itemView.findViewById(R.id.playButton)
        private val detailButton: ImageButton = itemView.findViewById(R.id.detailButton)
        private val episodeCount: TextView = itemView.findViewById(R.id.episodeCount)
        private val overlayGradient: View = itemView.findViewById(R.id.overlayGradient)

        fun bind(drama: Drama, isActive: Boolean) {
            titleText.text = drama.title
            episodeCount.text = "${drama.episodes.size} Episode"

            // Load cover image
            Glide.with(itemView.context)
                .load(drama.coverUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_cover)
                .into(coverImage)

            // Set genres
            genreChipGroup.removeAllViews()
            drama.genres.take(3).forEach { genre ->
                val chip = Chip(itemView.context).apply {
                    text = genre
                    isClickable = false
                    chipBackgroundColor = itemView.context.getColorStateList(R.color.chip_background)
                    textColor = itemView.context.getColorStateList(R.color.chip_text)
                }
                genreChipGroup.addView(chip)
            }

            // Play button click
            playButton.setOnClickListener {
                onItemClick(drama)
            }

            // Detail button click
            detailButton.setOnClickListener {
                onItemClick(drama)
            }

            // Show/hide overlay based on active state
            overlayGradient.visibility = if (isActive) View.VISIBLE else View.GONE
            playButton.visibility = if (isActive) View.VISIBLE else View.GONE
            detailButton.visibility = if (isActive) View.VISIBLE else View.GONE
        }
    }

    class DramaDiffCallback : DiffUtil.ItemCallback<Drama>() {
        override fun areItemsTheSame(oldItem: Drama, newItem: Drama): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        override fun areContentsTheSame(oldItem: Drama, newItem: Drama): Boolean {
            return oldItem == newItem
        }
    }
}
