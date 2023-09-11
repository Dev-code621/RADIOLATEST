package com.radiogapp.app.ui.dashboard

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.radiogapp.app.MainActivity.Companion.isFirstTime
import com.radiogapp.app.R
import com.radiogapp.app.RemoteData
import com.radiogapp.app.databinding.FragmentDashboardBinding
import com.radiogapp.app.models.DataMeta
import com.radiogapp.app.service.ExoService
import com.radiogapp.app.service.MediaPlaybackService
import com.radiogapp.app.utils.AdmobBanner
import com.radiogapp.app.utils.OnNotificationClick
import com.radiogapp.app.utils.PermissionsDialogWithoutAd
import com.radiogapp.app.utils.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Calendar
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess


class DashboardFragment : Fragment(), CoroutineScope, ExoService.AlbumArtCallback,
    ExoService.IPlayButtonCallback,
    ExoService.INextCallback, ExoService.IPreviousCallback {
    private var _binding: FragmentDashboardBinding? = null
    val exoPlayer: ExoPlayer by inject()
    val args: DashboardFragmentArgs by navArgs()
    val sharedPref: SharedPreferences by inject()
    val bannerAds: AdmobBanner by inject()
    private lateinit var nowPlaying: TextView
    private lateinit var nowPlayingTitle: TextView
    private lateinit var stationName: TextView
    private lateinit var stationBG: LinearLayoutCompat
    private lateinit var albumArt: ImageView
    private lateinit var btnPlayPause: ImageView
    private lateinit var playPrevious: ImageView
    private lateinit var playNext: ImageView
    private lateinit var bannerAd: ConstraintLayout
    private lateinit var bannerAdContainer: ConstraintLayout
    lateinit var sharedPrefHelper: SharedPrefHelper

    companion object {
        var currentPlayying: Int? = null
        var exoPlayerGlobal: ExoPlayer? = null
    }

    private var mService: ExoService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Cast the IBinder and get MyService instance
            val binder = service as ExoService.LocalBinder
            mService = binder.getService()
            mService?.setAlbumArtCall(this@DashboardFragment)
            mService?.setNextCallback(this@DashboardFragment)
            mService?.setPreviousCallback(this@DashboardFragment)
            mService?.setIPlayButtonCall(this@DashboardFragment)

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mService = null
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        nowPlaying = view.findViewById(R.id.now_playing)
        nowPlayingTitle = view.findViewById(R.id.now_playing_title)
        stationName = view.findViewById(R.id.station_name)
        stationBG = view.findViewById(R.id.stationBG)
        albumArt = view.findViewById(R.id.albumArt)
        playPrevious = view.findViewById(R.id.play_previous)
        playNext = view.findViewById(R.id.play_next)
        btnPlayPause = view.findViewById(R.id.btn_play_pause)
        bannerAd = view.findViewById(R.id.bannerAd)
        bannerAdContainer = view.findViewById(R.id.banner_ad_container)
        exoPlayerGlobal = exoPlayer
        sharedPrefHelper = SharedPrefHelper(requireContext())

        return view;
    }


    fun showBanner() {
        bannerAd.visibility = View.VISIBLE
        albumArt.visibility = View.GONE


        val bannerAdViewParent = bannerAds.mrcBanner?.parent as? ViewGroup
        bannerAdViewParent?.removeView(bannerAds.mrcBanner)
        bannerAdContainer.removeAllViews()
        if (bannerAds.mrcBanner != null) {
            bannerAdContainer.addView(bannerAds.mrcBanner)
        }
    }

    private val metadataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            launch {
                val title =
                    intent?.getStringExtra("title")
                        ?: return@launch  // Return if title is null or empty
                val artist = intent.getStringExtra("artist") ?: ""
                if (title.isNullOrEmpty()) return@launch

                bannerAd.visibility = View.GONE
                albumArt.visibility = View.VISIBLE

                nowPlaying.text = ""
                nowPlaying.text = title
                nowPlaying.visibility = View.VISIBLE
                nowPlaying.isSelected = true
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("sSHASASAS", "onViewCreateaaa: ${args.position}")
        showBanner()
        if (args.position != -1) {
            currentPlayying = args.position
            val intent = Intent(requireContext(), ExoService::class.java).apply {
                action = "AUTO_PLAY"
                putExtra("link", RemoteData.stationsList?.get(args.position)?.url)
                putExtra("stationName", RemoteData.stationsList?.get(args.position)?.name)

            }
            requireContext().startForegroundService(intent)
            stationName.text = RemoteData.stationsList?.get(args.position)?.name
            stationBG.setBackgroundColor(
                Color.parseColor(
                    "#" + RemoteData.stationsList?.get(
                        args.position
                    )?.color
                )
            )
        } else {
            if (currentPlayying == null) {
                RemoteData.stationsList?.forEachIndexed { index, stations ->
                    if (stations.main == true) {
                        currentPlayying = index
                        val intent = Intent(requireContext(), ExoService::class.java)
                        intent.action = "AUTO_PLAY"
                        intent.putExtra("link", stations.url)
                        intent.putExtra("stationName", stations.name)
                        requireContext().startForegroundService(intent)
                        stationName.text = stations.name


                    }


                }

            } else {
                stationName.text = RemoteData.stationsList?.get(currentPlayying!!)?.name
                stationBG.setBackgroundColor(
                    Color.parseColor(
                        "#" + RemoteData.stationsList?.get(
                            currentPlayying!!
                        )?.color
                    )
                )
            }
        }
        OnNotificationClick.onAction = { action ->
            when (action) {
                OnNotificationClick.PLAY_PAUSE -> {
                    onplaypause()

                }

                OnNotificationClick.PLAY_NEXT -> {
                    playNext()
                }

                OnNotificationClick.PLAY_PREVIOUS -> {
                    playprevious()
                }
            }

        }


        onClicks()
        isFirstTime = false

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClicks() {
        btnPlayPause.setOnClickListener {
            onplaypause()

        }
        playNext.setOnClickListener() {
            playNext()
        }
        playPrevious.setOnClickListener {
            playprevious()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onplaypause() {
        if (exoPlayer.isPlaying == true) {
            btnPlayPause.setImageResource(R.drawable.playbtns)
        } else {
            btnPlayPause.setImageResource(R.drawable.pausebtn)
        }

        val intent = Intent(requireContext(), ExoService::class.java)
        intent.action = "PLAY_BT_CLICK"

        requireContext().startForegroundService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playprevious() {

        currentPlayying?.let {
            if (it > 0 && RemoteData.stationsList?.get(it.minus(1))?.ads == false) {

                if (it > 0) {


                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(it.minus(1))?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.get(it.minus(1))?.name)
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                it.minus(1)
                            )?.color
                        )
                    )

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(it.minus(1))?.name
                    currentPlayying = it - 1


                } else {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.last()?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.last()?.name)
                    stationBG.setBackgroundColor(Color.parseColor("#" + RemoteData.stationsList?.last()?.color))

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.last()?.name
                    currentPlayying = RemoteData.stationsList?.size
                }
            } else if (!isCurrentTimeOneHourGreater(
                    sharedPref.getLong(
                        "last_shown",
                        Calendar.getInstance().timeInMillis - (60 * 60 * 1000)
                    )
                )
            ) {
                if (it > 0) {


                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(it.minus(1))?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.get(it.minus(1))?.name)
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                it.minus(1)
                            )?.color
                        )
                    )

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(it.minus(1))?.name
                    currentPlayying = it - 1


                } else {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.last()?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.last()?.name)
                    stationBG.setBackgroundColor(Color.parseColor("#" + RemoteData.stationsList?.last()?.color))

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.last()?.name
                    currentPlayying = RemoteData.stationsList?.size
                }
                albumArt.setBackgroundResource(R.drawable.holder)

            } else {

                PermissionsDialogWithoutAd(requireActivity()).show()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playNext() {
        currentPlayying?.let {

            if (it < (RemoteData.stationsList?.size?.minus(1) ?: 0) && RemoteData.stationsList?.get(
                    it.plus(1)
                )?.ads == false
            ) {
                if (it < (RemoteData.stationsList?.size?.minus(1) ?: 0)) {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(it.plus(1))?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.get(it.plus(1))?.name)

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(it.plus(1))?.name
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                it.plus(1)
                            )?.color
                        )
                    )
                    currentPlayying = it + 1

                } else {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(0)?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.get(0)?.name)
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                0
                            )?.color
                        )
                    )

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(0)?.name
                    currentPlayying = 0
                }
            } else if (!isCurrentTimeOneHourGreater(
                    sharedPref.getLong(
                        "last_shown",
                        Calendar.getInstance().timeInMillis - (60 * 60 * 1000)
                    )
                )
            ) {
                if (it < (RemoteData.stationsList?.size?.minus(1) ?: 0)) {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(it.plus(1))?.url)
                    intent.putExtra("stationName", RemoteData.stationsList?.get(it.plus(1))?.name)
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                it.plus(1)
                            )?.color
                        )
                    )

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(it.plus(1))?.name
                    currentPlayying = it + 1

                } else {

                    val intent = Intent(requireContext(), ExoService::class.java)
                    intent.action = "AUTO_PLAY"
                    intent.putExtra("link", RemoteData.stationsList?.get(0)?.url)

                    intent.putExtra("stationName", RemoteData.stationsList?.get(0)?.name)
                    stationBG.setBackgroundColor(
                        Color.parseColor(
                            "#" + RemoteData.stationsList?.get(
                                0
                            )?.color
                        )
                    )

                    requireContext().startForegroundService(intent)
                    stationName.text = RemoteData.stationsList?.get(0)?.name
                    currentPlayying = 0
                }
                albumArt.setBackgroundResource(R.drawable.holder)

            } else {

                PermissionsDialogWithoutAd(requireActivity()).show()
            }
        }
    }

    fun isCurrentTimeOneHourGreater(timeInMillis: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 60 * 60 * 1000 // 1 hour in milliseconds

        return currentTime >= timeInMillis + oneHourInMillis
    }

    override fun onResume() {
        super.onResume()
        val sharedPrefHelper = SharedPrefHelper(requireContext())
        val savedMetadata = sharedPrefHelper.getMetaData()
        val savedImage = sharedPrefHelper.getImage()
        if (savedMetadata?.title.toString().contains("NULL")) {
            Toast.makeText(requireContext(), "sa", Toast.LENGTH_SHORT).show()
        }

        nowPlaying.text = savedMetadata?.title.toString()
        if (nowPlaying.text.contains("NULL")) {
            nowPlaying.visibility = View.INVISIBLE
            Toast.makeText(requireContext(), "saa", Toast.LENGTH_SHORT).show()

        } else {
            nowPlaying.visibility = View.VISIBLE
        }
        nowPlaying.isSelected = true
        val dataMeta = DataMeta()
//        Glide.with(requireActivity()).load(savedImage).into(albumArt)
        Glide.with(requireActivity())
            .load(savedImage)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    albumArt.setBackgroundResource(R.drawable.holder)

                    return false  // Let Glide handle the error
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                albumArt.setBackgroundResource(R.drawable.holder)
                    return false  // Let Glide handle the setting of the image
                }
            })
            .into(albumArt)
        Intent(requireContext(), ExoService::class.java).also { intent ->
            requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
        bannerAd.visibility = View.GONE
        albumArt.visibility = View.VISIBLE
        mService?.setAlbumArtCall(this@DashboardFragment)
        mService?.setNextCallback(this@DashboardFragment)
        mService?.setPreviousCallback(this@DashboardFragment)
        mService?.setIPlayButtonCall(this@DashboardFragment)
        val filter = IntentFilter(ExoService.BROADCAST_ACTION_METADATA_CHANGED)
        requireActivity().registerReceiver(metadataReceiver, filter)

        Intent(requireContext(), MediaPlaybackService::class.java).also { intent ->
            requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
        requireActivity().registerReceiver(metadataReceiver, filter)
    }

    override fun onAlbumArtReceived(albumArtImage: Bitmap) {
        Log.d("TESTINGDA", "onAlbumArtReceived: $albumArtImage")
        if (albumArtImage == null) {
            return
        }
        Glide.with(requireActivity())
            .load(albumArtImage)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    albumArt.setBackgroundResource(R.drawable.holder)

                    return false  // Let Glide handle the error
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    albumArt.setBackgroundResource(R.drawable.holder)
                    return false  // Let Glide handle the setting of the image
                }
            })
            .into(albumArt)
        val data = DataMeta()
        data.image = albumArtImage
        sharedPrefHelper.saveImage(albumArtImage)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNext() {
        playNext()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPrevious() {
        playprevious()
    }

    override fun onPlayButtonClick(isPlaying: Boolean) {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.playbtns)
        } else {
            btnPlayPause.setImageResource(R.drawable.pausebtn)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }
}