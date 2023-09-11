package com.radiogapp.app.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.*


class MediaPlaybackService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, "ALARM_MEDIA_SESSION").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
            setPlaybackState(stateBuilder.build())

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    val builder =

                    this@MediaPlaybackService.startService(
                        Intent(
                            applicationContext,
                            MediaPlaybackService::class.java
                        )
                    )
                }

                override fun onStop() {

                }
            })

            setSessionToken(sessionToken)

            isActive = true
        }

        Log.d("aaaaaa", "start Media Service");
        try {
            val filter = IntentFilter(ExoService.BROADCAST_ACTION_METADATA_CHANGED)

            registerReceiver(broadcastReceiver, filter)
        }catch (e:Exception){

        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter()
        registerReceiver(broadcastReceiver, intentFilter)
        return START_STICKY
    }


    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("cccccccc", "test");
            val name  =
                intent?.getStringExtra("name")
            val artist  =
                intent?.getStringExtra("artist")
            val stationName  =
                intent?.getStringExtra("stationName")
            mediaSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,  name)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, stationName)
                    .build()
            )
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null);
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}