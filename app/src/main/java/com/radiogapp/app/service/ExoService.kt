package com.radiogapp.app.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.radiogapp.app.MainActivity
import com.radiogapp.app.utils.AlbumArtGetter
import com.radiogapp.app.utils.MediaNotificationManager
import com.radiogapp.app.utils.PlaybackStatus
import com.radiogapp.app.utils.SharedPrefHelper
import org.koin.android.ext.android.inject
import org.koin.dsl.koinApplication


class ExoService : Service() {
    val exoPlayer: ExoPlayer by inject()

    private lateinit var notificationManager: MediaNotificationManager

    var albumArtCallback: AlbumArtCallback? = null
    var iPlayButton: IPlayButtonCallback? = null

    inner class LocalBinder : Binder() {
        fun getService(): ExoService = this@ExoService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    private var nextCallback: INextCallback? = null
    fun setNextCallback(callback: INextCallback) {
        nextCallback = callback
    }

    private var previousCallback: IPreviousCallback? = null
    fun setPreviousCallback(callback: IPreviousCallback) {
        previousCallback = callback
    }

    fun setIPlayButtonCall(callback: IPlayButtonCallback) {
        iPlayButton = callback
    }

    fun setAlbumArtCall(callback: AlbumArtCallback) {
        albumArtCallback = callback
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = MediaNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        when (intent?.action) {
            "STOP_SERVICE" -> {

                stopForeground(STOP_FOREGROUND_REMOVE)


            }

            "AUTO_PLAY" -> {
                val stationName = intent?.getStringExtra("stationName")
                Log.d("stationName", "onStartCommand: $stationName")

                notificationManager.startNotify(PlaybackStatus.PLAYING, stationName.toString())

                val mediaItem = MediaItem.fromUri(intent?.getStringExtra("link").toString())
                Log.d("linkkk***", intent?.getStringExtra("link").toString())
                exoPlayer.clearMediaItems() // Clear the current playlist
                exoPlayer.setMediaItem(mediaItem) // Set the new media item
                exoPlayer.playWhenReady = true
                exoPlayer.prepare()



                exoPlayer.addListener(object : Player.Listener {
                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        super.onMediaMetadataChanged(mediaMetadata)

                        updateNotificationWithMetadata(mediaMetadata, stationName.toString())

                    }
                })

                exoPlayer.play()

            }

            ACTION_PREVIOUS -> {
                previousCallback?.onPrevious()
            }

            ACTION_NEXT -> {
                nextCallback?.onNext()
            }

            "PLAY_BT_CLICK" -> {


                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    notificationManager.startNotify(PlaybackStatus.PAUSED)
                    iPlayButton?.onPlayButtonClick(true)

                } else {
                    exoPlayer.play()
                    notificationManager.startNotify(PlaybackStatus.PLAYING)
                    iPlayButton?.onPlayButtonClick(false)

                }

            }
        }


        return START_REDELIVER_INTENT
    }

    @SuppressLint("NewApi")
    private fun updateNotificationWithMetadata(mediaMetadata: MediaMetadata? , stationName:String) {
        val broadcastIntent = Intent(BROADCAST_ACTION_METADATA_CHANGED)
        val sharedPrefHelper = SharedPrefHelper(this)
        if (mediaMetadata != null) {
            sharedPrefHelper.saveMetaData(mediaMetadata)
        }
        val artist = mediaMetadata?.artist
        val title = mediaMetadata?.title.toString().replace(" - ", " ")

        AlbumArtGetter.getImageForQuery(
            title.toString(),
            AlbumArtGetter.AlbumCallback { albumArtImage ->
                if (albumArtImage != null) {
                    albumArtCallback?.onAlbumArtReceived(albumArtImage)
                    sharedPrefHelper.saveImage(albumArtImage)
                    if (mediaMetadata != null) {
                        notificationManager.startNotify(albumArtImage, mediaMetadata)
                    }
                } else {
                    // Handle the error case where albumArtImage is null.
                    // Perhaps set a default image or inform the user, etc.
                }
            })
        var array = mediaMetadata?.title.toString().split(" - ")
        var name = ""
        var artists = ""
        if(array.size>0){
            name = array.get(0)
        }
        if(array.size>1) artists = array.get(1)
        broadcastIntent.putExtra("title", mediaMetadata?.title.toString())
        broadcastIntent.putExtra("artist", artists)
        broadcastIntent.putExtra("name", name)
        broadcastIntent.putExtra("stationName", stationName)
        sendBroadcast(broadcastIntent)


    }


    private fun getBitmapFromArtworkData(artworkData: ByteArray?): Bitmap? {
        return if (artworkData != null && artworkData.isNotEmpty()) {
            BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size)
        } else {
            null
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)

        } catch (e: Exception) {

            Log.e(TAG, "onStartCommand: $e")
        }
    }

    interface AlbumArtCallback {
        fun onAlbumArtReceived(albumArtImage: Bitmap) // or any other datatype you use for albumArtImage
    }

    interface INextCallback {
        fun onNext()
    }

    interface IPreviousCallback {
        fun onPrevious()
    }
    fun getPendingIntentForNotification(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_DASHBOARD"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, 0, intent, flags)
    }


    interface IPlayButtonCallback {
        fun onPlayButtonClick(isPlaying: Boolean)
    }

    companion object {
        const val BROADCAST_ACTION_METADATA_CHANGED = "com.radiogapp.app.service.METADATA_CHANGED"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREVIOUS = "PREVIOUS"
    }

}