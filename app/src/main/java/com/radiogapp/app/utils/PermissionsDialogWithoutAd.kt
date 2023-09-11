package com.radiogapp.app.utils

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import com.radiogapp.app.databinding.AdPermissionDialogBinding

class PermissionsDialogWithoutAd(val activity: Activity) : Dialog(activity) {
    lateinit var binding : AdPermissionDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdPermissionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.okTxt.setOnClickListener {
            dismiss()
        }
    }


}