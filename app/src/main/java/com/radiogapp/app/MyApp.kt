package com.radiogapp.app

import android.R
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.app.workoutfitnessreminder.di.appModule
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class MyApp : Application() {
    companion object{
        val CHANNEL_ID = "Radio G App"

    }
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        MobileAds.initialize(this) {}
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the N otificationChannel.
            val name = "Radio_G"
            val descriptionText = "RadioG Channels"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }
}