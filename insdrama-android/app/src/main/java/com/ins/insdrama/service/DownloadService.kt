package com.ins.insdrama.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ins.insdrama.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "insdrama_download_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FILENAME = "extra_filename"
        const val EXTRA_EPISODE = "extra_episode"
    }

    private var downloadThread: Thread? = null
    private var isDownloading = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val filename = intent?.getStringExtra(EXTRA_FILENAME)
        val episodeInfo = intent?.getStringExtra(EXTRA_EPISODE)

        if (url != null && filename != null) {
            startDownload(url, filename, episodeInfo ?: "")
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startDownload(url: String, filename: String, episodeInfo: String) {
        if (isDownloading) return
        isDownloading = true

        val notification = createNotification(0, episodeInfo)
        startForeground(NOTIFICATION_ID, notification)

        downloadThread = Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()

                val fileLength = connection.contentLength
                val downloadDir = File(getExternalFilesDir(null), "downloads")
                if (!downloadDir.exists()) downloadDir.mkdirs()

                val file = File(downloadDir, filename)
                val input = connection.inputStream
                val output = FileOutputStream(file)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    val progress = if (fileLength > 0) {
                        ((total * 100) / fileLength).toInt()
                    } else 0

                    updateNotification(progress, episodeInfo)
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                // Download complete
                stopForeground(STOP_FOREGROUND_REMOVE)
                showCompleteNotification(episodeInfo)

            } catch (e: Exception) {
                e.printStackTrace()
                stopForeground(STOP_FOREGROUND_REMOVE)
                showErrorNotification("Download failed: ${e.message}")
            } finally {
                isDownloading = false
                stopSelf()
            }
        }
        downloadThread?.start()
    }

    private fun createNotification(progress: Int, episodeInfo: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading...")
            .setContentText(episodeInfo)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(progress: Int, episodeInfo: String) {
        val notification = createNotification(progress, episodeInfo)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun showCompleteNotification(episodeInfo: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText(episodeInfo)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notification)
    }

    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Failed")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 2, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        downloadThread?.interrupt()
    }
}
