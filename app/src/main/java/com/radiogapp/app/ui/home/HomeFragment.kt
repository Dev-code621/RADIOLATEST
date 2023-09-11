package com.radiogapp.app.ui.home

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.GsonBuilder
import com.radiogapp.app.RemoteData
import com.radiogapp.app.adapters.StationsAdapter
import com.radiogapp.app.databinding.FragmentHomeBinding
import com.radiogapp.app.models.Contacts
import com.radiogapp.app.models.Stations
import org.json.JSONObject
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {

    val sharedPref: SharedPreferences by inject()
    lateinit var stationsAdapter: StationsAdapter

    companion object {
        var globalSharedPref: SharedPreferences? = null
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        globalSharedPref = sharedPref
        homeViewModel.text.observe(viewLifecycleOwner) {

        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val columns: Int =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
        binding.stationsRcv.layoutManager = GridLayoutManager(requireContext(), columns)
        binding.stationsRcv.addItemDecoration(GridSpacingItemDecoration(2, 15))

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val remoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings)
        stationsAdapter = StationsAdapter(
            requireActivity(),
            RemoteData.stationsList,
            findNavController(),
            HomeFragmentDirections,
            sharedPref
        )
        binding.stationsRcv.adapter = stationsAdapter
//        binding.stationsRcv.adapter = StationsAdapter(
//            requireActivity(),
//            RemoteData.stationsList,
//            findNavController(),
//            HomeFragmentDirections, sharedPref
//        )
        // Live data changes listener
        binding.constraintLayout.setOnClickListener {

        }
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains("Stations")) {
                    RemoteData.stationsList.clear()
                    remoteConfig.fetch().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            remoteConfig.activate().addOnCompleteListener { activateTask ->
                                if (activateTask.isSuccessful) {
                                    RemoteData.stationsList.clear()
                                    processData(remoteConfig)
                                }
                            }
                        } else {
                            // Handle the error.
                            Log.e("REMOTE", "Fetch failed")
                        }
                    }

                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w("REMOTE", "Config update error with code: " + error.code, error)
            }
        })

    }

    class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            outRect.top = spacing
        }
    }

    private fun processData(remoteConfig: FirebaseRemoteConfig) {
        val adConfigParam = remoteConfig.getString("Stations")
        val adsJson = JSONObject(adConfigParam)
        val stationsArray = adsJson.getJSONArray("Stations")
        for (i in 0 until stationsArray.length()) {
            val obj = stationsArray.getJSONObject(i)
            RemoteData.stationsList.add(
                Stations(
                    obj.getString("name"),
                    obj.getString("url"),
                    obj.getString("color"),
                    obj.getBoolean("ads"),
                    obj.getBoolean("main"),
                    obj.getBoolean("active")

                )

            )
            Log.d("REMOTE", "processData: ${RemoteData.stationsList}")
            stationsAdapter.notifyDataSetChanged()
        }
        val adsConfigParam = remoteConfig.getString("admob")
        val addsJson = JSONObject(adsConfigParam)
        val androidAds = addsJson.getJSONObject("android")
        RemoteData.rewardedAdId = androidAds.getString("interstitial_reward")

        val contactsConfigParam = remoteConfig.getString("Contacts")
        val contactsJson = JSONObject(contactsConfigParam).toString()
        RemoteData.contacts = GsonBuilder().create().fromJson(contactsJson, Contacts::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}