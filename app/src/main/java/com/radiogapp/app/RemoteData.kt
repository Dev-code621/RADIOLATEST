package com.radiogapp.app

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.GsonBuilder
import com.radiogapp.app.models.Contacts
import com.radiogapp.app.models.Stations
import org.json.JSONObject

class RemoteData {
    companion object {
        val stationsList: ArrayList<Stations> = ArrayList()
        var rewardedAdId: String? = null
        var contacts: Contacts? = null
    }

    fun fetch(getResponseListener: () -> Unit, failListener: () -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val remoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings)

        // Initial data fetch
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                stationsList.clear()
                val adConfigParam = remoteConfig.getString("Stations")
                val adsJson = JSONObject(adConfigParam)
                val stationsArray = adsJson.getJSONArray("Stations")
                for (i in 0 until stationsArray.length()) {
                    val obj = stationsArray.getJSONObject(i)
                    stationsList.add(
                        Stations(
                            obj.getString("name"),
                            obj.getString("url"),
                            obj.getString("color"),
                            obj.getBoolean("ads"),
                            obj.getBoolean("main"),
                            obj.getBoolean("active")

                        )

                    )
                    Log.d("REMOTEDATA", "processData: $stationsList")
                }
                val adsConfigParam = remoteConfig.getString("admob")
                val addsJson = JSONObject(adsConfigParam)
                val androidAds = addsJson.getJSONObject("android")
                rewardedAdId = androidAds.getString("interstitial_reward")

                val contactsConfigParam = remoteConfig.getString("Contacts")
                val contactsJson = JSONObject(contactsConfigParam).toString()
                contacts = GsonBuilder().create().fromJson(contactsJson, Contacts::class.java)
                getResponseListener.invoke()            } else {
                failListener.invoke()
            }
        }
    }

    private fun processData(remoteConfig: FirebaseRemoteConfig) {


    }
}
