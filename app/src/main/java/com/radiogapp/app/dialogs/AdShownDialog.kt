package com.radiogapp.app.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import com.radiogapp.app.databinding.AdShownDialogBinding

class AdShownDialog(val activity: Activity,val onOkClick: () -> Unit) : Dialog(activity) {
    lateinit var binding : AdShownDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdShownDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.okTxt.setOnClickListener {
            dismiss()
            onOkClick.invoke()
        }
    }


}