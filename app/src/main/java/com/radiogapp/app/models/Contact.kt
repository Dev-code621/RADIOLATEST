package com.radiogapp.app.models


import com.google.gson.annotations.SerializedName

data class Contact(
    @SerializedName("link")
    val link: String,
    @SerializedName("name")
    val name: String
)