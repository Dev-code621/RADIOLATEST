package com.radiogapp.app.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import com.radiogapp.app.RemoteData
import com.radiogapp.app.databinding.AdPermissionDialogBinding
import com.radiogapp.app.utils.RewardedAdd

class PermissionsDialog (val activity: Activity,onOkClick : () -> Unit) : Dialog(activity) {
    lateinit var binding : AdPermissionDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdPermissionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
       binding.okTxt.setOnClickListener {
           RewardedAdd.loadRewardedAd(activity,RemoteData.rewardedAdId.toString(),onAdLoad,onAdFailed)
           dismiss()
       }
    }
    var onAdLoad : () -> Unit = {
        RewardedAdd.showRewardedAd(activity,onAdDismissedFullScreenContent)

    }
    var onAdFailed : () -> Unit = {
     dismiss()
    }
    var onAdDismissedFullScreenContent : () -> Unit = {
        AdShownDialog(activity,onOkClick).show()
    }

}