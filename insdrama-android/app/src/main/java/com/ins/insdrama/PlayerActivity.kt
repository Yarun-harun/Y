package com.ins.insdrama

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ins.insdrama.databinding.ActivityPlayerBinding
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private var drama: Drama? = null
    private var currentEpisodeIndex = 0

    companion object {
        const val EXTRA_DRAMA = "extra_drama"
        const val EXTRA_EPISODE_INDEX = "extra_episode_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide system UI
        hideSystemUI()

        drama = intent.getParcelableExtra(EXTRA_DRAMA)
        currentEpisodeIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, 0)

        if (drama == null) {
            Toast.makeText(this, "Data drama tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPlayer()
        setupControls()
        setupGesture()
        loadEpisode(currentEpisodeIndex)
    }

    private fun setupPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer

        // Auto play next episode
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNextEpisode()
                }
            }
        })
    }

    private fun setupControls() {
        // Back button
        binding.backButton.setOnClickListener {
            exoPlayer?.stop()
            finish()
        }

        // Play/Pause button
        binding.playPauseButton.setOnClickListener {
            if (exoPlayer?.isPlaying == true) {
                exoPlayer?.pause()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                exoPlayer?.play()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        // Previous episode
        binding.previousButton.setOnClickListener {
            if (currentEpisodeIndex > 0) {
                currentEpisodeIndex--
                loadEpisode(currentEpisodeIndex)
            }
        }

        // Next episode
        binding.nextButton.setOnClickListener {
            if (currentEpisodeIndex < (drama?.episodes?.size ?: 0) - 1) {
                currentEpisodeIndex++
                loadEpisode(currentEpisodeIndex)
            } else {
                playNextDrama()
            }
        }

        // Detail button
        binding.detailButton.setOnClickListener {
            val detailIntent = android.content.Intent(this, DetailActivity::class.java)
            detailIntent.putExtra(DetailActivity.EXTRA_DRAMA, drama)
            startActivity(detailIntent)
        }

        // Genre button
        binding.genreButton.setOnClickListener {
            toggleGenrePanel()
        }
    }

    private fun setupGesture() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (velocityY > 0) {
                    // Swipe down - next episode/drama
                    binding.nextButton.performClick()
                } else if (velocityY < 0) {
                    // Swipe up - previous episode
                    binding.previousButton.performClick()
                }
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun loadEpisode(index: Int) {
        val d = drama ?: return
        if (index >= d.episodes.size) return

        val episode = d.episodes[index]
        
        // Update UI
        binding.dramaTitleText.text = d.title
        binding.episodeInfoText.text = "Episode ${episode.index} dari ${d.episodes.size}"

        // Check if episode is downloaded (offline mode)
        val localPath = com.ins.insdrama.util.DownloadManager.getLocalFilePath(this, d.bookId, episode)
        
        val mediaItem = if (localPath != null) {
            // Play from local file (offline)
            MediaItem.fromUri(android.net.Uri.fromFile(java.io.File(localPath)))
        } else {
            // Stream from URL (online)
            MediaItem.fromUri(episode.videoUrl)
        }
        
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        // Update play button icon
        binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun playNextEpisode() {
        val d = drama ?: return
        if (currentEpisodeIndex < d.episodes.size - 1) {
            currentEpisodeIndex++
            loadEpisode(currentEpisodeIndex)
        } else {
            playNextDrama()
        }
    }

    private fun playNextDrama() {
        Toast.makeText(this, "Selesai menonton drama ini", Toast.LENGTH_SHORT).show()
        exoPlayer?.stop()
        finish()
    }

    private fun toggleGenrePanel() {
        binding.genrePanel.visibility = if (binding.genrePanel.visibility == android.view.View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
