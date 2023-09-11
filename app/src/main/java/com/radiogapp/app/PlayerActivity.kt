package com.radiogapp.app//package com.quran.alquran.holyquran.islam.activities
//
//import android.content.pm.ActivityInfo
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.WindowManager
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.Toast
//import com.google.android.exoplayer2.ExoPlayer
//import com.google.android.exoplayer2.MediaItem
//import com.google.android.exoplayer2.ui.StyledPlayerView
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.interstitial.InterstitialAd
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.quran.alquran.holyquran.islam.R
//import com.quran.alquran.holyquran.islam.activities.MainActivity.Companion.fromPlayer
//import com.quran.alquran.holyquran.islam.databinding.ActivityPlayerBinding
//import com.quran.alquran.holyquran.islam.utils.General
//import com.quran.alquran.holyquran.islam.utils.RemoteData
//import com.quran.alquran.holyquran.islam.utils.setOnOneClickListener
//
//class PlayerActivity : AppCompatActivity() {
//    private lateinit var exoPlayer: ExoPlayer
//    lateinit var exoPlayerView: StyledPlayerView
//    var link: String? = null
//    var volume : Boolean = true
//    private var mInterstitialAd: InterstitialAd? = null
//    lateinit var binding : ActivityPlayerBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityPlayerBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        exoPlayer = ExoPlayer.Builder(this).build()
//        hideMobileBar()
//
//        binding.exoPlayerView.findViewById<ImageView>(R.id.exoBackButton).setOnOneClickListener{
//            fromPlayer = true
//            finish()
//
////            interad()
//        }
//        binding.exoPlayerView.findViewById<ImageView>(R.id.imageFullScreen).setOnClickListener {
//            if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//                binding.exoPlayerView.findViewById<ImageView>(R.id.imageFullScreen).setImageResource(R.drawable.les_screen_ic)
//
//            } else {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
//                binding.exoPlayerView.findViewById<ImageView>(R.id.imageFullScreen).setImageResource(R.drawable.full_screen_ic)
//
//            }
//        }
//        binding.exoPlayerView.findViewById<ImageView>(R.id.exo_volume).setOnClickListener{
//            if (!volume){
//                exoPlayer.volume = 1f
//                binding.exoPlayerView.findViewById<ImageView>(R.id.exo_volume).setImageResource(R.drawable.sound_on)
//                volume = true
//            }else{
//                exoPlayer.volume = 0f
//                binding.exoPlayerView.findViewById<ImageView>(R.id.exo_volume).setImageResource(R.drawable.sound_off)
//                volume = false
//            }
//
//
//
//        }
//
//
//    }
//
//    private fun hideMobileBar() {
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );
//        window.decorView.apply {
//            systemUiVisibility =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//        }
//    }
//
//    fun playChannel() {
//        if (intent.hasExtra("link") == true) {
//            link = intent.getStringExtra("link")
//
//        }
//        exoPlayerView = findViewById(R.id.exoPlayerView)
//        exoPlayerView.player = exoPlayer
//        exoPlayerView.hideController()
//
//        var mediaItem: MediaItem? = null
//        mediaItem = MediaItem.fromUri(link.toString())
//        exoPlayer.addMediaItem(mediaItem)
//        exoPlayer.playWhenReady = true
//        exoPlayer.prepare()
//        exoPlayer.play()
//
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        playChannel()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        exoPlayer.pause()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        exoPlayer.release()
//    }
//
//    override fun onBackPressed() {
//     fromPlayer = true
//        finish()
////        Toast.makeText(this, "ShowAd", Toast.LENGTH_SHORT).show()
////        interad()
//    }
//
//
//}