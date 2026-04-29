package com.ins.insdrama

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ins.insdrama.adapter.EpisodeAdapter
import com.ins.insdrama.api.ApiClient
import com.ins.insdrama.databinding.ActivityDetailBinding
import com.ins.insdrama.model.Drama
import com.ins.insdrama.util.DownloadManager
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var episodeAdapter: EpisodeAdapter
    private var drama: Drama? = null

    companion object {
        const val EXTRA_DRAMA = "extra_drama"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drama = intent.getParcelableExtra(EXTRA_DRAMA)
        if (drama == null) {
            Toast.makeText(this, "Data drama tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadDramaData()
    }

    private fun setupViews() {
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Play all button
        binding.playAllButton.setOnClickListener {
            // Start player with first episode
            val playerIntent = android.content.Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.EXTRA_DRAMA, drama)
            startActivity(playerIntent)
        }

        // Episode adapter with download support
        episodeAdapter = EpisodeAdapter(
            dramaId = d.bookId,
            dramaTitle = d.title,
            onPlayClick = { episode ->
                // Start player with this episode
                val playerIntent = android.content.Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(PlayerActivity.EXTRA_DRAMA, d)
                playerIntent.putExtra(PlayerActivity.EXTRA_EPISODE_INDEX, episode.index - 1)
                startActivity(playerIntent)
            },
            onDownloadClick = { episode ->
                // Download or delete episode
                if (DownloadManager.isEpisodeDownloaded(this, d.bookId, episode)) {
                    // Delete download
                    if (DownloadManager.deleteEpisode(this, d.bookId, episode)) {
                        Toast.makeText(this, "Download deleted", Toast.LENGTH_SHORT).show()
                        episodeAdapter.submitList(d.episodes)
                    }
                } else {
                    // Start download
                    DownloadManager.downloadEpisode(this, d.bookId, d.title, episode)
                    Toast.makeText(this, "Download started: Episode ${episode.index}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.episodeRecyclerView.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(this@DetailActivity)
        }
    }

    private fun loadDramaData() {
        val d = drama ?: return
        
        // Load cover image
        Glide.with(this)
            .load(d.coverUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_cover)
            .into(binding.coverImage)

        // Set title
        binding.titleText.text = d.title

        // Set episode count
        binding.episodeCountText.text = "${d.episodes.size} Episode"

        // Set date
        binding.dateText.text = d.createdAt.substring(0, 10)

        // Set genres
        binding.genreChipGroup.removeAllViews()
        d.genres.forEach { genre ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = genre
                isClickable = false
                isCheckable = false
                chipBackgroundColor = getColorStateList(R.color.chip_background)
                textColor = getColorStateList(R.color.chip_text)
            }
            binding.genreChipGroup.addView(chip)
        }

        // Set description
        binding.descriptionText.text = d.description

        // Set episodes
        episodeAdapter.submitList(d.episodes)
    }
}
