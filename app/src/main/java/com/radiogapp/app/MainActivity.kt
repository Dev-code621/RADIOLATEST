package com.radiogapp.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.exoplayer2.ExoPlayer
import com.radiogapp.app.databinding.ActivityMainBinding
import com.radiogapp.app.service.ExoService
import org.koin.android.ext.android.inject
import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.radiogapp.app.service.MediaPlaybackService

class MainActivity : AppCompatActivity() {
    companion object{
        var isFirstTime : Boolean = true
        var toHome : Boolean = false
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101

    }
    private var doubleBackToExitPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())

    val exoPlayer : ExoPlayer by inject()

    private lateinit var binding: ActivityMainBinding
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstTime = true
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 is API level 31 (Build.VERSION_CODES.S)
            requestNotificationPermission()
        }
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.findViewById<View>(R.id.navigation_home).setOnClickListener {
            navController?.navigate(R.id.navigation_home)
        }
        navView.findViewById<View>(R.id.navigation_dashboard).setOnClickListener {
            navController?.navigate(R.id.navigation_dashboard)
        }
        navView.findViewById<View>(R.id.navigation_notifications).setOnClickListener {
            navController?.navigate(R.id.navigation_notifications)
        }
        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
            // Perform actions or updates based on the current destination
            when (destination.id) {
                R.id.navigation_home -> {
                        navView.selectedItemId = R.id.navigation_home
                }
                R.id.navigation_dashboard -> {
                    navView.selectedItemId = R.id.navigation_dashboard
                }
                R.id.navigation_notifications -> {
                        navView.selectedItemId = R.id.navigation_notifications
                }
            }
        }


    }
    private fun requestNotificationPermission() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Optionally, inform the user why the permission is important to your app and prompt again or guide them to settings
                }
            }
            // Handle other permission results if any
        }
    }

    override fun onBackPressed() {
        if (navController?.currentDestination?.id == R.id.navigation_dashboard) {
            if (doubleBackToExitPressedOnce) {
                // Move the app to background, equivalent to pressing the home button
                moveTaskToBack(true)
                return
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

            handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000) // Reset the flag after 2 seconds
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)  // Remove any pending callbacks

        exoPlayer.release()
        val intent = Intent(this, ExoService::class.java)
        intent.action = "STOP_SERVICE"
        startForegroundService(intent)

        val intent2= Intent(this, MediaPlaybackService::class.java)
        startForegroundService(intent2)
        finishAffinity()
    }
}