package com.radiogapp.app.utils


import com.radiogapp.app.R
import com.radiogapp.app.service.ExoService

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.exoplayer2.MediaMetadata

class MediaNotificationManager(private val service: ExoService) {

    private val resources = service.resources
    private val strAppName = resources.getString(R.string.app_name)
    private val strLiveBroadcast = "DashboardFragment"
    private var playbackStatus: String? = null
    private var stationName: String? = null
    private var notifyIcon: Bitmap? = null
    private var meta:MediaMetadata? = null

    fun startNotify(playbackStatus: String) {
        this.playbackStatus = playbackStatus
        startNotify()
    }
    fun startNotify(playbackStatus: String, stationName: String) {
        this.playbackStatus = playbackStatus
        this.stationName = stationName // Add this as a class property as well
        startNotify()
        Log.d("stationName", "onStad: $stationName")

    }

    fun startNotify(notifyIcon: Bitmap, meta: MediaMetadata) {
        this.notifyIcon = notifyIcon
        this.meta = meta
        startNotify()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startNotify() {
        playbackStatus ?: return

        notifyIcon = notifyIcon ?: BitmapFactory.decodeResource(resources, R.drawable.holder)

        val notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                service.getString(R.string.audio_notification),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableVibration(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }

        val icon = if (playbackStatus == PlaybackStatus.PAUSED) {
            R.drawable.play_notification
        } else {
            R.drawable.pause_notification
        }

        val playbackAction = Intent(service, ExoService::class.java).apply {
            action="PLAY_BT_CLICK"
        }
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(service, 1, playbackAction, PendingIntent.FLAG_IMMUTABLE or 0)
        } else {
            PendingIntent.getService(service, 1, playbackAction, 0)
        }


        val nextActionIntent = Intent(service, ExoService::class.java)
        nextActionIntent.action = ExoService.ACTION_NEXT
        val nextActionPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(service, 2, nextActionIntent, PendingIntent.FLAG_IMMUTABLE or 0)
        } else {
            PendingIntent.getService(service, 2, nextActionIntent, 0)
        }


        val prevActionIntent = Intent(service, ExoService::class.java)
        prevActionIntent.action = ExoService.ACTION_PREVIOUS
        val prevActionPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(service, 3, prevActionIntent, PendingIntent.FLAG_IMMUTABLE or 0)
        } else {
            PendingIntent.getService(service, 3, prevActionIntent, 0)
        }
        Log.d("stationName", "ontartCommand: $stationName")


        NotificationManagerCompat.from(service).cancel(NOTIFICATION_ID)
        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID).apply {
            val title = stationName ?: strLiveBroadcast
            val subTitle = meta?.title ?: strAppName
            setContentTitle(title)
            setContentText(subTitle)
            setLargeIcon(notifyIcon)
            setSmallIcon(R.drawable.ic_radio_playing)
            setContentIntent(service.getPendingIntentForNotification())
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            addAction(
                com.google.android.exoplayer2.R.drawable.exo_icon_previous,
                "previous",
                prevActionPendingIntent
            )
            addAction(icon, "pause", action)
            addAction(
                com.google.android.exoplayer2.R.drawable.exo_icon_next,
                "next",
                nextActionPendingIntent
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setVibrate(longArrayOf(0L))
            setWhen(System.currentTimeMillis())
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
            )
        }

        val notification = builder.build()
        service.startForeground(NOTIFICATION_ID, notification)
    }

    fun resetMetaData() {
        this.meta = null
    }

    fun cancelNotify() {
        service.stopForeground(true)
    }

    companion object {
        const val NOTIFICATION_ID = 555
        const val NOTIFICATION_CHANNEL_ID = "single_radio_channel"
        const val FRAGMENT_DATA = "transaction_data"
        const val FRAGMENT_CLASS = "transaction_target"
    }
}
