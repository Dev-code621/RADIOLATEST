package com.radiogapp.app.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.radiogapp.app.ui.home.HomeFragment
import org.koin.android.ext.android.inject
import java.util.Calendar


object RewardedAdd {

    private var rewardedAd: RewardedAd? = null
    private final var TAG = "MainActivity"


    fun loadRewardedAd(context : Context,id : String,onAdLoad : () -> Unit,onAdFailed :() -> Unit){
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(context,"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "error$adError")
                rewardedAd = null
                onAdFailed.invoke()
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                onAdLoad.invoke()
            }
        })
    }
    fun showRewardedAd(context: Activity, onAdDismissedFullScreenContent: () -> Unit){
        rewardedAd?.let { ad ->
            ad.show(context, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward.")
            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
                onAdDismissedFullScreenContent.invoke()
                HomeFragment.globalSharedPref?.edit()?.putLong("last_shown",Calendar.getInstance().timeInMillis)?.apply()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }
}