package com.radiogapp.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.ExoPlayer
import com.radiogapp.app.R
import com.radiogapp.app.service.ExoService
import com.radiogapp.app.ui.dashboard.DashboardFragment.Companion.exoPlayerGlobal
import org.koin.java.KoinJavaComponent.inject

class OnNotificationClick : BroadcastReceiver() {
    companion object{
        var onAction : ((action : String) -> Unit?)? = null

        var PLAY_PREVIOUS = "PLAY_PREVIOUS"
        var PLAY_PAUSE = "PLAY_PAUSE"
        var PLAY_NEXT = "PLAY_NEXT"
    }


     @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.getStringExtra("action") == "play"){
 onAction?.invoke(PLAY_PAUSE)
       }
       else if (intent?.getStringExtra("action") == "next"){
            onAction?.invoke(PLAY_NEXT)


       }
        else if(intent?.getStringExtra("action") == "previous"){
            onAction?.invoke(PLAY_PREVIOUS)

        }
    }
}