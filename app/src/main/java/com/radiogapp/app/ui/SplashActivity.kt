package com.radiogapp.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.radiogapp.app.MainActivity
import com.radiogapp.app.R
import com.radiogapp.app.RemoteData
import com.radiogapp.app.RemoteData.Companion.stationsList
import com.radiogapp.app.utils.AdmobBanner
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {
    val bannerAds : AdmobBanner by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        RemoteData().fetch(onLoad, onFail)

    }

    var onLoad: () -> Unit = {
        startActivity(Intent(this, MainActivity::class.java))
        Log.d("list***",stationsList.toString())
        bannerAds.loadMrcBannerAd(this,"123",{},{})
    }
    var onFail: () -> Unit = {
        showFailedDialog()
    }

    private fun showFailedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.whoops)
        builder.setMessage(resources.getString(R.string.dialog_internet_description))
        builder.setPositiveButton("Ok") { dialog, which -> finish() }
        builder.show()
    }
}