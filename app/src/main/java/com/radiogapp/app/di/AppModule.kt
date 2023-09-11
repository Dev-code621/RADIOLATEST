package com.app.workoutfitnessreminder.di


import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.radiogapp.app.utils.AdmobBanner
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

var appModule = module {
 single {
     ExoPlayer.Builder(get()).build()
 }
    single {
        androidContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
    }
    single {
        AdmobBanner()
    }

}