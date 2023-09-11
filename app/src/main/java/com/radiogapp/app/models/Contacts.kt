package com.radiogapp.app.models


import com.google.gson.annotations.SerializedName

data class Contacts(
    @SerializedName("Contacts")
    val contacts: List<Contact>
)