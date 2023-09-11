package com.radiogapp.app.utils

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.*

class AdmobBanner {

    var bannerAdView: AdView? = null
    var mrcBanner : AdView? = null
    fun loadAllBannerAd(activity: Activity, bannerId: String) {
        bannerAdView = AdView(activity)
//        bannerAdView?.adUnitId = bannerId
        bannerAdView?.adUnitId = bannerId
        bannerAdView?.setAdSize(AdSize.BANNER)
        val adRequest = AdRequest.Builder().build()
        bannerAdView?.loadAd(adRequest)
        bannerAdView?.adListener = object : AdListener() {
            override fun onAdClicked() {

                // Code to be executed when the user clicks on an ad.
                loadAllBannerAd(activity, bannerId)
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("banner**", "$adError")
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                Log.e("banner**", "$bannerAdView")
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }


    }
    fun loadMrcBannerAd(activity: Activity, bannerId: String,onLoad : () -> Unit,onFail : () -> Unit) {

        mrcBanner = AdView(activity)
//        mrcBanner?.adUnitId = bannerId
        mrcBanner?.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        mrcBanner?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        val adRequest = AdRequest.Builder().build()
        mrcBanner?.loadAd(adRequest)
        mrcBanner?.adListener = object : AdListener() {
            override fun onAdClicked() {

                // Code to be executed when the user clicks on an ad.
                loadMrcBannerAd(activity, bannerId,onLoad,onFail)
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("banner**", "$adError")
                // Code to be executed when an ad request fails.
                onFail.invoke()
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                Log.e("banner**", "$mrcBanner")
                onLoad.invoke()
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }


    }

}