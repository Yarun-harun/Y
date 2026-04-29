package com.ins.insdrama.util

import android.content.Context
import android.content.Intent
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode
import com.ins.insdrama.service.DownloadService
import java.io.File

object DownloadManager {
    
    private const val DOWNLOADS_FOLDER = "downloads"
    private const val PREFS_NAME = "download_prefs"
    private const val KEY_DOWNLOADED = "downloaded_episodes"

    // Get download directory
    fun getDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), DOWNLOADS_FOLDER)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // Check if episode is downloaded
    fun isEpisodeDownloaded(context: Context, dramaId: String, episode: Episode): Boolean {
        val filename = getEpisodeFilename(dramaId, episode)
        val file = File(getDownloadDir(context), filename)
        return file.exists()
    }

    // Get downloaded episodes for a drama
    fun getDownloadedEpisodes(context: Context, dramaId: String, episodes: List<Episode>): List<Episode> {
        return episodes.filter { isEpisodeDownloaded(context, dramaId, it) }
    }

    // Start download
    fun downloadEpisode(context: Context, dramaId: String, dramaTitle: String, episode: Episode) {
        if (isEpisodeDownloaded(context, dramaId, episode)) return

        val filename = getEpisodeFilename(dramaId, episode)
        val intent = Intent(context, DownloadService::class.java).apply {
            putExtra(DownloadService.EXTRA_URL, episode.videoUrl)
            putExtra(DownloadService.EXTRA_FILENAME, filename)
            putExtra(DownloadService.EXTRA_EPISODE, "$dramaTitle - Episode ${episode.index}")
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // Delete downloaded episode
    fun deleteEpisode(context: Context, dramaId: String, episode: Episode): Boolean {
        val filename = getEpisodeFilename(dramaId, episode)
        val file = File(getDownloadDir(context), filename)
        return if (file.exists()) file.delete() else false
    }

    // Get local file path for episode
    fun getLocalFilePath(context: Context, dramaId: String, episode: Episode): String? {
        val filename = getEpisodeFilename(dramaId, episode)
        val file = File(getDownloadDir(context), filename)
        return if (file.exists()) file.absolutePath else null
    }

    // Get download progress (0-100)
    fun getDownloadProgress(context: Context, dramaId: String, episode: Episode): Int {
        val filename = getEpisodeFilename(dramaId, episode)
        val file = File(getDownloadDir(context), filename)
        return if (file.exists()) 100 else 0
    }

    // Get total downloaded size in MB
    fun getTotalDownloadedSize(context: Context): Double {
        val dir = getDownloadDir(context)
        var totalBytes: Long = 0
        
        dir.listFiles()?.forEach { file ->
            totalBytes += file.length()
        }
        
        return totalBytes / (1024.0 * 1024.0) // Convert to MB
    }

    // Clear all downloads
    fun clearAllDownloads(context: Context) {
        val dir = getDownloadDir(context)
        dir.listFiles()?.forEach { file ->
            file.delete()
        }
    }

    // Generate unique filename for episode
    private fun getEpisodeFilename(dramaId: String, episode: Episode): String {
        return "insdrama_${dramaId}_ep${episode.index}.mp4"
    }

    // Get all downloaded dramas
    fun getDownloadedDramas(context: Context, allDramas: List<Drama>): List<Drama> {
        return allDramas.filter { drama ->
            getDownloadedEpisodes(context, drama.bookId, drama.episodes).isNotEmpty()
        }
    }
}
